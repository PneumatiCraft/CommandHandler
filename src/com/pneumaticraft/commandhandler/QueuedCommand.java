package com.pneumaticraft.commandhandler;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class QueuedCommand {
    private String name;
    private List<Object> args;
    private Class<?> paramTypes[];
    private CommandSender sender;
    private JavaPlugin plugin;
    private Calendar timeRequested;
    private String success;
    private String fail;
    private int expiration;

    public QueuedCommand(String commandName, List<Object> args, Class<?> partypes[], CommandSender sender, Calendar instance, JavaPlugin plugin, String success, String fail, int expiration) {
        this.plugin = plugin;
        this.name = commandName;
        this.args = args;
        this.sender = sender;
        this.timeRequested = instance;
        this.paramTypes = partypes;
        this.setSuccess(success);
        this.setFail(fail);
        this.expiration = expiration;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public boolean execute() {
        this.timeRequested.add(Calendar.SECOND, this.expiration);
        if (this.timeRequested.after(Calendar.getInstance())) {
            try {
                Method method = this.plugin.getClass().getMethod(this.name, this.paramTypes);
                Object[] listAsArray = this.args.toArray(new String[this.args.size()]);
                method.invoke(this.plugin, listAsArray);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            this.sender.sendMessage("This command has expired. Please type the original command again.");
        }
        return false;
    }

    private void setSuccess(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return this.success;
    }

    private void setFail(String fail) {
        this.fail = fail;
    }

    public String getFail() {
        return this.fail;
    }

}
