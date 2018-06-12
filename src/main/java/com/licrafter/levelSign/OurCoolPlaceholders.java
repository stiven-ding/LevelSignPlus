/*    */ package com.licrafter.levelSign;
/*    */ 
/*    */ import me.clip.placeholderapi.external.EZPlaceholderHook;
/*    */ import org.bukkit.entity.Player;
/*    */ 
/*    */ public class OurCoolPlaceholders
/*    */   extends EZPlaceholderHook
/*    */ {
/*    */   private SignExtend plugin;
/*    */   
/*    */   public OurCoolPlaceholders(SignExtend plugin)
/*    */   {
/* 12 */     super(plugin, "timeslevel");
/* 13 */     this.plugin = plugin;
/*    */   }
/*    */   
/*    */   public String onPlaceholderRequest(Player p, String identifier)
/*    */   {
/* 19 */     if (identifier.equals("level")) {
/* 20 */       return this.plugin.getPlayerLevelNick(p.getUniqueId());
/*    */     }
/* 22 */     if (p == null) {
/* 23 */       return "";
/*    */     }
/* 25 */     return null;
/*    */   }
/*    */ }


/* Location:           C:\Users\14488\Desktop\LevelSignPlus.jar
 * Qualified Name:     com.licrafter.levelSign.OurCoolPlaceholders
 * JD-Core Version:    0.7.0.1
 */