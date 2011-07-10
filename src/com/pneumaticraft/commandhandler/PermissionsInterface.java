package com.pneumaticraft.commandhandler;

import org.bukkit.command.CommandSender;

public interface PermissionsInterface {
    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired);
}
