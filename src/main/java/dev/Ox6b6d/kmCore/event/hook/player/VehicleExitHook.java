package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.vehicle.VehicleExitEvent;

/**
 * Hook fired when a living entity exits a vehicle.
 * <p>
 * Used by {@code DownedModule} to prevent a downed player from dismounting
 * their carrier while in the carried state.
 */
public interface VehicleExitHook extends Hook {

    /**
     * Called when an entity exits a vehicle.
     *
     * @param event the Bukkit {@link VehicleExitEvent}; may be cancelled
     */
    void onVehicleExit(VehicleExitEvent event);
}