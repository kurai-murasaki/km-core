package dev.Ox6b6d.kmCore.module.impl.mechanics;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.player.BlockFlowHook;
import dev.Ox6b6d.kmCore.module.Module;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockFromToEvent;

public class RedstoneObsidianModule implements Module, Configurable, BlockFlowHook {

    @ConfigValue(key = "play-sound", comment = "Play the lava extinguish sound when obsidian is formed")
    private boolean playSound = true;

    @ConfigValue(key = "require-redstone-wire", comment = "Only trigger when lava/water flows onto redstone wire. If false, triggers on any block")
    private boolean requireRedstoneWire = true;

    @Override public String getName()          { return "RedstoneObsidianModule"; }
    @Override public String getConfigSection() { return "redstone-obsidian"; }

    @Override public void enable()  {}
    @Override public void disable() {}

    private boolean isAdjacentToOppositeFluid(Block block, Material source) {
        Material opposite = (source == Material.WATER) ? Material.LAVA : Material.WATER;

        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            if (block.getRelative(face).getType() == opposite) return true;
        }

        return false;
    }

    @Override
    public void onBlockFlow(BlockFromToEvent event) {
        Material source = event.getBlock().getType();

        if (source != Material.WATER && source != Material.LAVA) return;

        Block target = event.getToBlock();

        if (requireRedstoneWire && target.getType() != Material.REDSTONE_WIRE) return;

        if (!isAdjacentToOppositeFluid(target, source)) return;

        event.setCancelled(true);
        target.setType(Material.OBSIDIAN);

        if (playSound) {
            target.getWorld().playSound(
                    target.getLocation(),
                    Sound.BLOCK_LAVA_EXTINGUISH,
                    1.0f,
                    1.0f
            );
        }
    }
}