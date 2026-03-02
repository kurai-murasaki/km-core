package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerJoinEvent;

public interface PlayerJoinHook extends Hook {
    void onPlayerJoin(PlayerJoinEvent event);
}
