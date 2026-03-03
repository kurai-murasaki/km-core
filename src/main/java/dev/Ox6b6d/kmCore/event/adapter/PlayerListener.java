package dev.Ox6b6d.kmCore.event.adapter;

import dev.Ox6b6d.kmCore.KmCore;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryClickHook;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryCloseHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerInteractHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerItemHeldHook;   // ← new
import dev.Ox6b6d.kmCore.event.hook.player.PlayerJoinHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerQuitHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;              // ← new
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final KmCore plugin;

    public PlayerListener(KmCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        plugin.getModuleManager()
                .getHooks(PlayerJoinHook.class)
                .forEach(hook -> hook.onPlayerJoin(event));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        plugin.getModuleManager()
                .getHooks(PlayerQuitHook.class)
                .forEach(hook -> hook.onPlayerQuit(event));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerInteractHook.class)
                .forEach(hook -> hook.onPlayerInteract(event));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        plugin.getModuleManager()
                .getHooks(InventoryCloseHook.class)
                .forEach(hook -> hook.onInventoryClose(event));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getModuleManager()
                .getHooks(InventoryClickHook.class)
                .forEach(hook -> hook.onInventoryClick(event));
    }
    /**
     * Delegates hotbar slot-change events to all registered
     * {@link PlayerItemHeldHook} modules (e.g. {@code CombatModule}).
     */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerItemHeldHook.class)
                .forEach(hook -> hook.onPlayerItemHeld(event));
    }
}