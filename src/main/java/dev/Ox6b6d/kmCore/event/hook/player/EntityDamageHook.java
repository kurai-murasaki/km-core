package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Hook fired when any entity receives damage.
 * <p>
 * Primarily used by {@code DownedModule} to intercept lethal damage and
 * transition players into the downed state instead of dying.
 */
public interface EntityDamageHook extends Hook {

    /**
     * Called when an entity is damaged.
     *
     * @param event the Bukkit {@link EntityDamageEvent}; may be cancelled
     */
    void onEntityDamage(EntityDamageEvent event);
}