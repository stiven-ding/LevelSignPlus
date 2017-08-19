package com.licrafter.levelSign.lib;

/**
 * Created by lijx on 16/6/2.
 */

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

public abstract class Holder implements Listener {
    private final String key;
    private final String permissionsNode;

    public Holder(Plugin plugin, String key, String permissionsNode) {
        if (plugin != null && plugin.isEnabled()) {
            if (key != null && permissionsNode != null) {
                if (key.length() > 15) {
                    throw new IllegalArgumentException("The key must not be longer then 15!");
                } else {
                    this.key = key;
                    this.permissionsNode = permissionsNode;
                    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
                }
            } else {
                throw new IllegalArgumentException("The key and the permissions node must not be null!");
            }
        } else {
            throw new IllegalArgumentException("The plugin must not be null and has to be enabled!");
        }
    }

    public abstract String getValue(Player var1, Location var2, String var3);

    @EventHandler(
            priority = EventPriority.NORMAL,
            ignoreCancelled = true
    )
    public void onSignSend(LevelSendEvent event) {
        for (int i = 0; i < 4; ++i) {
            String line = event.getLine(i);
            if (line.contains(this.key)) {
                event.setLine(i, line.replace(this.key, this.getValue(event.getPlayer(), event.getLocation(), line)));
            }
        }

    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onSignCreate(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        String[] var7 = lines;
        int var6 = lines.length;

        for (int var5 = 0; var5 < var6; ++var5) {
            String line = var7[var5];
            if (line.contains(this.key) && !player.isOp()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "No permission to use \'" + this.key + "\' on your sign.");
            }
        }

    }
}
