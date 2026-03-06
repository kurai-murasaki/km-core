package dev.Ox6b6d.kmCore.event.adapter;

import dev.Ox6b6d.kmCore.KmCore;
import dev.Ox6b6d.kmCore.event.hook.player.EntityDamageHook;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryClickHook;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryCloseHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerDeathHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerDropItemHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerInteractEntityHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerInteractHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerItemConsumeHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerItemHeldHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerJoinHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerMoveHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerQuitHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerRespawnHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerToggleSneakHook;
import dev.Ox6b6d.kmCore.event.hook.player.VehicleExitHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class PlayerListener implements Listener {

    private final KmCore plugin;

    public PlayerListener(KmCore plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Original handlers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Handlers added for DownedModule (and available to future modules)
    // -------------------------------------------------------------------------

    /**
     * Delegates entity-damage events at HIGH priority so intercepting modules
     * run after most other plugins but before MONITOR-priority listeners.
     * <p>
     * {@code ignoreCancelled = false} ensures downed-immunity cancellation
     * still fires even if another plugin already cancelled the event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
        plugin.getModuleManager()
                .getHooks(EntityDamageHook.class)
                .forEach(hook -> hook.onEntityDamage(event));
    }

    /**
     * Delegates right-click-entity events (e.g. picking up a downed player
     * by right-clicking them).
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerInteractEntityHook.class)
                .forEach(hook -> hook.onPlayerInteractEntity(event));
    }

    /**
     * Delegates sneak-toggle events used for revival start/cancel and
     * carry-drop mechanics.
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerToggleSneakHook.class)
                .forEach(hook -> hook.onPlayerToggleSneak(event));
    }

    /**
     * Delegates player-move events at HIGH priority so downed-player
     * freezing and reviver-range checks run reliably.
     * <p>
     * {@code ignoreCancelled = false} ensures frozen downed players are
     * re-teleported even if their movement was already cancelled elsewhere.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerMoveHook.class)
                .forEach(hook -> hook.onPlayerMove(event));
    }

    /**
     * Delegates player-death events at HIGH priority so drop modifications
     * (partial item drops on bleedout) happen before MONITOR listeners
     * observe the drop list.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerDeathHook.class)
                .forEach(hook -> hook.onPlayerDeath(event));
    }

    /**
     * Delegates player-respawn events used to restore items that were kept
     * (not dropped) by the per-slot drop-chance mechanic on bleedout death.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerRespawnHook.class)
                .forEach(hook -> hook.onPlayerRespawn(event));
    }

    /**
     * Delegates item-consume events so modules can prevent downed players
     * from eating food or drinking potions.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerItemConsumeHook.class)
                .forEach(hook -> hook.onPlayerItemConsume(event));
    }

    /**
     * Delegates item-drop events so modules can prevent downed players
     * from throwing items on the ground.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        plugin.getModuleManager()
                .getHooks(PlayerDropItemHook.class)
                .forEach(hook -> hook.onPlayerDropItem(event));
    }

    /**
     * Delegates vehicle-exit events so modules can prevent a downed player
     * from dismounting their carrier.
     * <p>
     * Note: {@link VehicleExitEvent} is a vehicle event, not a player event,
     * but it is dispatched here because it always involves a living entity
     * (the passenger) and its most relevant consumer is player-state logic.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        plugin.getModuleManager()
                .getHooks(VehicleExitHook.class)
                .forEach(hook -> hook.onVehicleExit(event));
    }

}