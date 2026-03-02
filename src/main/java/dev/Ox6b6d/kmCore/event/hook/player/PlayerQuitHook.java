package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerQuitEvent;

public interface PlayerQuitHook extends Hook {
    void onPlayerQuit(PlayerQuitEvent event);
}
