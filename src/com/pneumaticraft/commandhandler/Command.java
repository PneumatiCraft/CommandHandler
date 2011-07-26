package com.pneumaticraft.commandhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Command {
    private JavaPlugin plugin;

    private String permissionString;
    private boolean opRequired;

    private int minimumArgLength;
    private int maximumArgLength;

    private String commandName;
    private String commandExample;
    private String commandUsage;

    private List<String> commandKeys;

    private Permission permission;

    public Command(JavaPlugin plugin) {
        this.plugin = plugin;

        this.commandKeys = new ArrayList<String>();
    }

    public abstract void runCommand(CommandSender sender, List<String> args);

    public boolean checkArgLength(List<String> args) {
        return (this.minimumArgLength == -1 || this.minimumArgLength <= args.size())
                && (args.size() <= this.maximumArgLength || this.maximumArgLength == -1);
    }

    private String getArgsString(List<String> args) {
        String returnString = "";
        for (String s : args) {
            returnString += s + " ";
        }
        return returnString.substring(0, returnString.length() - 1);
    }

    /**
     * This method is provided as a convenience to add additional permissions recursively to all nodes
     * 
     * @param otherPerm The Permission to add.
     */
    public void addAdditonalPermission(Permission otherPerm) {
        this.plugin.getServer().getPluginManager().addPermission(otherPerm);
        this.addToParentPerms(otherPerm.getName());
    }

    public String getKey(ArrayList<String> parsedArgs) {
        // Combines our args to a space separated string
        String argsString = this.getArgsString(parsedArgs);

        for (String s : this.commandKeys) {
            String identifier = s.toLowerCase();
            if (argsString.matches(identifier + "(\\s+.*|\\s*)")) {
                return identifier;
            }
        }
        return null;
    }

    // mutates!
    public List<String> removeKeyArgs(List<String> args, String key) {
        int identifierLength = key.split(" ").length;
        for (int i = 0; i < identifierLength; i++) {
            // Since we're pulling from the front, always remove the first element
            args.remove(0);
        }
        return args;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void setPermission(String p, String desc, PermissionDefault defaultPerm) {
        this.setPermission(new Permission(p, desc, defaultPerm));
    }

    public void setPermission(Permission perm) {
        this.opRequired = (perm.getDefault() == PermissionDefault.OP);
        this.permissionString = perm.getName();
        this.permission = perm;
        this.plugin.getServer().getPluginManager().addPermission(this.permission);
        this.addToParentPerms(this.permissionString);
    }

    private void addToParentPerms(String permString) {
        String[] seperated = permString.split("\\.");
        String cumulativePerm = "";
        Permission tempPerm = null;
        for (String s : seperated) {
            cumulativePerm += s;
            tempPerm = this.plugin.getServer().getPluginManager().getPermission(cumulativePerm + ".*");
            if (tempPerm == null)
            {
                tempPerm = new Permission(cumulativePerm + ".*");
                this.plugin.getServer().getPluginManager().addPermission(tempPerm);
            }
            if (!tempPerm.getChildren().containsKey(this.permissionString)) {
                tempPerm.getChildren().put(this.permissionString, true);
                this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(tempPerm);
            }
            cumulativePerm += ".";
        }
    }

    public boolean isOpRequired() {
        return this.opRequired;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public String getCommandDesc() {
        return this.permission.getDescription();
    }

    public String getCommandExample() {
        return this.commandExample;
    }

    public String getCommandUsage() {
        return this.commandUsage;
    }

    public void setCommandExample(String example) {
        this.commandExample = example;
    }

    public void setCommandUsage(String usage) {
        this.commandUsage = usage;
    }

    public void setArgRange(int min, int max) {
        this.minimumArgLength = min;
        this.maximumArgLength = max;
    }

    public void setName(String name) {
        this.commandName = name;
    }

    public void addKey(String key) {
        this.commandKeys.add(key);
        Collections.sort(commandKeys, new ReverseLengthSorter());
    }

    /**
     * @return the plugin
     */
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    private class ReverseLengthSorter implements Comparator<String> {
        public int compare(String stringA, String stringB) {
            if (stringA.length() > stringB.length()) {
                return -1;
            } else if (stringA.length() < stringB.length()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
