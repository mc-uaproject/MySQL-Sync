package hd.sphinx.sync.mysql;

import hd.sphinx.sync.Main;
import hd.sphinx.sync.MainManageData;
import hd.sphinx.sync.api.SyncProfile;
import hd.sphinx.sync.backup.CustomSyncSettings;
import hd.sphinx.sync.util.*;
import org.bukkit.GameMode;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManageMySQLData {
    public static Boolean isPlayerInDB(Player player) {
        return isPlayerInDB(player.getUniqueId());
    }

    public static Boolean isPlayerInDB(UUID playerId) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            PreparedStatement tryPreparedStatement = MySQL.getConnection().prepareStatement("SELECT p.last_joined FROM playerdata as p WHERE p.player_uuid = ?");
            tryPreparedStatement.setString(1, String.valueOf(playerId));
            ResultSet rs = tryPreparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException ignored) {
            return false;
        }
    }

    public static void generatePlayer(Player player) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        if (isPlayerInDB(player)) return;
        try {
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("INSERT INTO playerdata (player_uuid, player_name, last_joined) VALUES(?,?,?)");
            preparedStatement.setString(1, String.valueOf(player.getUniqueId()));
            preparedStatement.setString(2, player.getName());
            java.util.Date dateNow = new Date( );
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat ("MM.dd.yyyy 'at' HH:mm:ss z");
            preparedStatement.setString(3, simpleDateFormat.format(dateNow));
            preparedStatement.executeUpdate();
            Main.schedulerManager.getScheduler().scheduleExecuteCommands(player);
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
                Main.schedulerManager.getScheduler().scheduleMySQLGeneratePlayer(player);
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with registering a Player!");
                if (ConfigManager.getBoolean("settings.sending.error")) {
                    player.sendMessage(ConfigManager.getColoredString("messages.error"));
                }
            }
        }
    }

    public static void generatePlayer(UUID playerId, String playerName) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        if (isPlayerInDB(playerId)) return;
        try {
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("INSERT INTO playerdata (player_uuid, player_name, last_joined) VALUES(?,?,?)");
            preparedStatement.setString(1, String.valueOf(playerId));
            preparedStatement.setString(2, playerName);
            java.util.Date dateNow = new Date( );
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat ("MM.dd.yyyy 'at' HH:mm:ss z");
            preparedStatement.setString(3, simpleDateFormat.format(dateNow));
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with registering a Player!");
            }
        }
    }

    public static void loadInventory(UUID playerId, PlayerInventory inventory) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM playerdata as p WHERE p.player_uuid = ?");
            preparedStatement.setString(1, String.valueOf(playerId));
            ResultSet resultSet = preparedStatement.executeQuery();
            String result;
            if (resultSet.next()) {
                result = resultSet.getString("inventory");
                try {
                    if (result != null) {
                        InventoryManager.loadItem(result, inventory);
                        result = null;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with loading player inventory!");
            }
        }
    }

    public static void loadEnderChest(UUID playerId, Inventory enderChest) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM playerdata as p WHERE p.player_uuid = ?");
            preparedStatement.setString(1, String.valueOf(playerId));
            ResultSet resultSet = preparedStatement.executeQuery();
            String result;
            if (resultSet.next()) {
                result = resultSet.getString("enderchest");
                try {
                    if (result != null) {
                        InventoryManager.loadEChest(result, enderChest);
                        result = null;
                    }
                } catch (Exception ignored) { }
            }
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with loading player ender chest!");
            }
        }
    }

    public static void loadPlayer(Player player) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            SyncProfile syncProfile = new SyncProfile(player);
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM playerdata as p WHERE p.player_uuid = ?");
            preparedStatement.setString(1, String.valueOf(player.getUniqueId()));
            ResultSet resultSet = preparedStatement.executeQuery();
            String result;
            if (resultSet.next()) {
                result = resultSet.getString("inventory");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.inventory")) {
                        InventoryManager.loadItem(result, player.getInventory());
                        syncProfile.setPlayerInventory(player.getInventory());
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("gamemode");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.gamemode")) {
                        player.setGameMode(GameMode.valueOf(result));
                        syncProfile.setGameMode(GameMode.valueOf(result));
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("attributes");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.attributes")) {
                        AttributeManager.loadAttributes(player, result);
                        syncProfile.setAttributesData(result);
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("health");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.health")) {
                        player.setHealth(Double.parseDouble(result));
                        syncProfile.setHealth(Double.parseDouble(result));
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("food");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.hunger")) {
                        player.setFoodLevel(Integer.parseInt(result));
                        syncProfile.setHunger(Integer.parseInt(result));
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("exp");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.exp")) {
                        player.setLevel(Integer.parseInt(result));
                        syncProfile.setExp(Integer.parseInt(result));
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("enderchest");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.enderchest")) {
                        InventoryManager.loadEChest(result, player.getEnderChest());
                        syncProfile.setEnderChest(player.getEnderChest());
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("effects");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.effects")) {
                        Collection<PotionEffect> collection = null;
                        collection = Arrays.asList(BukkitSerialization.potionEffectArrayFromBase64(result));
                        player.addPotionEffects(collection);
                        syncProfile.setPotionEffects(collection);
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("advancements");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.advancements")) {
                        syncProfile.setAdvancements(AdvancementManager.loadPlayerAdvancements(player, result));
                        result = null;
                    }
                } catch (Exception ignored) { }
                result = resultSet.getString("statistics");
                try {
                    if (result != null && ConfigManager.getBoolean("settings.syncing.statistics")) {
                        syncProfile.setRawStatistics(StatisticsManager.loadPlayerStatistics(player, result));
                    }
                } catch (Exception ignored) { }
                player.sendMessage(ConfigManager.getColoredString("messages.loaded"));
            }
            Main.schedulerManager.getScheduler().scheduleExecuteCommands(player);
            Main.schedulerManager.getScheduler().scheduleCompleteLoadEvent(player, syncProfile);
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with loading a Player!");
                if (ConfigManager.getBoolean("settings.sending.error")) {
                    player.sendMessage(ConfigManager.getColoredString("messages.error"));
                }
            }
        }
    }

    public static void saveInventory(UUID playerId, String invBase64) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            String statement = "UPDATE playerdata AS p SET p.inventory = ? WHERE p.player_uuid = ?";
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement(statement);
            preparedStatement.setString(1, invBase64);
            preparedStatement.setString(2, String.valueOf(playerId));
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with saving player inventory!");
            }
        }
    }

    public static void saveEnderChest(UUID playerId, String ecBase64) {
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            String statement = "UPDATE playerdata AS p SET p.enderchest = ? WHERE p.player_uuid = ?";
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement(statement);
            preparedStatement.setString(1, ecBase64);
            preparedStatement.setString(2, String.valueOf(playerId));
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with saving player ender chest!");
            }
        }
    }

    public static void savePlayer(Player player, String invBase64, String ecBase64) {
        if (MainManageData.loadedPlayerData.contains(player)) return;
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            String statement = "UPDATE playerdata AS p SET p.player_name = ?, p.last_joined = ?";
            if (ConfigManager.getBoolean("settings.syncing.inventory")) {
                statement = statement + ", p.inventory = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.gamemode")) {
                statement = statement + ", p.gamemode = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.health")) {
                statement = statement + ", p.health = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.hunger")) {
                statement = statement + ", p.food = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.enderchest")) {
                statement = statement + ", p.enderchest = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.exp")) {
                statement = statement + ", p.exp = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.effects")) {
                statement = statement + ", p.effects = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.attributes")) {
                statement = statement + ", p.attributes = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.advancements")) {
                statement = statement + ", p.advancements = ?";
            }
            if (ConfigManager.getBoolean("settings.syncing.statistics")) {
                statement = statement + ", p.statistics = ?";
            }
            statement = statement + " WHERE p.player_uuid = ?";
            SyncProfile syncProfile = new SyncProfile(player);
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement(statement);
            preparedStatement.setString(1, player.getName());
            Date dateNow = new Date( );
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat ("MM.dd.yyyy 'at' HH:mm:ss z");
            preparedStatement.setString(2, simpleDateFormat.format(dateNow));

            String[] arguments = statement.split(",");

            int real = 1;
            for (String string : arguments) {
                if (string.contains("inventory")) {
                    preparedStatement.setString(real, invBase64);
                    syncProfile.setPlayerInventory(player.getInventory());
                } else if (string.contains("gamemode")) {
                    preparedStatement.setString(real, String.valueOf(player.getGameMode()));
                    syncProfile.setGameMode(player.getGameMode());
                } else if (string.contains("health")) {
                    preparedStatement.setInt(real, (int) player.getHealth());
                    syncProfile.setHealth(player.getHealth());
                } else if (string.contains("food")) {
                    preparedStatement.setInt(real, player.getFoodLevel());
                    syncProfile.setHunger(player.getFoodLevel());
                } else if (string.contains("enderchest")) {
                    preparedStatement.setString(real, ecBase64);
                    syncProfile.setEnderChest(player.getEnderChest());
                } else if (string.contains("exp")) {
                    preparedStatement.setInt(real, player.getLevel());
                    syncProfile.setExp(player.getLevel());
                } else if (string.contains("effects")) {
                    Collection<PotionEffect> effectCollection = player.getActivePotionEffects();
                    PotionEffect[] effectArray = new ArrayList<PotionEffect>(effectCollection).toArray(new PotionEffect[0]);
                    preparedStatement.setString(real, BukkitSerialization.potionEffectArrayToBase64(effectArray));
                    syncProfile.setPotionEffects(effectCollection);
                } else if (string.contains("attributes")) {
                    String attributesData = AttributeManager.saveAttributes(player);
                    preparedStatement.setString(real, attributesData);
                    syncProfile.setAttributesData(attributesData);
                } else if (string.contains("advancements")) {
                    HashMap<Advancement, Boolean> advancementMap = AdvancementManager.getAdvancementMap(player);
                    preparedStatement.setString(real, BukkitSerialization.advancementBooleanHashMapToBase64(advancementMap));
                    syncProfile.setAdvancements(advancementMap);
                } else if (string.contains("statistics")) {
                    HashMap<String, Integer> statisticsMap = StatisticsManager.getStatisticsMap(player);
                    preparedStatement.setString(real, BukkitSerialization.statisticsIntegerHashMapToBase64(statisticsMap));
                    syncProfile.setRawStatistics(statisticsMap);
                }
                real++;
            }
            preparedStatement.setString(real, String.valueOf(player.getUniqueId()));
            preparedStatement.executeUpdate();

            Main.schedulerManager.getScheduler().scheduleSavingDataEvent(player, syncProfile);
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
                Main.schedulerManager.getScheduler().scheduleMySQLSavePlayer(player, invBase64, ecBase64);
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with saving a Player!");
                if (ConfigManager.getBoolean("settings.sending.error")) {
                    player.sendMessage(ConfigManager.getColoredString("messages.error"));
                }
            }
        }
    }

    public static void savePlayer(Player player, CustomSyncSettings customSyncSettings) {
        if (MainManageData.loadedPlayerData.contains(player)) return;
        if (!MySQL.isConnected()) {
            MySQL.connectMySQL();
        }
        try {
            String statement = "UPDATE playerdata AS p SET p.player_name = ?, p.last_joined = ?";
            if (customSyncSettings.isSyncingInventory()) {
                statement = statement + ", p.inventory = ?";
            }
            if (customSyncSettings.isSyncingGamemode()) {
                statement = statement + ", p.gamemode = ?";
            }
            if (customSyncSettings.isSyncingHealth()) {
                statement = statement + ", p.health = ?";
            }
            if (customSyncSettings.isSyncingHunger()) {
                statement = statement + ", p.food = ?";
            }
            if (customSyncSettings.isSyncingEnderchest()) {
                statement = statement + ", p.enderchest = ?";
            }
            if (customSyncSettings.isSyncingExp()) {
                statement = statement + ", p.exp = ?";
            }
            if (customSyncSettings.isSyncingEffects()) {
                statement = statement + ", p.effects = ?";
            }
            if (customSyncSettings.isSyncingAttributes()) {
                statement = statement + ", p.attributes = ?";
            }
            if (customSyncSettings.isSyncingAdvancements()) {
                statement = statement + ", p.advancements = ?";
            }
            if (customSyncSettings.isSyncingStatistics()) {
                statement = statement + ", p.statistics = ?";
            }
            statement = statement + " WHERE p.player_uuid = ?";
            SyncProfile syncProfile = new SyncProfile(player);
            PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement(statement);
            preparedStatement.setString(1, player.getName());
            Date dateNow = new Date( );
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat ("MM.dd.yyyy 'at' HH:mm:ss z");
            preparedStatement.setString(2, simpleDateFormat.format(dateNow));

            String[] arguments = statement.split(",");

            int real = 1;
            for (String string : arguments) {
                if (string.contains("inventory")) {
                    preparedStatement.setString(real, InventoryManager.saveItems(player.getInventory()));
                    syncProfile.setPlayerInventory(player.getInventory());
                } else if (string.contains("gamemode")) {
                    preparedStatement.setString(real, String.valueOf(player.getGameMode()));
                    syncProfile.setGameMode(player.getGameMode());
                } else if (string.contains("health")) {
                    preparedStatement.setInt(real, (int) player.getHealth());
                    syncProfile.setHealth(player.getHealth());
                } else if (string.contains("food")) {
                    preparedStatement.setInt(real, player.getFoodLevel());
                    syncProfile.setHunger(player.getFoodLevel());
                } else if (string.contains("enderchest")) {
                    preparedStatement.setString(real, InventoryManager.saveEChest(player.getEnderChest()));
                    syncProfile.setEnderChest(player.getEnderChest());
                } else if (string.contains("exp")) {
                    preparedStatement.setInt(real, player.getLevel());
                    syncProfile.setExp(player.getLevel());
                } else if (string.contains("effects")) {
                    Collection<PotionEffect> effectCollection = player.getActivePotionEffects();
                    PotionEffect[] effectArray = new ArrayList<PotionEffect>(effectCollection).toArray(new PotionEffect[0]);
                    preparedStatement.setString(real, BukkitSerialization.potionEffectArrayToBase64(effectArray));
                    syncProfile.setPotionEffects(effectCollection);
                } else if (string.contains("attributes")) {
                    String attributesData = AttributeManager.saveAttributes(player);
                    preparedStatement.setString(real, attributesData);
                    syncProfile.setAttributesData(attributesData);
                } else if (string.contains("advancements")) {
                    HashMap<Advancement, Boolean> advancementMap = AdvancementManager.getAdvancementMap(player);
                    preparedStatement.setString(real, BukkitSerialization.advancementBooleanHashMapToBase64(advancementMap));
                    syncProfile.setAdvancements(advancementMap);
                } else if (string.contains("statistics")) {
                    HashMap<String, Integer> statisticsMap = StatisticsManager.getStatisticsMap(player);
                    preparedStatement.setString(real, BukkitSerialization.statisticsIntegerHashMapToBase64(statisticsMap));
                    syncProfile.setRawStatistics(statisticsMap);
                }
                real++;
            }
            preparedStatement.setString(real, String.valueOf(player.getUniqueId()));
            preparedStatement.executeUpdate();

            Main.schedulerManager.getScheduler().scheduleSavingDataEvent(player, syncProfile);
        } catch (SQLException exception) {
            if (!MySQL.isConnected()) {
                MySQL.connectMySQL();
                Main.schedulerManager.getScheduler().scheduleMySQLSavePlayer(player, customSyncSettings);
            } else {
                exception.printStackTrace();
                Main.logger.warning("Something went wrong with saving a Player!");
                if (ConfigManager.getBoolean("settings.sending.error")) {
                    player.sendMessage(ConfigManager.getColoredString("messages.error"));
                }
            }
        }
    }
}
