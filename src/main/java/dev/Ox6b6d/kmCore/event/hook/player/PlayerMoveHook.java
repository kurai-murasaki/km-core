package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Hook fired every time a player moves (position or head rotation).
 * <p>
 * Used by {@code DownedModule} to:
 * <ul>
 *   <li>Freeze downed players in place (cancel XZ position changes).</li>
 *   <li>Prevent carriers from sprinting, flying, gliding, or swimming.</li>
 *   <li>Cancel an in-progress revival when the reviver walks out of range.</li>
 * </ul>
 *
 * <strong>Note:</strong> this hook fires very frequently — keep implementations
 * as lightweight as possible.
 */
public interface PlayerMoveHook extends Hook {

    /**
     * Called when a player moves.
     *
     * @param event the Bukkit {@link PlayerMoveEvent}; position may be overridden
     */
    void onPlayerMove(PlayerMoveEvent event);
}