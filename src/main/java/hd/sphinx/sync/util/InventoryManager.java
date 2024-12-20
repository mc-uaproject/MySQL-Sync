package hd.sphinx.sync.util;

import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiConsumer;

public class InventoryManager {

    private static final int INVENTORY_SIZE = 41;

    public static String saveItems(@NotNull PlayerInventory playerInventory) {
        ItemStack[] items = new ItemStack[INVENTORY_SIZE];

        for (int i = 0; i < items.length; i++) {
            ItemStack item = playerInventory.getItem(i);

            if (ExclusionManager.isNotExcluded(item)) {
                items[i] = playerInventory.getItem(i);
            }
        }

        return BukkitSerialization.itemStackArrayToBase64(items);
    }

    public static void loadItem(@NotNull String base64, @NotNull PlayerInventory inventory) {
        ItemStack[] items = new ItemStack[0];
        try {
            items = BukkitSerialization.itemStackArrayFromBase64(base64);
            if (items == null) return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        BiConsumer<Integer, ItemStack> setItem = getSetItem(inventory);
        for (int i = 0; i <= 40; i++) {
            if (items[i] == null || ExclusionManager.isExcluded(items[i])) {
                setItem.accept(i, null);
            } else {
                setItem.accept(i, items[i]);
            }
        }
    }

    public static String saveEChest(@NotNull Inventory enderChest) {
        ItemStack[] items = new ItemStack[27];
        int i = 0;
        while (i <= 26) {
            items[i] = enderChest.getItem(i);
            i++;
        }
        return BukkitSerialization.itemStackArrayToBase64(items);
    }

    public static void loadEChest(@NotNull String base64, @NotNull Inventory enderChest) {
        ItemStack[] items = new ItemStack[0];
        try {
            items = BukkitSerialization.itemStackArrayFromBase64(base64);
            if (items == null) return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        BiConsumer<Integer, ItemStack> setItem = getSetItem(enderChest);
        int i = 0;
        while (i <= 26) {
            setItem.accept(i, items[i]);
            i++;
        }
    }

    private static BiConsumer<Integer, ItemStack> getSetItem(Inventory inventory) {
        if (inventory instanceof CraftInventoryPlayer craftInventoryPlayer) {
            return (i, item) -> craftInventoryPlayer.getInventory().setItem(i, CraftItemStack.asNMSCopy(item));
        }
        return inventory::setItem;
    }
}
