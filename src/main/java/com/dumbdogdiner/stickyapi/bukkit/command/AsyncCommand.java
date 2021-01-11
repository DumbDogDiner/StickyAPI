/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
/*
<<<<<<< HEAD
 * Copyright (c) 2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
=======
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
>>>>>>> yeen-cherrypick
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;

import com.dumbdogdiner.stickyapi.StickyAPI;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This class is designed to handle execution of commands given by a user or the
 * console, it runs the command code asynchronously from the server thread and
 * will not cause tick lag if the command takes too long. Please use the with
 * caution!
 * 
 * @deprecated Use
 *             {@link com.dumbdogdiner.stickyapi.bukkit.command.BukkitCommandBuilder}
 *             as this will be removed in a future release
 */
@Deprecated
public abstract class AsyncCommand extends Command implements PluginIdentifiableCommand {
    private Plugin owner;
    private TabCompleter completer;

    /**
     * Create a new command for the associated plugin
     * 
     * @param commandName The name of the command the user will execute
     * @param owner       The plugin that owns this command.
     */
    public AsyncCommand(String commandName, Plugin owner) {
        super(commandName);
        this.owner = owner;
    }

    /**
     * Execute the command itself (part of the derived class)
     * 
     * 
     * @param sender       Who is executing the command
     * @param commandLabel The command string triggering this command
     * @param args         The arguments provided to this command
     * @return The exit code for the command
     */
    // public abstract int executeCommand(Sender sender, String commandLabel,
    // String[] args);

    public abstract ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args);

    /**
     * This is a vastly simplified command class. We only check if the plugin is
     * enabled before we execute whereas spigot's `PluginCommand` will attempt to
     * check permissions beforehand.
     * 
     * <p>
     * This also allows us to do async commands if we so desire and it nulls the
     * point of CommandExecutors because they were fucking pointless to begin with.
     * 
     * @param sender       The person executing the command
     * @param commandLabel The command that was executed
     * @param args         The arguments given to the command.
     * @return {@link ExitCode}
     */
    @Override
    public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.owner.isEnabled())
            throw new CommandException(String.format("Cannot execute command \"%s\" in plugin %s - plugin is disabled.",
                    commandLabel, this.owner.getDescription().getFullName()));

        AsyncCommand self = this;
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    ExitCode resultingExitCode = self.executeCommand(sender, commandLabel, args);

                    if (resultingExitCode == null) {
                        throw new IllegalArgumentException("A null exit code was returned");
                    }

                    if (resultingExitCode.getMessage() != null) {
                        sender.sendMessage(ChatColor.RED + resultingExitCode.getMessage());
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                            + self.owner.getDescription().getFullName(), ex);
                }
                return true;
            }
        });

        StickyAPI.getPool().execute(t);

        return true; // we always return true, we don't care what bukkit thinks
    }

    /**
     * Gets the owner of this PluginCommand
     *
     * @return Plugin that owns this command
     */
    @Override
    public Plugin getPlugin() {
        return this.owner;
    }

    /**
     * Sets the {@link CommandExecutor} to run when parsing this command NOTE: This
     * function does nothing.
     * 
     * @param executor New executor to run
     */
    public void setExecutor(CommandExecutor executor) {
        // no-op
    }

    /**
     * Gets the {@link CommandExecutor} associated with this command NOTE: These
     * commands will never have CommandExecutors and will *ALWAYS* return the plugin
     * itself.
     * 
     * @return Plugin.
     */
    public CommandExecutor getExecutor() {
        return this.owner;
    }

    /**
     * Sets the {@link TabCompleter} to run when tab-completing this command.
     * <p>
     * If no TabCompleter is specified, and the command's executor implements
     * TabCompleter, then the executor will be used for tab completion.
     *
     * @param completer New tab completer
     */
    public void setTabCompleter(TabCompleter completer) {
        this.completer = completer;
    }

    /**
     * Gets the {@link TabCompleter} associated with this command.
     *
     * @return TabCompleter object linked to this command
     */
    public @Nullable TabCompleter getTabCompleter() {
        return this.completer;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the tab completer if present.
     * <p>
     * If it is not present or returns null, will delegate to the current command
     * executor if it implements {@link TabCompleter}. If a non-null list has not
     * been found, will default to standard player name completion in
     * {@link Command#tabComplete(CommandSender, String, String[])}.
     * <p>
     * This method does not consider permissions.
     *
     * @throws CommandException         if the completer or executor throw an
     *                                  exception during the process of
     *                                  tab-completing.
     * @throws IllegalArgumentException if sender, alias, or args is null
     */
    @Override
    public java.util.@NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias,
            String[] args) throws CommandException, IllegalArgumentException {
        if (args == null)
            throw new NullPointerException("arguments to tabComplete cannot be null");

        List<String> completions = null;
        try {
            if (completer != null)
                completions = completer.onTabComplete(sender, this, alias, args);
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (String arg : args)
                message.append(arg).append(' ');

            message.deleteCharAt(message.length() - 1).append("' in plugin ")
                    .append(owner.getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }

        if (completions == null)
            return super.tabComplete(sender, alias, args);

        return completions;
    }

    /**
     * Convert this command name to a string
     * 
     * @return the human readable name of the class
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(", ").append(owner.getDescription().getFullName()).append(')');
        return stringBuilder.toString();
    }
}
