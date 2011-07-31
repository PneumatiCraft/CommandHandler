package com.pneumaticraft.commandhandler;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface PermissionsInterface {
    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired);
    public boolean hasAnyPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired);
    public boolean hasAllPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired);
}
