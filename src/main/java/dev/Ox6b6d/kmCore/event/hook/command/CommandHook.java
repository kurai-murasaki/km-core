package dev.Ox6b6d.kmCore.event.hook.command;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Collections;

public interface CommandHook extends Hook {

    String getCommand();

    boolean onCommand(CommandSender sender, String[] args);

    /**
     * Tab completion
     */
    default Collection<String> suggest(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}