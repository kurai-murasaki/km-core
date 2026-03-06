package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Hook fired when a player starts or stops sneaking (presses/releases Shift).
 * <p>
 * Used by {@code DownedModule} to:
 * <ul>
 *   <li>Begin a revival channel when a player sneaks near a downed teammate.</li>
 *   <li>Cancel an in-progress revival when the player stops sneaking.</li>
 *   <li>Drop a carried downed player when the carrier sneaks.</li>
 * </ul>
 */
public interface PlayerToggleSneakHook extends Hook {

    /**
     * Called when a player toggles their sneak state.
     *
     * @param event the Bukkit {@link PlayerToggleSneakEvent}
     */
    void onPlayerToggleSneak(PlayerToggleSneakEvent event);
}