package tech.xanthia.kits.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import tech.xanthia.XDuels;
import tech.xanthia.kits.Kit;
import tech.xanthia.kits.gui.*;

/**
 * Handles all kit-related GUI clicks (list, editor, rule editor, effects, rounds, attributes).
 */
public class KitGUIListener implements Listener {

    private final XDuels plugin;

    public KitGUIListener(XDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.contains("Kit Manager")) {
            event.setCancelled(true);
            handleKitListClick(player, event.getSlot(), clicked);
        } else if (title.contains("Edit Kit:")) {
            event.setCancelled(true);
            handleKitEditorClick(player, event.getSlot(), clicked, event.getClick());
        } else if (title.contains("Rules:")) {
            event.setCancelled(true);
            handleRuleEditorClick(player, event.getSlot(), clicked, event.getClick());
        } else if (title.contains("Effects:")) {
            event.setCancelled(true);
            handleEffectsClick(player, event.getSlot(), clicked, event.getClick(), title);
        } else if (title.contains("Rounds:")) {
            event.setCancelled(true);
            handleRoundsClick(player, event.getSlot(), clicked, event.getClick());
        } else if (title.contains("Attributes:")) {
            event.setCancelled(true);
            handleAttributesClick(player, event.getSlot(), clicked, event.getClick());
        }
    }

    private Kit getKitFromEditorTitle(String title) {
        // Titles are of form "§6§lEdit Kit: §e<name>" or similar
        int idx = title.indexOf(":");
        if (idx == -1 || idx + 1 >= title.length()) {
            return null;
        }
        String raw = title.substring(idx + 1).replace("§e", "").trim();
        if (raw.isEmpty()) {
            return null;
        }
        return plugin.getKitManager().getKit(raw);
    }

    // ================= Kit List =================
    private void handleKitListClick(Player player, int slot, ItemStack clicked) {
        if (slot >= 0 && slot < 45) {
            // Each item is a kit; use its name to resolve kit
            String name = clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()
                ? clicked.getItemMeta().getDisplayName().replace("§6§l", "")
                : null;
            if (name == null || name.isEmpty()) {
                return;
            }
            Kit kit = plugin.getKitManager().getKit(name);
            if (kit == null) {
                player.sendMessage("§cKit not found: §e" + name);
                return;
            }
            KitEditorGUI gui = new KitEditorGUI(plugin, kit);
            gui.open(player);
        } else if (slot == 45 && clicked.getType() == Material.ARROW) {
            // Previous page (not fully tracked yet)
            player.sendMessage("§7Previous page (not implemented)");
        } else if (slot == 53 && clicked.getType() == Material.ARROW) {
            player.sendMessage("§7Next page (not implemented)");
        }
    }

    // ================= Main Kit Editor =================
    private void handleKitEditorClick(Player player, int slot, ItemStack clicked, ClickType click) {
        Kit kit = getKitFromEditorTitle(player.getOpenInventory().getTitle());
        if (kit == null) {
            player.closeInventory();
            return;
        }
        KitEditorGUI gui = new KitEditorGUI(plugin, kit);

        switch (slot) {
            case 10 -> { // status toggle
                kit.setEnabled(!kit.isEnabled());
                player.sendMessage("§eKit " + kit.getName() + " " + (kit.isEnabled() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 11 -> { // display item editor (simple: use item in hand)
                ItemStack inHand = player.getInventory().getItemInMainHand();
                if (inHand != null && inHand.getType() != Material.AIR) {
                    kit.setDisplayItem(inHand.clone());
                    player.sendMessage("§aUpdated display item for kit §e" + kit.getName());
                    gui.open(player);
                } else {
                    player.sendMessage("§cHold the desired display item in your hand.");
                }
            }
            case 19 -> { // rule editor
                player.closeInventory();
                new KitRuleEditorGUI(plugin, kit).open(player);
            }
            case 20 -> { // attributes
                player.closeInventory();
                new KitAttributeGUI(plugin, kit).open(player);
            }
            case 21 -> { // effects
                player.closeInventory();
                new KitEffectsGUI(plugin, kit).open(player);
            }
            case 22 -> { // rounds
                player.closeInventory();
                new KitRoundGUI(plugin, kit).open(player);
            }
            case 28 -> { // boxing mode
                kit.setBoxingMode(!kit.isBoxingMode());
                player.sendMessage("§eBoxing mode: " + (kit.isBoxingMode() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 29 -> { // sumo mode
                kit.setSumoMode(!kit.isSumoMode());
                player.sendMessage("§eSumo mode: " + (kit.isSumoMode() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 30 -> { // spleef mode
                kit.setSpleefMode(!kit.isSpleefMode());
                player.sendMessage("§eSpleef mode: " + (kit.isSpleefMode() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 31 -> { // KB sync
                kit.setKbSync(!kit.isKbSync());
                player.sendMessage("§eKB Sync: " + (kit.isKbSync() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 37 -> { // health indicators
                kit.setHealthIndicators(!kit.isHealthIndicators());
                player.sendMessage("§eHealth indicators: " + (kit.isHealthIndicators() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 38 -> { // item drops
                kit.setItemDropAllowed(!kit.isItemDropAllowed());
                player.sendMessage("§eItem drops: " + (kit.isItemDropAllowed() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 39 -> { // item pickup
                kit.setItemPickupAllowed(!kit.isItemPickupAllowed());
                player.sendMessage("§eItem pickup: " + (kit.isItemPickupAllowed() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 40 -> { // virtual editor allowed
                kit.setVirtualEditorAllowed(!kit.isVirtualEditorAllowed());
                player.sendMessage("§eVirtual editor: " + (kit.isVirtualEditorAllowed() ? "§aenabled" : "§cdisabled"));
                gui.open(player);
            }
            case 45 -> { // back to kit list
                player.closeInventory();
                new KitListGUI(plugin).open(player);
            }
            case 53 -> { // close
                player.closeInventory();
            }
            default -> {
            }
        }
    }

    // ================= Rule Editor =================
    private void handleRuleEditorClick(Player player, int slot, ItemStack clicked, ClickType click) {
        Kit kit = getKitFromEditorTitle(player.getOpenInventory().getTitle().replace("Rules:", "Edit Kit:"));
        if (kit == null) {
            player.closeInventory();
            return;
        }
        var rules = kit.getRuleSet();

        switch (slot) {
            case 10 -> rules.setHunger(!rules.isHunger());
            case 11 -> rules.setDamageEnabled(!rules.isDamageEnabled());
            case 12 -> rules.setFallDamage(!rules.isFallDamage());
            case 13 -> rules.setRegen(!rules.isRegen());
            case 14 -> rules.setCrafting(!rules.isCrafting());
            case 19 -> rules.setBuild(!rules.isBuild());
            case 20 -> rules.setBreakBlocks(!rules.isBreakBlocks());
            case 21 -> rules.setOnlyBreakPlayerPlaced(!rules.isOnlyBreakPlayerPlaced());
            case 28 -> rules.setDropsAllowed(!rules.isDropsAllowed());
            case 29 -> rules.setPickupAllowed(!rules.isPickupAllowed());
            case 37 -> rules.setHealthIndicators(!rules.isHealthIndicators());
            case 38 -> rules.setKbSync(!rules.isKbSync());
            case 40 -> {
                int delta = (click.isLeftClick() ? 1 : -1) * (click.isShiftClick() ? 5 : 1);
                int newSeconds = Math.max(0, rules.getStartTimerSeconds() + delta);
                rules.setStartTimerSeconds(newSeconds);
            }
            case 44 -> { // back
                new KitEditorGUI(plugin, kit).open(player);
                return;
            }
            case 49 -> { // save
                plugin.getKitManager().saveKit(kit);
                player.sendMessage("§aKit rules saved for §e" + kit.getName());
                new KitEditorGUI(plugin, kit).open(player);
                return;
            }
            default -> {
            }
        }
        new KitRuleEditorGUI(plugin, kit).open(player);
    }

    // ================= Effects =================
    private void handleEffectsClick(Player player, int slot, ItemStack clicked, ClickType click, String title) {
        Kit kit = getKitFromEditorTitle(title.replace("Effects:", "Edit Kit:"));
        if (kit == null) {
            player.closeInventory();
            return;
        }
        KitEffectsGUI gui = new KitEffectsGUI(plugin, kit);

        if (slot == 45 && clicked.getType() == Material.EMERALD) {
            // open selector
            gui.openSelector(player);
        } else if (slot == 48 && clicked.getType() == Material.ARROW) {
            player.sendMessage("§7Previous page (not implemented)");
        } else if (slot == 50 && clicked.getType() == Material.ARROW) {
            player.sendMessage("§7Next page (not implemented)");
        } else if (slot == 49 && clicked.getType() == Material.ARROW) {
            new KitEditorGUI(plugin, kit).open(player);
            return;
        } else if (slot == 53 && clicked.getType() == Material.BARRIER) {
            gui.clearEffects();
            player.sendMessage("§cCleared all effects for kit §e" + kit.getName());
        }

        gui.open(player);
    }

    // ================= Rounds =================
    private void handleRoundsClick(Player player, int slot, ItemStack clicked, ClickType click) {
        Kit kit = getKitFromEditorTitle(player.getOpenInventory().getTitle().replace("Rounds:", "Edit Kit:"));
        if (kit == null) {
            player.closeInventory();
            return;
        }
        KitRoundGUI gui = new KitRoundGUI(plugin, kit);

        switch (slot) {
            case 10 -> adjustRounds(gui::adjustUnrankedRounds, click);
            case 12 -> adjustRounds(gui::adjustRankedRounds, click);
            case 14 -> gui.toggleUnrankedQueue();
            case 16 -> gui.toggleRankedQueue();
            case 22 -> new KitEditorGUI(plugin, kit).open(player);
            case 26 -> {
                plugin.getKitManager().saveKit(kit);
                player.sendMessage("§aKit round settings saved for §e" + kit.getName());
                new KitEditorGUI(plugin, kit).open(player);
                return;
            }
            default -> {
            }
        }
        gui.open(player);
    }

    private void adjustRounds(java.util.function.IntConsumer adjuster, ClickType click) {
        int delta = (click.isLeftClick() ? 1 : -1) * (click.isShiftClick() ? 5 : 1);
        adjuster.accept(delta);
    }

    // ================= Attributes =================
    private void handleAttributesClick(Player player, int slot, ItemStack clicked, ClickType click) {
        Kit kit = getKitFromEditorTitle(player.getOpenInventory().getTitle().replace("Attributes:", "Edit Kit:"));
        if (kit == null) {
            player.closeInventory();
            return;
        }
        KitAttributeGUI gui = new KitAttributeGUI(plugin, kit);
        var attrs = kit.getAttributeSet();

        double delta = (click.isLeftClick() ? 1 : -1) * (click.isShiftClick() ? 5 : 1);

        switch (slot) {
            case 10 -> attrs.setMaxHealth(Math.max(1.0, Math.min(1024.0, attrs.getMaxHealth() + delta)));
            case 11 -> attrs.setMovementSpeed(Math.max(0.0, Math.min(1.0, attrs.getMovementSpeed() + delta * 0.01)));
            case 12 -> attrs.setAttackDamage(Math.max(0.0, Math.min(100.0, attrs.getAttackDamage() + delta * 0.5)));
            case 13 -> attrs.setKnockbackResistance(Math.max(0.0, Math.min(1.0, attrs.getKnockbackResistance() + delta * 0.1)));
            case 14 -> attrs.setScale((float) Math.max(0.0625, Math.min(16.0, attrs.getScale() + delta * 0.125)));
            case 19 -> applyPreset(kit, "vanilla");
            case 20 -> applyPreset(kit, "tank");
            case 21 -> applyPreset(kit, "speed");
            case 22 -> applyPreset(kit, "glass cannon");
            case 26 -> new KitEditorGUI(plugin, kit).open(player);
            case 31 -> {
                plugin.getKitManager().saveKit(kit);
                player.sendMessage("§aKit attributes saved for §e" + kit.getName());
                new KitEditorGUI(plugin, kit).open(player);
                return;
            }
            default -> {
            }
        }

        gui.open(player);
    }

    private void applyPreset(Kit kit, String preset) {
        new KitAttributeGUI(plugin, kit).applyPreset(preset);
    }
}
