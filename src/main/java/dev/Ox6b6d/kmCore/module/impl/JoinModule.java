package dev.Ox6b6d.kmCore.module.impl;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerJoinHook;
import dev.Ox6b6d.kmCore.module.Module;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinModule implements Module, Configurable, PlayerJoinHook {

    @ConfigValue(key = "message", comment = "Join message. Use {player} for the player name. Supports MiniMessage format.")
    private String message = "<dark_gray>[<light_purple>+<dark_gray>] <gray>{player}<dark_gray> connected.";

    @Override public String getName()          { return "JoinModule"; }
    @Override public String getConfigSection() { return "join"; }
    @Override public void enable()             {}
    @Override public void disable()            {}

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        String formatted = message.replace("{player}", event.getPlayer().getName());
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(formatted));
    }
}