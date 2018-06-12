package com.licrafter.levelSign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

/**
 * Created by lijx on 16/5/16.
 */
public class PlayerListener implements Listener {

    private SignExtend plugin;


    public PlayerListener(SignExtend plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            Material blockType = block.getType();
            if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST || blockType == Material.SIGN) {
                Sign sign = (Sign) block.getState();
                for (int i = 0; i < 4; i++) {
                    String line = sign.getLine(i);
                    if (line.contains("§a 时光等级 §b§l>> §f")) {
                        //扣钱的功能代添加
                        boolean success = plugin.withDraw(player);
                        if (success) {
                            player.sendMessage(ChatColor.GREEN + "§a 时光等级 §b§l>> §f" + ChatColor.GRAY
                                    + "你成功购买了" + plugin.buyCount + "点数,花费" + plugin.price * plugin.buyCount + "游戏币");
                        } else {
                            player.sendMessage(ChatColor.RED + "购买失败,或许你的游戏币不够用了,快去赚钱吧!");
                            return;
                        }
                        int levelUp = plugin.addPlayerPoint(player.getUniqueId());
                        switch (levelUp) {
                            case SignExtend.LEVEL_UP:
                                player.sendMessage(ChatColor.GREEN + "§a 时光等级 §b§l>> §f" + ChatColor.GRAY + "你的爵位升级成功,继续努力哦!");
                                if (plugin.broadCast) {
                                    plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                            "&a§a 时光等级 §b§l>> §f&7恭喜&b" + player.getName() + "&7的爵位升级到" + plugin.getPlayerLevelNick(player.getUniqueId()) + "&7!"));
                                }
                                break;
                            case SignExtend.REACH_MAX:
                                player.sendMessage(ChatColor.RED + "你已经达到了最高经验值,请不要再重复购买!");
                                break;
                        }
                        break;
                    }
                }
            }
        }
    }

//    @EventHandler
//    public void onPlayerPlaceBlack(BlockPlaceEvent event) {
//        Player player = event.getPlayer();
//        if (player.getWorld().getName().equals("world") && !player.isOp()) {
//            if (!player.hasPermission("world.place")) {
//                player.sendMessage(ChatColor.RED+"主世界禁止放置方块");
//                event.setCancelled(true);
//            }
//        }
//    }
//
//    @EventHandler
//    public void onPlayerBreakBlack(BlockBreakEvent event) {
//        Player player = event.getPlayer();
//        if (player.getWorld().getName().equals("world") && !player.isOp()) {
//            if (!player.hasPermission("world.break")) {
//                player.sendMessage(ChatColor.RED+"主世界禁止破坏方块");
//                event.setCancelled(true);
//            }
//        }
//    }


    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (killer instanceof Projectile) {
            Player player = (Player) ((Projectile) killer).getShooter();
            plugin.updateMobCount(player.getName());
        }
        if (killer instanceof Player) {
            Player player = (Player) killer;
            plugin.updateMobCount(player.getName());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        //近战伤害
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double newDamage = event.getDamage() + plugin.getAttackPlus(player.getUniqueId());
            event.setDamage(newDamage);
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        setMaxHealth(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setMaxHealth(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        setMaxHealth(event.getPlayer());
    }
/*
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        String format = event.getFormat();
        event.setFormat(ChatColor.translateAlternateColorCodes('&', "&l" + plugin.getPlayerLevelNick(p.getUniqueId())));
        String pformat = event.getFormat();
        event.setFormat("[" + pformat + ChatColor.WHITE + "]" + format);
        event.setFormat(event.getFormat());
    }
    */
    public void setMaxHealth(Player player) {
        if (!plugin.isEnableHealth()) {
            return;
        }
        Double maxHealth = plugin.getPlayerMaxHealth(player.getUniqueId());
        player.setHealthScale(maxHealth);
        player.setMaxHealth(maxHealth);
        player.setHealthScaled(true);
    }

}
