package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Hook fired when a player breaks a block.
 * <p>
 * Used by {@code DownedModule} to prevent downed players from mining.
 */
public interface BlockBreakHook extends Hook {

    /**
     * Called when a player breaks a block.
     *
     * @param event the Bukkit {@link BlockBreakEvent}; may be cancelled
     */
    void onBlockBreak(BlockBreakEvent event);
}