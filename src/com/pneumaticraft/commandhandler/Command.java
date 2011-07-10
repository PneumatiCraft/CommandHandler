package com.pneumaticraft.commandhandler;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Command {
    protected JavaPlugin plugin;

    protected String permission;
    protected boolean opRequired;

    protected int minimumArgLength;
    protected int maximumArgLength;

    protected String commandName;
    protected String commandDesc;
    protected String commandExample;
    protected String commandUsage;

    protected List<String> commandKeys;

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

    public String getPermission() {
        return this.permission;
    }

    public boolean isOpRequired() {
        return this.opRequired;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public String getCommandDesc() {
        return this.commandDesc;
    }

    public String getCommandExample() {
        return this.commandExample;
    }

    public String getCommandUsage() {
        return this.commandUsage;
    }

    /**
     * @return the plugin
     */
    public JavaPlugin getPlugin() {
        return this.plugin;
    }
}
