package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Hook fired when a player respawns after death.
 * <p>
 * Used by {@code DownedModule} to restore inventory items that were preserved
 * by the per-slot drop-chance mechanic on bleedout death.
 */
public interface PlayerRespawnHook extends Hook {

    /**
     * Called when a player respawns.
     *
     * @param event the Bukkit {@link PlayerRespawnEvent}
     */
    void onPlayerRespawn(PlayerRespawnEvent event);
}