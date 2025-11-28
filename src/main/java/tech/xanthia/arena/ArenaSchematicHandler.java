package tech.xanthia.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import tech.xanthia.XDuels;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles schematic capture and restoration for arenas.
 * 
 * Supports two modes:
 * 1. Schematic Mode: Captures full arena state and restores it
 * 2. Block Tracking Mode: Tracks only modified blocks during duels
 * 
 * Schematics are stored in a compressed format in:
 * plugins/xDuels/arenas/schematics/
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArenaSchematicHandler {
    
    private final XDuels plugin;
    private final File schematicsFolder;
    
    /**
     * Creates a new schematic handler.
     * 
     * @param plugin Plugin instance
     */
    public ArenaSchematicHandler(XDuels plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "arenas/schematics");
        
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }
    
    // ========================================
    // SCHEMATIC CAPTURE
    // ========================================
    
    /**
     * Captures a schematic of the arena region.
     * Must be called synchronously on the main thread.
     * 
     * @param arena Arena to capture
     * @return true if capture was successful
     */
    public boolean captureSchematic(Arena arena) {
        if (!arena.hasLocation(ArenaLocationType.CORNER1) || 
            !arena.hasLocation(ArenaLocationType.CORNER2)) {
            plugin.getLogger().warning("[ ArenaSchematicHandler ] Cannot capture schematic for " + 
                arena.getId() + " - missing corners");
            return false;
        }
        
        try {
            Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
            Location corner2 = arena.getLocation(ArenaLocationType.CORNER2);
            
            SimpleSchematic schematic = new SimpleSchematic(corner1, corner2);
            schematic.capture();
            
            File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
            schematic.save(schematicFile);
            
            plugin.getLogger().info("[ ArenaSchematicHandler ] Captured schematic for arena " + 
                arena.getId() + " (" + schematic.getBlockCount() + " blocks)");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("[ ArenaSchematicHandler ] Failed to capture schematic for " + 
                arena.getId() + ": " + e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // SCHEMATIC RESTORATION
    // ========================================
    
    /**
     * Restores an arena from its schematic.
     * Must be called synchronously on the main thread.
     * 
     * @param arena Arena to restore
     * @return true if restoration was successful
     */
    public boolean restoreSchematic(Arena arena) {
        File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
        
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("[ ArenaSchematicHandler ] Schematic not found for arena " + 
                arena.getId() + " - attempting auto-capture");
            return captureSchematic(arena);
        }
        
        try {
            Location corner1 = arena.getLocation(ArenaLocationType.CORNER1);
            
            SimpleSchematic schematic = SimpleSchematic.load(schematicFile);
            schematic.restore(corner1);
            
            arena.updateRegenTimestamp();
            
            plugin.getLogger().info("[ ArenaSchematicHandler ] Restored schematic for arena " + 
                arena.getId() + " (" + schematic.getBlockCount() + " blocks)");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("[ ArenaSchematicHandler ] Failed to restore schematic for " + 
                arena.getId() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a schematic exists for an arena.
     * 
     * @param arena Arena to check
     * @return true if schematic file exists
     */
    public boolean schematicExists(Arena arena) {
        File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
        return schematicFile.exists();
    }
    
    /**
     * Deletes a schematic file for an arena.
     * 
     * @param arena Arena whose schematic to delete
     * @return true if deleted successfully
     */
    public boolean deleteSchematic(Arena arena) {
        File schematicFile = new File(schematicsFolder, arena.getSchematicName() + ".schem");
        
        if (schematicFile.exists()) {
            return schematicFile.delete();
        }
        
        return false;
    }
    
    // ========================================
    // INNER CLASS: SIMPLE SCHEMATIC
    // ========================================
    
    /**
     * Simple schematic implementation for block-by-block capture and restore.
     * Uses GZIP compression for efficient storage.
     */
    private static class SimpleSchematic {
        
        private final int width, height, length;
        private final String worldName;
        private final List<BlockData> blocks;
        
        /**
         * Creates a schematic from two corner locations.
         * 
         * @param corner1 First corner
         * @param corner2 Second corner
         */
        public SimpleSchematic(Location corner1, Location corner2) {
            int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
            
            this.width = maxX - minX + 1;
            this.height = maxY - minY + 1;
            this.length = maxZ - minZ + 1;
            this.worldName = corner1.getWorld().getName();
            this.blocks = new ArrayList<>();
        }
        
        /**
         * Private constructor for loading from file.
         */
        private SimpleSchematic(int width, int height, int length, String worldName, List<BlockData> blocks) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.worldName = worldName;
            this.blocks = blocks;
        }
        
        /**
         * Captures all blocks in the schematic region.
         */
        public void capture() {
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) return;
            
            blocks.clear();
            
            // Iterate through all blocks in the cuboid
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {
                        Location loc = new Location(world, x, y, z);
                        Block block = loc.getBlock();
                        
                        blocks.add(new BlockData(
                            x, y, z,
                            block.getType(),
                            block.getBlockData().getAsString()
                        ));
                    }
                }
            }
        }
        
        /**
         * Restores all blocks from this schematic.
         * 
         * @param origin Origin point (typically corner1)
         */
        public void restore(Location origin) {
            org.bukkit.World world = origin.getWorld();
            if (world == null) return;
            
            int baseX = origin.getBlockX();
            int baseY = origin.getBlockY();
            int baseZ = origin.getBlockZ();
            
            for (BlockData blockData : blocks) {
                Location loc = new Location(
                    world,
                    baseX + blockData.x,
                    baseY + blockData.y,
                    baseZ + blockData.z
                );
                
                Block block = loc.getBlock();
                block.setType(blockData.material, false);
                
                if (blockData.blockDataString != null && !blockData.blockDataString.isEmpty()) {
                    try {
                        org.bukkit.block.data.BlockData blockDataObj = 
                            Bukkit.createBlockData(blockData.blockDataString);
                        block.setBlockData(blockDataObj, false);
                    } catch (Exception ignored) {
                        // Fallback to just material if block data fails
                    }
                }
            }
        }
        
        /**
         * Saves this schematic to a file.
         * 
         * @param file File to save to
         * @throws IOException If save fails
         */
        public void save(File file) throws IOException {
            try (DataOutputStream out = new DataOutputStream(
                    new GZIPOutputStream(new FileOutputStream(file)))) {
                
                // Write header
                out.writeInt(width);
                out.writeInt(height);
                out.writeInt(length);
                out.writeUTF(worldName);
                out.writeInt(blocks.size());
                
                // Write blocks
                for (BlockData block : blocks) {
                    out.writeInt(block.x);
                    out.writeInt(block.y);
                    out.writeInt(block.z);
                    out.writeUTF(block.material.name());
                    out.writeUTF(block.blockDataString != null ? block.blockDataString : "");
                }
            }
        }
        
        /**
         * Loads a schematic from a file.
         * 
         * @param file File to load from
         * @return Loaded schematic
         * @throws IOException If load fails
         */
        public static SimpleSchematic load(File file) throws IOException {
            try (DataInputStream in = new DataInputStream(
                    new GZIPInputStream(new FileInputStream(file)))) {
                
                // Read header
                int width = in.readInt();
                int height = in.readInt();
                int length = in.readInt();
                String worldName = in.readUTF();
                int blockCount = in.readInt();
                
                // Read blocks
                List<BlockData> blocks = new ArrayList<>();
                for (int i = 0; i < blockCount; i++) {
                    int x = in.readInt();
                    int y = in.readInt();
                    int z = in.readInt();
                    Material material = Material.valueOf(in.readUTF());
                    String blockDataString = in.readUTF();
                    
                    blocks.add(new BlockData(x, y, z, material, 
                        blockDataString.isEmpty() ? null : blockDataString));
                }
                
                return new SimpleSchematic(width, height, length, worldName, blocks);
            }
        }
        
        public int getBlockCount() {
            return blocks.size();
        }
    }
    
    /**
     * Simple block data holder.
     */
    private static class BlockData {
        final int x, y, z;
        final Material material;
        final String blockDataString;
        
        BlockData(int x, int y, int z, Material material, String blockDataString) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.blockDataString = blockDataString;
        }
    }
}
