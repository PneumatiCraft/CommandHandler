package com.pneumaticraft.commandhandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandHandler {

    protected JavaPlugin plugin;

    protected List<QueuedCommand> queuedCommands;
    protected List<Command> allCommands;

    protected PermissionsInterface permissions;

    public CommandHandler(JavaPlugin plugin, PermissionsInterface permissions) {
        this.plugin = plugin;

        this.allCommands = new ArrayList<Command>();
        this.queuedCommands = new ArrayList<QueuedCommand>();
        this.permissions = permissions;
    }
    
    public List<Command> getCommands(CommandSender sender) {
        List<Command> permissiveCommands = new ArrayList<Command>();
        for(Command c : this.allCommands) {
            if(this.permissions.hasPermission(sender, c.getPermission(), c.isOpRequired())) {
                permissiveCommands.add(c);
            }
        }
        return permissiveCommands;
    }
    
    public List<Command> getAllCommands() {
        return this.allCommands;
    }

    public boolean locateAndRunCommand(CommandSender sender, List<String> args) {
        ArrayList<String> parsedArgs = parseAllQuotedStrings(args);
        String key = null;

        Iterator<Command> iterator = this.allCommands.iterator();
        Command foundCommand = null;
        while (iterator.hasNext() && key == null) {
            foundCommand = iterator.next();
            key = foundCommand.getKey(parsedArgs);
            if (key != null) {
                // This method, removeKeyArgs mutates parsedArgs
                foundCommand.removeKeyArgs(parsedArgs, key);
                checkAndRunCommand(sender, parsedArgs, foundCommand);
            }
        }
        return true;
    }

    public void registerCommand(Command command) {
        this.allCommands.add(command);
    }

    /**
     * Combines all quoted strings
     * 
     * @param args
     * @return
     */
    private ArrayList<String> parseAllQuotedStrings(List<String> args) {
        // TODO: Allow '
        // TODO: make less awkward, less magical
        ArrayList<String> newArgs = new ArrayList<String>();
        // Iterate through all command params:
        // we could have: "Fish dog" the man bear pig "lives today" and maybe "even tomorrow" or "the" next day
        int start = -1;
        for (int i = 0; i < args.size(); i++) {

            // If we aren't looking for an end quote, and the first part of a string is a quote
            if (start == -1 && args.get(i).substring(0, 1).equals("\"")) {
                start = i;
            }
            // Have to keep this separate for one word quoted strings like: "fish"
            if (start != -1 && args.get(i).substring(args.get(i).length() - 1, args.get(i).length()).equals("\"")) {
                // Now we've found the second part of a string, let's parse the quoted one out
                // Make sure it's i+1, we still want I included
                newArgs.add(parseQuotedString(args, start, i + 1));
                // Reset the start to look for more!
                start = -1;
            } else if (start == -1) {
                // This is a word that is NOT enclosed in any quotes, so just add it
                newArgs.add(args.get(i));
            }
        }
        // If the string was ended but had an open quote...
        if (start != -1) {
            // ... then we want to close that quote and make that one arg.
            newArgs.add(parseQuotedString(args, start, args.size()));
        }

        return newArgs;
    }

    /**
     * 
     */
    public void queueCommand(CommandSender sender, String commandName, String methodName, List<String> args, Class<?>[] paramTypes, String success, String fail) {
        cancelQueuedCommand(sender);
        this.queuedCommands.add(new QueuedCommand(methodName, args, paramTypes, sender, Calendar.getInstance(), this.plugin, success, fail));
        sender.sendMessage("The command " + ChatColor.RED + commandName + ChatColor.WHITE + " has been halted due to the fact that it could break something!");
        sender.sendMessage("If you still wish to execute " + ChatColor.RED + commandName + ChatColor.WHITE);
        sender.sendMessage("please type: " + ChatColor.GREEN + "/mvconfirm");
        sender.sendMessage(ChatColor.GREEN + "/mvconfirm" + ChatColor.WHITE + " will only be available for 10 seconds.");
    }

    /**
     * Tries to fire off the command
     * 
     * @param sender
     * @return
     */
    public boolean confirmQueuedCommand(CommandSender sender) {
        for (QueuedCommand com : this.queuedCommands) {
            if (com.getSender().equals(sender)) {
                if (com.execute()) {
                    sender.sendMessage(com.getSuccess());
                    return true;
                } else {
                    sender.sendMessage(com.getFail());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Cancels(invalidates) a command that has been requested. This is called when a user types something other than 'yes' or when they try to queue a second command Queuing a second command will delete the first command entirely.
     * 
     * @param sender
     */
    public void cancelQueuedCommand(CommandSender sender) {
        QueuedCommand c = null;
        for (QueuedCommand com : this.queuedCommands) {
            if (com.getSender().equals(sender)) {
                c = com;
            }
        }
        if (c != null) {
            // Each person is allowed at most one queued command.
            this.queuedCommands.remove(c);
        }
    }

    /**
     * Returns the given flag value
     * 
     * @param flag A param flag, like -s or -g
     * @param args All arguments to search through
     * @return A string or null
     */
    public static String getFlag(String flag, List<String> args) {
        int i = 0;
        try {
            for (String s : args) {
                if (s.equalsIgnoreCase(flag)) {
                    return args.get(i + 1);
                }
                i++;
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }

    /**
     * Takes a string array and returns a combined string, excluding the stop position, including the start
     * 
     * @param args
     * @param start
     * @param stop
     * @return
     */
    private String parseQuotedString(List<String> args, int start, int stop) {
        String returnVal = args.get(start);
        for (int i = start + 1; i < stop; i++) {
            returnVal += " " + args.get(i);
        }
        return returnVal.replace("\"", "");
    }

    private void checkAndRunCommand(CommandSender sender, List<String> parsedArgs, Command foundCommand) {
        if (foundCommand.checkArgLength(parsedArgs)) {
            // Or used so if someone doesn't implement permissions interface, all commands will run.
            if (this.permissions == null || this.permissions.hasPermission(sender, foundCommand.getPermission(), foundCommand.isOpRequired())) {
                foundCommand.runCommand(sender, parsedArgs);
            } else {
                sender.sendMessage("You do not have the required permission (" + foundCommand.getPermission() + ").");
            }
        } else {
            // TODO make me pretty
            sender.sendMessage(foundCommand.commandName);
            sender.sendMessage(foundCommand.commandDesc);
            sender.sendMessage(foundCommand.commandUsage);
            sender.sendMessage(foundCommand.permission);
        }
    }
}
