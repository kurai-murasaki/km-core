package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Hook fired when a player changes their active hotbar slot.
 * <p>
 * Implement on any {@link dev.Ox6b6d.kmCore.module.Module} that needs to react
 * to held-item changes — e.g. applying per-weapon attack-speed overrides.
 */
public interface PlayerItemHeldHook extends Hook {

    /**
     * Called when a player switches their selected hotbar slot.
     *
     * @param event the Bukkit {@link PlayerItemHeldEvent}
     */
    void onPlayerItemHeld(PlayerItemHeldEvent event);
}