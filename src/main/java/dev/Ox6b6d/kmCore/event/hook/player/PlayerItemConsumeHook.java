package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Hook fired when a player finishes eating or drinking an item.
 * <p>
 * Used by {@code DownedModule} to prevent downed players from eating or
 * drinking (e.g. potions, food).
 */
public interface PlayerItemConsumeHook extends Hook {

    /**
     * Called when a player consumes an item.
     *
     * @param event the Bukkit {@link PlayerItemConsumeEvent}; may be cancelled
     */
    void onPlayerItemConsume(PlayerItemConsumeEvent event);
}