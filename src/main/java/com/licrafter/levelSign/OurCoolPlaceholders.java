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

 import me.clip.placeholderapi.external.EZPlaceholderHook;
 import org.bukkit.entity.Player;
 
 public class OurCoolPlaceholders
   extends EZPlaceholderHook
 {
   private SignExtend plugin;
   
   public OurCoolPlaceholders(SignExtend plugin)
   {
     super(plugin, "timeslevel");
     this.plugin = plugin;
   }
   
   public String onPlaceholderRequest(Player p, String identifier)
   {
     if (identifier.equals("level")) {
       return this.plugin.getPlayerLevelNick(p.getUniqueId());
     }
     if (identifier.equals("remaining")) {
       return String.valueOf(this.plugin.getPlayerRemainingPoint(p.getUniqueId()));
     }
     if (identifier.equals("point")) {
       return this.plugin.getPlayerPoint(p.getUniqueId());
     }
     if (identifier.equals("desc")) {
       return this.plugin.getPlayerLevelDesc(p.getUniqueId());
     }
     if (p == null) {
       return "";
     }
     return null;
   }
 }


/* Location:           C:\Users\14488\Desktop\LevelSignPlus.jar
 * Qualified Name:     com.licrafter.levelSign.OurCoolPlaceholders
 * JD-Core Version:    0.7.0.1
 */