package com.licrafter.levelSign.lib;

/**
 * Created by lijx on 16/6/2.
 */

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.licrafter.signPlus.SignExtend;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PtlManager implements Listener {

    private static ProtocolManager protocolManager;
    private SignExtend plugin;

    public PtlManager(SignExtend plugin) {
        this.plugin = plugin;
        protocolManager = ProtocolLibrary.getProtocolManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onEnable() {

        // register listener for outgoing tile entity data:
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Server.TILE_ENTITY_DATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                assert ProtocolUtils.Packet.TileEntityData.isTileEntityDataPacket(packet);
                if (!ProtocolUtils.Packet.TileEntityData.isUpdateSignPacket(packet)) {
                    return; // ignore
                }
                Player player = event.getPlayer();
                BlockPosition blockPosition = ProtocolUtils.Packet.TileEntityData.getBlockPosition(packet);
                Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                NbtCompound signData = ProtocolUtils.Packet.TileEntityData.getTileEntityData(packet);
                String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(signData);

                // call the SignSendEvent:
                LevelSendEvent signSendEvent = callLevelSendEvent(player, location, rawLines);

                if (signSendEvent.isCancelled()) {
                    // don't send tile entity update packet:
                    event.setCancelled(true);
                } else if (signSendEvent.isModified()) { // only replacing the outgoing packet if it is needed
                    String[] newLines = signSendEvent.getLines();

                    // prepare new outgoing packet:
                    PacketContainer outgoingPacket = packet.shallowClone();
                    // create new sign data compound:
                    NbtCompound outgoingSignData = (NbtCompound) NbtFactory.ofCompound(signData.getName());
                    // copy tile entity data (shallow copy):
                    for (String key : signData.getKeys()) {
                        outgoingSignData.put(key, signData.getValue(key));
                    }
                    // replace lines:
                    ProtocolUtils.TileEntity.Sign.setText(outgoingSignData, newLines);
                    // use the modified sign data for the outgoing packet:
                    ProtocolUtils.Packet.TileEntityData.setTileEntityData(outgoingPacket, outgoingSignData);

                    // replace packet for this player:
                    event.setPacket(outgoingPacket);
                }
            }
        });

        // register listener for outgoing map chunk packets:
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                assert ProtocolUtils.Packet.MapChunk.isMapChunkPacket(packet);
                Player player = event.getPlayer();
                World world = player.getWorld();

                // only replacing the outgoing packet if it is needed:
                PacketContainer outgoingPacket = null;
                List<Object> outgoingTileEntitiesData = null;
                boolean removedSignData = false;

                List<Object> tileEntitiesData = ProtocolUtils.Packet.MapChunk.getTileEntitiesData(packet);
                for (int index = 0, size = tileEntitiesData.size(); index < size; index++) {
                    Object nmsTileEntityData = tileEntitiesData.get(index);
                    NbtCompound tileEntityData = NbtFactory.fromNMSCompound(nmsTileEntityData);
                    if (!ProtocolUtils.TileEntity.Sign.isTileEntitySignData(tileEntityData)) {
                        continue; // ignore
                    }

                    int x = ProtocolUtils.TileEntity.getX(tileEntityData);
                    int y = ProtocolUtils.TileEntity.getY(tileEntityData);
                    int z = ProtocolUtils.TileEntity.getZ(tileEntityData);
                    Location location = new Location(world, x, y, z);
                    String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(tileEntityData);

                    // call the SignSendEvent:
                    LevelSendEvent signSendEvent = callLevelSendEvent(player, location, rawLines);
                    if (signSendEvent.isCancelled() || signSendEvent.isModified()) {
                        // prepare new outgoing packet, if we didn't already create one:
                        if (outgoingPacket == null) {
                            outgoingPacket = packet.shallowClone();
                            // copy tile entities data list (shallow copy):
                            outgoingTileEntitiesData = new ArrayList<Object>(tileEntitiesData);
                            // use the new tile entities data list for the outgoing packet:
                            ProtocolUtils.Packet.MapChunk.setTileEntitiesData(outgoingPacket, outgoingTileEntitiesData);
                        }

                        if (signSendEvent.isCancelled()) {
                            // remove tile entity data for this sign from the outgoing packet:
                            // mark the index for later removal by replacing the sign tile entity data with null:
                            outgoingTileEntitiesData.set(index, null);
                            removedSignData = true;
                        } else if (signSendEvent.isModified()) {
                            String[] newLines = signSendEvent.getLines();

                            // prepare new outgoing packet, if we didn't already create one:
                            if (outgoingPacket == null) {
                                outgoingPacket = packet.shallowClone();
                                // copy tile entities data list (shallow copy):
                                outgoingTileEntitiesData = new ArrayList<Object>(tileEntitiesData);
                                // use the new tile entities data list for the outgoing packet:
                                ProtocolUtils.Packet.MapChunk.setTileEntitiesData(outgoingPacket, outgoingTileEntitiesData);
                            }

                            // create new sign data compound:
                            NbtCompound outgoingSignData = (NbtCompound) NbtFactory.ofCompound(tileEntityData.getName());
                            // copy tile entity data:
                            for (String key : tileEntityData.getKeys()) {
                                outgoingSignData.put(key, tileEntityData.getValue(key));
                            }
                            // replace lines:
                            ProtocolUtils.TileEntity.Sign.setText(outgoingSignData, newLines);
                            // replace old sign data with the modified sign data in the outgoing packet:
                            outgoingTileEntitiesData.set(index, ((NbtWrapper<?>) outgoingSignData).getHandle());
                        }
                    }
                }

                if (outgoingPacket != null) {
                    if (removedSignData) {
                        // remove marked (null) tile entity data entries:
                        Iterator<Object> iter = outgoingTileEntitiesData.iterator();
                        while (iter.hasNext()) {
                            if (iter.next() == null) {
                                iter.remove();
                            }
                        }
                    }

                    // replace packet for this player:
                    event.setPacket(outgoingPacket);
                }
            }
        });

    }

    public void onDisable() {
        protocolManager = null;
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            Material blockType = block.getType();
            if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) {
                Sign sign = (Sign) block.getState();
                sendSignChange(event.getPlayer(), sign);
            }
        }

    }

    public static void sendSignChange(Player player, Sign sign) {
        if (player == null || !player.isOnline()) return;
        if (sign == null) return;

        player.sendSignChange(sign.getLocation(), sign.getLines());
    }

    private LevelSendEvent callLevelSendEvent(Player player, Location location, String[] rawLines) {
        // call the LevelSendEvent:
        LevelSendEvent levelSendEvent = new LevelSendEvent(player, location, rawLines);
        Bukkit.getPluginManager().callEvent(levelSendEvent);
        return levelSendEvent;
    }

}
