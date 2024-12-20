package hd.sphinx.sync.listener;

import hd.sphinx.sync.Main;
import hd.sphinx.sync.MainManageData;
import hd.sphinx.sync.util.ConfigManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;

public class DeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (MainManageData.loadedPlayerData.contains(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (ConfigManager.getBoolean("settings.onlySyncPermission") && !event.getEntity().hasPermission("sync.sync")) return;
        addDeadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (ConfigManager.getBoolean("settings.onlySyncPermission") && !event.getPlayer().hasPermission("sync.sync")) return;
        removeDeadPlayer(event.getPlayer());
    }

    private static NamespacedKey getDeadKey() {
        return new NamespacedKey(Main.main, "dead");
    }

    public static void addDeadPlayer(Player player) {
        player.getPersistentDataContainer().set(getDeadKey(), PersistentDataType.BOOLEAN, true);
    }

    public static void removeDeadPlayer(Player player) {
        player.getPersistentDataContainer().remove(getDeadKey());
    }

    public static boolean isDead(Player player) {
        return player.getPersistentDataContainer().has(getDeadKey());
    }
}
