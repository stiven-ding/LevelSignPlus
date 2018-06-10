package com.licrafter.levelSign;

import java.io.File;

import java.util.UUID;

import com.licrafter.levelSign.lib.Holder;
import com.licrafter.levelSign.lib.PtlManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;

/**
 * Created by lijx on 16/5/16.
 */

public class SignExtend extends JavaPlugin {

    public static final int NO_LEVEL_UP = 0x00;
    public static final int LEVEL_UP = 0x01;
    public static final int REACH_MAX = 0x02;

    private FileConfiguration levelConfig;
    private DataConfiguration levelConfiguration;
    private FileConfiguration mobConfig;
    private DataConfiguration mobConfiguration;

    private PtlManager ptlManager;
    private LevelUpExecutor levelUpExecutor;

    private Economy econ = null;
    private Double balance = 0.0D;

    public boolean hasTagApi;
    public String maxLevel;
    public int maxLevelPoint;
    // ����
    public double price;
    // 点击一次购买数量
    public int buyCount;
    // 当玩家升级的时候是否全服广播
    public boolean broadCast;

    @Override
    public void onEnable() {
        getLogger().info("Enabling SignExtend (�汾:" + getDescription().getVersion() + " " + "����:" +
                getDescription().getAuthors().get(0) + " )" + " WebSite:" + getDescription().getWebsite());
        if (setupEconomy()) {
            getLogger().info("Plugin Vault Is Found! " + econ.getName());
        }
        levelUpExecutor = new LevelUpExecutor(this);
        ptlManager = new PtlManager(this);
        ptlManager.onEnable();
        hasTagApi = getServer().getPluginManager().getPlugin("TagAPI") != null;
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
            reloadConfig();
        }

        levelConfiguration = new DataConfiguration(this, "levels.yml");
        levelConfig = levelConfiguration.getDataConfig();
        mobConfiguration = new DataConfiguration(this, "mobs.yml");
        mobConfig = mobConfiguration.getDataConfig();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ResListener(this), this);
        getServer().getPluginCommand("level").setExecutor(new PlayerCommand(this));
        initHolder();
        init();

        new OurCoolPlaceholders(this).hook();
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void init() {
        maxLevel = getMaxLevel();
        maxLevelPoint = getMaxPoint(maxLevel);
        price = getPointPrice();
        buyCount = getConfig().getInt("setting.buy");
        broadCast = getConfig().getBoolean("setting.broadCast");
    }

    private void initHolder() {
        new Holder(this, "[NAME]", "sign.create") {
            @Override
            public String getValue(Player player, Location location, String originalLine) {
                return ChatColor.GREEN + player.getName();
            }

        };

        new Holder(this, "[LEVEL]", "sign.create") {
            @Override
            public String getValue(Player player, Location location, String originalLine) {
                return centerText(ChatColor.translateAlternateColorCodes('&',
                        "&l�ȼ�:" + "&a&l" + getPlayerLevelNick(player.getUniqueId())));
            }

        };
        new Holder(this, "[POINT]", "sign.create") {
            @Override
            public String getValue(Player player, Location location, String originalLine) {
                return centerText(ChatColor.translateAlternateColorCodes('&',
                        "&l����:" + "&a&l" + getPlayerPoint(player.getUniqueId())));
            }
        };

        new Holder(this, "[MAXPOINT]", "sign.create") {
            @Override
            public String getValue(Player player, Location location, String originalLine) {
                return centerText(ChatColor.translateAlternateColorCodes('&',
                        "&l����:" + "&a&l" + getPlayerMaxPoint(player.getUniqueId())));
            }
        };

        new Holder(this, "[ONE]", "sign.create") {

            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getString("rank.one", "����"));
            }
        };
        new Holder(this, "[TWO]", "sign.create") {
            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getString("rank.two", "����"));
            }
        };
        new Holder(this, "[THREE]", "sign.create") {
            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getString("rank.three", "����"));
            }
        };

        new Holder(this, "[COUNT1]", "sign.create") {
            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getInt("count.one") + "ֻ");
            }
        };

        new Holder(this, "[COUNT2]", "sign.create") {
            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getInt("count.two") + "ֻ");
            }
        };

        new Holder(this, "[COUNT3]", "sign.create") {
            @Override
            public String getValue(Player var1, Location var2, String var3) {
                return ChatColor.translateAlternateColorCodes('&', "&a&l" + mobConfig.getInt("count.three") + "ֻ");
            }
        };
    }

    public String centerText(String text) {
        StringBuilder sb = new StringBuilder(text);
        return sb.append("               ").toString();
    }

    // �������ӵ���
    public int addPlayerPoint(UUID uuid) {
        int newPoint = getPlayerPoint2(uuid) + buyCount;
        if (newPoint > maxLevelPoint) {
            levelConfig.set(uuid.toString() + ".point", maxLevelPoint);
            return REACH_MAX;
        }
        levelConfig.set(uuid.toString() + ".point", newPoint);
        return canLevelUp(uuid, newPoint);
    }

    // ָ��������ӵ���
    public void setPlayerPoint(UUID uuid, int point) {
        if (point < 0) {
            return;
        }
        int newPoint = Math.min(getPlayerPoint2(uuid) + point, maxLevelPoint);
        levelConfig.set(uuid.toString() + ".point", newPoint);
        int result = LEVEL_UP;
        while (result == LEVEL_UP) {
            result = canLevelUp(uuid, getPlayerPoint2(uuid));
        }
    }

    // �ж��Ƿ�ﵽ������,Ȼ������
    public int canLevelUp(UUID uuid, int point) {
        if (point > getPlayerMaxPoint(uuid)) {
            levelUp(uuid);
            return LEVEL_UP;
        } else {
            return NO_LEVEL_UP;
        }
    }

    public String getMaxLevel() {
        int level_num = getConfig().getConfigurationSection("setting.levels").getKeys(false).size() - 1;
        return "level_" + level_num;
    }

    private Double getPointPrice() {
        return getConfig().getDouble("setting.price");
    }

    public boolean withDraw(Player player) {
        balance = econ.getBalance(player);
        if (balance >= price * buyCount) {
            econ.withdrawPlayer(player, price * buyCount);
            return true;
        } else {
            return false;
        }
    }

    // ����
    public void levelUp(UUID uuid) {
        String level = getPlayerLevel(uuid);
        int level_num = Integer.valueOf(level.replace("level_", "")) + 1;
        String levelTo = new StringBuilder("level_").append(level_num).toString();
        levelConfig.set(uuid + ".level", levelTo);
        levelConfiguration.saveDataConfig();
        levelUpExecutor.levelUp(uuid, levelTo);
        if (isEnableHealth()) {
            setPlayerMaxHealth(uuid);
        }
    }

    public void setPlayerLevel(UUID uuid, String level) {
        levelConfig.set(uuid.toString().concat(".level"), level);
    }

    // ����uuid�õ�����ҵ�point
    public String getPlayerPoint(UUID uuid) {
        if (levelConfig.contains(uuid.toString())) {
            return String.valueOf(levelConfig.getInt(uuid.toString() + ".point"));
        } else {
            return "0";
        }
    }

    public int getPlayerPoint2(UUID uuid) {
        return levelConfig.getInt(uuid.toString() + ".point");
    }

    // �õ���ҵȼ�ǰ׺,���û�и�����򷵻�Ĭ�ϵȼ�ǰ׺
    public String getPlayerLevelNick(UUID uuid) {
        return getLevelNick(getPlayerLevel(uuid));
    }

    public String getLevelNick(String level) {
        return getConfig().getString("setting.levels." + level + ".nick");
    }

    // �õ�������Ѫ��
    public Double getPlayerMaxHealth(UUID uuid) {
        return getConfig().getDouble("setting.levels." + getPlayerLevel(uuid) + ".health");
    }

    // �õ��������ӵ�е�����������
    public int getPlayerMaxResCount(UUID uuid) {
        return getConfig().getInt("setting.levels." + getPlayerLevel(uuid) + ".resCount");
    }

    // �õ���������س���
    public int getPlayerMaxResSize(UUID uuid) {
        return getConfig().getInt("setting.levels." + getPlayerLevel(uuid) + ".resSize");
    }

    // �������Ѫ�����޵Ĺ����Ƿ���
    public boolean isEnableHealth() {
        return getConfig().getBoolean("setting.enable_health", true);
    }

    // ������ش�С�������Ĺ����Ƿ���
    public boolean isEnableRes() {
        return getConfig().getBoolean("setting.enable_res", true);
    }

    public void setPlayerMaxHealth(UUID uuid) {
        Player player = getServer().getPlayer(uuid);
        Double maxHealth = getPlayerMaxHealth(uuid);
        player.setMaxHealth(maxHealth);
        player.setHealthScaled(false);
        player.setHealth(maxHealth);
    }

    // �õ���ҵ�level�ȼ�,���û���򷵻�Ĭ�ϵȼ�
    public String getPlayerLevel(UUID uuid) {
        if (levelConfig.contains(uuid.toString() + ".level")) {
            return levelConfig.getString(uuid.toString() + ".level");
        } else {
            return "level_0";
        }
    }

    // �õ������Ҫ�ﵽ����������point
    public int getPlayerMaxPoint(UUID uuid) {
        return getMaxPoint(getPlayerLevel(uuid));
    }

    // ĳ���ȼ���Ҫ�ﵽ�������������
    public int getMaxPoint(String level) {
        return getConfig().getInt("setting.levels." + level + ".maxPoint");
    }

    public void reload() {
        reloadConfig();
        levelConfiguration.reloadDataConfig();
        mobConfiguration.reloadDataConfig();
        init();
    }

    @Override
    public void onDisable() {
        levelConfiguration.saveDataConfig();
        mobConfiguration.saveDataConfig();
        ptlManager.onDisable();
        ptlManager = null;
    }

    public boolean updateMobCount(String player) {
        int count = getKillAmountByPlayer(player) + 1;
        String rank = null;
        String old_one = null;
        String old_two = null;
        int old_c1 = 0;
        int old_c2 = 0;

        mobConfig.set("mobs." + player, count);
        if (count > mobConfig.getInt("count.three")) {
            rank = "three";
            if (count > mobConfig.getInt("count.two")) {
                rank = "two";
                if (count > mobConfig.getInt("count.one")) {
                    rank = "one";
                    String one = mobConfig.getString("rank.one", "");
                    if (!one.equals(player)) {
                        old_one = mobConfig.getString("rank.one", null);
                        old_two = mobConfig.getString("rank.two", null);
                        if (player.equals(old_two)) {
                            old_two = null;
                        }
                        old_c1 = mobConfig.getInt("count.one");
                        old_c2 = mobConfig.getInt("count.two");
                    }
                } else {
                    String two = mobConfig.getString("rank.two", "");
                    if (!two.equals(player)) {
                        old_two = mobConfig.getString("rank.two", null);
                        old_c2 = mobConfig.getInt("count.two");
                    }
                }
            }
        }

        if (old_two != null) {
            mobConfig.set("rank.three", old_two);
            mobConfig.set("count.three", old_c2);
        }

        if (old_one != null) {
            mobConfig.set("rank.two", old_one);
            mobConfig.set("count.two", old_c1);
        }

        if (rank != null) {
            mobConfig.set("rank." + rank, player);
            mobConfig.set("count." + rank, count);
        }
        return true;
    }

    public Double getAttackPlus(UUID uuid) {
        return getConfig().getDouble("setting.levels." + getPlayerLevel(uuid) + ".attack");
    }

    public int getKillMob(String player) {
        return mobConfig.getInt("mobs." + player);
    }

    public int getKillAmountByPlayer(String player) {
        return mobConfig.getInt("mobs." + player, 0);
    }

    public Double getProgress(UUID uuid) {
        return getConfig().getDouble("setting.levels." + getPlayerLevel(uuid) + ".attack");
    }
}
