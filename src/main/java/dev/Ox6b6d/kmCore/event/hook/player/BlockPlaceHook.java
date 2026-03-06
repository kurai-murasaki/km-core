package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Hook fired when a player places a block.
 * <p>
 * Used by {@code DownedModule} to prevent downed players from placing blocks.
 */
public interface BlockPlaceHook extends Hook {

    /**
     * Called when a player places a block.
     *
     * @param event the Bukkit {@link BlockPlaceEvent}; may be cancelled
     */
    void onBlockPlace(BlockPlaceEvent event);
}