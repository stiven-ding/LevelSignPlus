package com.licrafter.levelSign;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

/**
 * Created by lijx on 16/7/28.
 */
public class LevelUpExecutor {

    private SignExtend plugin;
    private List<String> permissions;
    private List<String> cmds;

    public LevelUpExecutor(SignExtend plugin) {
        this.plugin = plugin;
    }

    public void levelUp(UUID uuid, String levelTo) {
        permissions = plugin.getConfig().getStringList(new StringBuilder("setting.levels.").append(levelTo).append(".permissions").toString());
        cmds = plugin.getConfig().getStringList(new StringBuilder("setting.levels.").append(levelTo).append(".cmds").toString());
        String player = Bukkit.getPlayer(uuid).getName();
        for (String cmd : cmds) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", player));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%level%", plugin.getPlayerLevelNick(uuid)));
        }

        for (String perm : permissions) {
            String[] args = perm.split("|");
            if (args.length == 1) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manuaddp ".concat(player).concat(" ").concat(args[0]));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manselect ".concat(args[i]));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manuaddp ".concat(player).concat(" ").concat(args[0]));
            }
        }
    }
}
