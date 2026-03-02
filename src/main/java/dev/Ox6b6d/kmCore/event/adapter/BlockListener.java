package dev.Ox6b6d.kmCore.event.adapter;

import dev.Ox6b6d.kmCore.KmCore;
import dev.Ox6b6d.kmCore.event.hook.player.BlockFlowHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

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
}