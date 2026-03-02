package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerInteractEvent;

public interface PlayerInteractHook extends Hook {
    void onPlayerInteract(PlayerInteractEvent event);
}
