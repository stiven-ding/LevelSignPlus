package com.licrafter.levelSign.lib;

/**
 * Created by lijx on 16/6/2.
 */
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LevelSendEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Location location;
    private String[] lines;
    private boolean modified = false;
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    LevelSendEvent(Player player, Location location, String[] lines) {
        this.player = player;
        this.location = location;
        this.lines = lines;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {
        return this.lines[index];
    }

    String[] getLines() {
        return this.lines;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        if(line == null) {
            line = "";
        }

        if(!this.modified) {
            this.modified = true;
            this.lines = new String[]{this.lines[0], this.lines[1], this.lines[2], this.lines[3]};
        }

        this.lines[index] = line;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
