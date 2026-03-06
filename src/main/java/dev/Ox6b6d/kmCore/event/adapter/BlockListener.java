package dev.Ox6b6d.kmCore.event.adapter;

import dev.Ox6b6d.kmCore.KmCore;
import dev.Ox6b6d.kmCore.event.hook.player.BlockBreakHook;
import dev.Ox6b6d.kmCore.event.hook.player.BlockFlowHook;
import dev.Ox6b6d.kmCore.event.hook.player.BlockPlaceHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private final KmCore plugin;

    public BlockListener(KmCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockFlow(BlockFromToEvent event) {
        plugin.getModuleManager()
                .getHooks(BlockFlowHook.class)
                .forEach(hook -> hook.onBlockFlow(event));
    }

    /**
     * Delegates block-break events so modules (e.g. {@code DownedModule}) can
     * prevent downed players from breaking blocks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getModuleManager()
                .getHooks(BlockBreakHook.class)
                .forEach(hook -> hook.onBlockBreak(event));
    }

    /**
     * Delegates block-place events so modules (e.g. {@code DownedModule}) can
     * prevent downed players from placing blocks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.getModuleManager()
                .getHooks(BlockPlaceHook.class)
                .forEach(hook -> hook.onBlockPlace(event));
    }
}