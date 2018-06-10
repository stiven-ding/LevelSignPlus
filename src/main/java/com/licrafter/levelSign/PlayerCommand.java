package com.licrafter.levelSign;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * Created by lijx on 16/5/19.
 */
public class PlayerCommand implements CommandExecutor {

    private SignExtend plugin;

    public PlayerCommand(SignExtend plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equals("reload") && sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "��a ʱ��ȼ� ��b��l>> ��f" + ChatColor.AQUA + "reloading config....");
            plugin.reload();
            sender.sendMessage(ChatColor.RED + "��a ʱ��ȼ� ��b��l>> ��f" + ChatColor.AQUA + "config reloaded!");
            return true;
        }
        if (args.length == 1 && args[0].equals("list")) {

            String message = "";
            Iterator<String> levels = plugin.getConfig().getConfigurationSection("setting.levels").getKeys(false).iterator();
            while (levels.hasNext()) {
                message += plugin.getLevelNick(levels.next());
                if (levels.hasNext()) {
                    message += "&7->";
                }
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        if (args.length == 2 && args[0].equals("move") && sender.isOp()) {
            Player player = (Player) sender;
            plugin.setPlayerLevel(player.getUniqueId(), args[1]);
            return true;
        }

        if (args.length == 3 && args[0].equals("add") && sender.isOp()) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "��a ʱ��ȼ� ��b��l>> ��f��Ҳ����ڻ��߲�����!");
                return true;
            }
            try {
                plugin.setPlayerPoint(player.getUniqueId(), Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "��a ʱ��ȼ� ��b��l>> ��f�������Ϊһ������0������!");
                return true;
            }
            sender.sendMessage(ChatColor.AQUA + "��a ʱ��ȼ� ��b��l>> ��fΪ���" + args[1] + "�����˾���:" + args[2]);
            return true;
        }

        if (args.length == 1 && args[0].equals("me") && sender instanceof Player) {
            Player player = (Player) sender;
            List<String> messages = plugin.getConfig().getStringList("setting.message");
            for (String msg : messages) {
                msg = replace(player, msg);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
            return true;
        }

        if (args.length==1&&sender.isOp()){
            Player player = Bukkit.getPlayer(args[0]);
            if (player==null){
                sender.sendMessage(ChatColor.RED + "��a ʱ��ȼ� ��b��l>> ��f��Ҳ����ڻ��߲�����!");
                return true;
            }
            List<String> messages = plugin.getConfig().getStringList("setting.message");
            for (String msg : messages) {
                msg = replace(player, msg);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("setting.title")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/level list  &7- �鿴�ȼ�˳��"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/level me  &7- �鿴�Լ��ĵȼ���Ϣ"));
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/level add [player] [����]  &7- Ϊ������Ӿ������(����Ϊ������)"));
        }
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/level [player]  &7- �鿴ĳ����ҵĵȼ���Ϣ"));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/level reload  &7-  ���������ļ�"));
        return true;
    }

    public String replace(Player player, String msg) {
        UUID uuid = player.getUniqueId();
        msg = msg.replaceAll("%player%", player.getName())
                 .replaceAll("%level%", plugin.getPlayerLevelNick(uuid))
                 .replaceAll("%point%", plugin.getPlayerPoint(uuid))
                 .replaceAll("%maxPoint%", String.valueOf(plugin.getPlayerMaxPoint(uuid)))
                 .replaceAll("%maxHealth%", String.valueOf(plugin.getPlayerMaxHealth(uuid)))
                 .replaceAll("%resCount%", String.valueOf(plugin.getPlayerMaxResCount(uuid)))
                 .replaceAll("%resSize%", String.valueOf(plugin.getPlayerMaxResSize(uuid)))
                 .replaceAll("%killMob%", String.valueOf(plugin.getKillMob(player.getName())))
                 .replaceAll("%attack%", String.valueOf(plugin.getAttackPlus(player.getUniqueId())));
        return msg;
    }

}
