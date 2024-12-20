package hd.sphinx.sync;

import hd.sphinx.sync.backup.BackupHandler;
import hd.sphinx.sync.backup.CustomSyncSettings;
import hd.sphinx.sync.listener.DeathListener;
import hd.sphinx.sync.mongo.ManageMongoData;
import hd.sphinx.sync.mongo.MongoDB;
import hd.sphinx.sync.mysql.ManageMySQLData;
import hd.sphinx.sync.mysql.MySQL;
import hd.sphinx.sync.util.ConfigManager;
import hd.sphinx.sync.util.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class MainManageData {

    public static StorageType storageType;

    public static ArrayList<Player> loadedPlayerData = new ArrayList<Player>();
    public static HashMap<Player, ArrayList<String>> commandHashMap = new HashMap<Player, ArrayList<String>>();

    public static void initialize() {
        try {
            storageType = StorageType.valueOf(ConfigManager.getString("settings.storageType"));
        } catch (Exception ignored) {
            Main.logger.severe("No valid StorageType is set in Config!\n Disabling Plugin!");
            Bukkit.getPluginManager().disablePlugin(Main.main);
        }

        if (storageType == StorageType.MYSQL) {
            MySQL.connectMySQL();
            try {
                MySQL.registerMySQL();
            } catch (SQLException ignored) {
                Main.logger.severe("Could not initialize Database!\n Disabling Plugin!");
                Bukkit.getPluginManager().disablePlugin(Main.main);
            }
        } else if (storageType == StorageType.MONGODB) {
            MongoDB.connectMongoDB();
        }

        BackupHandler.initialize();
    }

    public static void reload() {
        BackupHandler.shutdown();

        try {
            storageType = StorageType.valueOf(ConfigManager.getString("settings.storageType"));
        } catch (Exception exception) {
            Main.logger.severe("No valid StorageType is set in Config!\n Disabling Plugin!");
            Bukkit.getPluginManager().disablePlugin(Main.main);
        }

        if (storageType == StorageType.MYSQL) {
            if (MySQL.isConnected()) {
                MySQL.disconnectMySQL();
            } else if (MongoDB.isConnected()) {
                MongoDB.disconnectMongoDB();
            }
            MySQL.connectMySQL();
            try {
                MySQL.registerMySQL();
            } catch (SQLException ignored) {
                Main.logger.severe("Could not initialize Database!\n Disabling Plugin!");
                Bukkit.getPluginManager().disablePlugin(Main.main);
            }
        } else if (storageType == StorageType.MONGODB) {
            if (MySQL.isConnected()) {
                MySQL.disconnectMySQL();
            } else if (MongoDB.isConnected()) {
                MongoDB.disconnectMongoDB();
            }
            MongoDB.connectMongoDB();
        }

        BackupHandler.initialize();
    }

    public static void startShutdown() {
        BackupHandler.shutdown();

        Collection<Player> players = (Collection<Player>) Bukkit.getOnlinePlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.iterator().next();
            savePlayer(player);
            if (i == players.size() - 1) {
                shutdown();
            }
        }
    }

    public static void shutdown() {
        if (storageType == StorageType.MYSQL) {
            MySQL.disconnectMySQL();
        } else if (storageType == StorageType.MONGODB) {
            MongoDB.disconnectMongoDB();
        }
    }

    public static Boolean isPlayerKnown(Player player) {
        if (storageType == StorageType.MYSQL) {
            return ManageMySQLData.isPlayerInDB(player);
        } else if (storageType == StorageType.MONGODB) {
            return ManageMongoData.isPlayerInDB(player);
        }
        return false;
    }

    public static void generatePlayer(Player player) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.generatePlayer(player);
        } else if (storageType == StorageType.MONGODB) {
            ManageMongoData.generatePlayer(player);
        }
    }

    public static void generatePlayer(UUID playerId, String playerName) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.generatePlayer(playerId, playerName);
        } else {
            throw new RuntimeException("Only MySQL supported for generatePlayer(UUID, String) as for now");
        }
    }

    public static void loadInventory(UUID playerId, PlayerInventory inventory) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.loadInventory(playerId, inventory);
        } else {
            throw new RuntimeException("Only MySQL supported for loadInventory as for now");
        }
    }

    public static void saveInventory(UUID playerId, PlayerInventory inventory) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.saveInventory(playerId, InventoryManager.saveItems(inventory));
        } else {
            throw new RuntimeException("Only MySQL supported for saveInventory as for now");
        }
    }

    public static void loadEnderChest(UUID playerId, Inventory enderChest) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.loadEnderChest(playerId, enderChest);
        } else {
            throw new RuntimeException("Only MySQL supported for loadEnderChest as for now");
        }
    }

    public static void saveEnderChest(UUID playerId, Inventory enderChest) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.saveEnderChest(playerId, InventoryManager.saveEChest(enderChest));
        } else {
            throw new RuntimeException("Only MySQL supported for saveEnderChest as for now");
        }
    }

    public static void loadPlayer(Player player) {
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.loadPlayer(player);
        } else if (storageType == StorageType.MONGODB) {
            ManageMongoData.loadPlayer(player);
        }
    }

    public static void savePlayer(Player player) {
        if (DeathListener.isDead(player)) {
            player.getInventory().clear();
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                player.setHealth(maxHealth.getValue());
            } else {
                player.setHealth(20);
            }
            player.setFoodLevel(20);
            player.setLevel(0);
        }
        try {
            player.getInventory().addItem(player.getItemOnCursor());
            player.setItemOnCursor(new ItemStack(Material.AIR));
        } catch (Exception ignored) { }
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.savePlayer(player, InventoryManager.saveItems(player.getInventory()), InventoryManager.saveEChest(player.getEnderChest()));
        } else if (storageType == StorageType.MONGODB) {
            ManageMongoData.savePlayer(player, InventoryManager.saveItems(player.getInventory()), InventoryManager.saveEChest(player.getEnderChest()));
        }
    }

    public static void savePlayer(Player player, CustomSyncSettings customSyncSettings) {
        try {
            player.getInventory().addItem(player.getItemOnCursor());
            player.setItemOnCursor(new ItemStack(Material.AIR));
        } catch (Exception ignored) { }
        if (storageType == StorageType.MYSQL) {
            ManageMySQLData.savePlayer(player, customSyncSettings);
        } else if (storageType == StorageType.MONGODB) {
            ManageMongoData.savePlayer(player, customSyncSettings);
        }
    }

    public enum StorageType {

        MYSQL,
        MONGODB,
        CLOUD; // For a future Update

    }
}
