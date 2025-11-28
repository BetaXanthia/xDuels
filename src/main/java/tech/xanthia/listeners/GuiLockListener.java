package tech.xanthia.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Prevents players from grabbing items out of locked GUIs.
 *
 * This listener is intentionally conservative:
 * - Only cancels clicks/drags when the *top* inventory title starts with the locked prefix
 * - Allows other plugin GUIs (like arena editors/selectors) to handle their own clicks
 */
public class GuiLockListener implements Listener {

    private static final String LOCKED_PREFIX = "§6§l";

    private boolean isLockedTopInventory(InventoryView view) {
        if (view == null) {
            return false;
        }

        String title = view.getTitle();
        return title != null && title.startsWith(LOCKED_PREFIX);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        // Only cancel if this is explicitly a locked GUI; allow other GUIs to process the click
        if (isLockedTopInventory(event.getView())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isLockedTopInventory(event.getView())) {
            event.setCancelled(true);
        }
    }
}