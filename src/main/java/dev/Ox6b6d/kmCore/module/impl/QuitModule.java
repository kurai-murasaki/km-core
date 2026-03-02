package dev.Ox6b6d.kmCore.module.impl;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerQuitHook;
import dev.Ox6b6d.kmCore.module.Module;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitModule implements Module, Configurable, PlayerQuitHook {

    @ConfigValue(key = "message", comment = "Quit message. Use {player} for the player name. Supports MiniMessage format.")
    private String message = "<dark_gray>[<light_purple>-<dark_gray>] <gray>{player}<dark_gray> disconnected.";

    @Override public String getName()          { return "QuitModule"; }
    @Override public String getConfigSection() { return "quit"; }
    @Override public void enable()             {}
    @Override public void disable()            {}

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String formatted = message.replace("{player}", event.getPlayer().getName());
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(formatted));
    }
}