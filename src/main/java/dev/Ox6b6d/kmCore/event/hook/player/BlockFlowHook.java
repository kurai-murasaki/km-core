package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.block.BlockFromToEvent;

public interface BlockFlowHook extends Hook {
    void onBlockFlow(BlockFromToEvent event);
}
