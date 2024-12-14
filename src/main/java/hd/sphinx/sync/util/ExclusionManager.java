package hd.sphinx.sync.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExclusionManager {

    private static Plugin circleOfImagination;
    private static boolean isCircleOfImaginationLoaded;

    public static boolean isNotExcluded(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!checkForCircleOfImagination()) return true;

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        return !hasAnyNamespacedKey(dataContainer);
    }

    public static boolean checkForCircleOfImagination() {
        @Nullable Plugin coi = Bukkit.getPluginManager().getPlugin("CircleOfImagination");
        if (coi != null) {
            isCircleOfImaginationLoaded = true;
            circleOfImagination = coi;
        }
        return isCircleOfImaginationLoaded;
    }

    private static boolean hasAnyNamespacedKey(PersistentDataContainer dataContainer) {
        List<String> keys = List.of("shortcut", "abilityId");
        boolean found = false;
        for (String key : keys) {
            NamespacedKey keyNamespacedKey = new NamespacedKey(circleOfImagination, key);
            if (dataContainer.has(keyNamespacedKey)) {
                found = true;
                break;
            }
        }

        return found;
    }

}
