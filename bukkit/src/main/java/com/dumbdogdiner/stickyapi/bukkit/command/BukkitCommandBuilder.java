/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;

import com.dumbdogdiner.stickyapi.StickyAPI;
import com.dumbdogdiner.stickyapi.bukkit.util.SoundUtil;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.command.CommandBuilder;
import com.dumbdogdiner.stickyapi.common.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.ServerVersion;
import com.dumbdogdiner.stickyapi.common.util.NotificationType;
import com.dumbdogdiner.stickyapi.common.util.reflection.ReflectionUtil;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.google.common.collect.ImmutableList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * CommandBuilder for avoiding bukkit's terrible command API and making creating
 * new commands as simple as possible
 * 
 * @since 2.0
 */
public class BukkitCommandBuilder extends CommandBuilder<BukkitCommandBuilder> {

    // Hmm...
    HashMap<CommandSender, Long> cooldownSenders = new HashMap<>();

    Executor executor;
    TabExecutor tabExecutor;

    ErrorHandler errorHandler;
    Plugin owner;

    @FunctionalInterface
    public interface Executor {
        public ExitCode apply(CommandSender sender, Arguments args, HashMap<String, String> vars);
    }

    public interface TabExecutor {
        public java.util.List<String> apply(CommandSender sender, String commandLabel, Arguments args);
    }

    public interface ErrorHandler {
        public void apply(ExitCode exitCode, CommandSender sender, Arguments args, HashMap<String, String> vars);
    }

    /**
     * Create a new {@link BukkitCommandBuilder} instance
     * <p>
     * Used to build and register Bukkit commands
     * 
     * @param name The name of the command
     */
    public BukkitCommandBuilder(@NotNull String name) {
        super(name);
    }

    public BukkitCommandBuilder(@NotNull String name, @NotNull Plugin owner) {
        this(name);
        this.owner = owner;
    }

    private void performAsynchronousExecution(CommandSender sender, org.bukkit.command.Command command, String label,
            List<String> args) {
        StickyAPI.getPool().execute(new FutureTask<Void>(() -> {
            performExecution(sender, command, label, args);
            return null;
        }));
    }

    /**
     * Execute this command. Checks for existing sub-commands, and runs the error
     * handler if anything goes wrong.
     */
    private void performExecution(CommandSender sender, org.bukkit.command.Command command, String label,
            List<String> args) {
        // look for subcommands
        if (args.size() > 0 && getSubCommands().containsKey(args.get(0))) {
            BukkitCommandBuilder subCommand = (BukkitCommandBuilder) getSubCommands().get(args.get(0));
            if (!getSynchronous() && subCommand.getSynchronous()) {
                throw new RuntimeException("Attempted to asynchronously execute a synchronous sub-command!");
            }

            // We can't modify List, so we need to make a clone of it, because java is
            // special.
            ArrayList<String> argsClone = new ArrayList<String>(args);
            argsClone.remove(0);

            // spawn async command from sync
            if (getSynchronous() && !subCommand.getSynchronous()) {
                subCommand.performAsynchronousExecution(sender, command, label, argsClone);
            } else {
                subCommand.performExecution(sender, command, label, argsClone);
            }

            return;
        }

        ExitCode exitCode;
        Arguments a = new Arguments(args);
        var variables = new HashMap<String, String>();
        variables.put("command", command.getName());
        variables.put("sender", sender.getName());
        variables.put("player", sender.getName());
        variables.put("uuid", (sender instanceof Player) ? ((Player) sender).getUniqueId().toString() : "");
        variables.put("cooldown", getCooldown().toString());
        variables.put("cooldown_remaining",
                cooldownSenders.containsKey(sender)
                        ? String.valueOf(getCooldown() - (System.currentTimeMillis() - cooldownSenders.get(sender)))
                        : "0");
        try {
            if (cooldownSenders.containsKey(sender)
                    && ((System.currentTimeMillis() - cooldownSenders.get(sender)) < getCooldown())) {
                exitCode = ExitCode.EXIT_COOLDOWN;
            } else {

                // Add our sender and their command execution time to our hashmap of coolness
                cooldownSenders.put(sender, System.currentTimeMillis());

                // If the user does not have permission to execute the sub command, don't let
                // them execute and return permission denied
                if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
                    exitCode = ExitCode.EXIT_PERMISSION_DENIED;
                } else if (this.getRequiresPlayer() && !(sender instanceof Player)) {
                    exitCode = ExitCode.EXIT_MUST_BE_PLAYER;
                } else {
                    exitCode = executor.apply(sender, a, variables);
                }
            }
        } catch (Exception e) {
            exitCode = ExitCode.EXIT_ERROR;
            e.printStackTrace();
        }

        // run the error handler - something made a fucky wucky uwu
        if (exitCode != ExitCode.EXIT_SUCCESS) {
            if (exitCode == ExitCode.EXIT_INFO) {
                _playSound(sender, NotificationType.INFO);
                return;
            }
            errorHandler.apply(exitCode, sender, a, variables);
            _playSound(sender, NotificationType.ERROR);
        } else {
            _playSound(sender, NotificationType.SUCCESS);
        }
    }

    /**
     * Set the executor of the command
     * 
     * @param executor to set
     * @return {@link CommandBuilder}
     */
    public BukkitCommandBuilder onExecute(@NotNull Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Set the tab complete executor of the command
     * 
     * @param executor to set
     * @return {@link CommandBuilder}
     */
    public BukkitCommandBuilder onTabComplete(@NotNull TabExecutor executor) {
        this.tabExecutor = executor;
        return this;
    }

    /**
     * Set the error handler of the command
     * 
     * @param handler to set
     * @return {@link CommandBuilder}
     */
    public BukkitCommandBuilder onError(@NotNull ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Build the command!
     * 
     * @param plugin to build it for
     * @return {@link org.bukkit.command.Command}
     */
    public org.bukkit.command.Command build(@NotNull Plugin plugin) {
        PluginCommand command = new PluginCommand(this.getName(), plugin);

        if (this.getSynchronous() == null) {
            this.synchronous(false);
        }

        BukkitCommandBuilder that = this;

        // Execute the command by creating a new CommandExecutor and passing the
        // arguments to our executor
        command.setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label,
                    String[] args) {
                if (that.getSynchronous()) {
                    performExecution(sender, command, label, Arrays.asList(args));
                } else {
                    performAsynchronousExecution(sender, command, label, Arrays.asList(args));
                }
                return true;
            }
        });

        command.setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
                if (tabExecutor == null) {
                    if (args.length == 0) {
                        return ImmutableList.of();
                    }

                    String lastWord = args[args.length - 1];

                    Player senderPlayer = sender instanceof Player ? (Player) sender : null;

                    ArrayList<String> matchedPlayers = new ArrayList<String>();
                    for (Player player : sender.getServer().getOnlinePlayers()) {
                        String name = player.getName();
                        if ((senderPlayer == null || senderPlayer.canSee(player))
                                && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                            matchedPlayers.add(name);
                        }
                    }

                    Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
                    return matchedPlayers;
                } else {
                    return tabExecutor.apply(sender, alias, new Arguments(Arrays.asList(args)));
                }
            }
        });

        command.setDescription(this.getDescription());

        if (this.getAliases() != null)
            command.setAliases(this.getAliases());

        command.setPermission(this.getPermission());
        return command;
    }

    /**
     * Build the command!
     * 
     * @return {@link org.bukkit.command.Command}
     */
    public org.bukkit.command.Command build() throws NullPointerException {
        if (this.owner == null) {
            throw new NullPointerException("Owning plugin is null, did you construct this object without an owner?");
        }
        return this.build(this.owner);
    }

    /**
     * Register the command with a {@link org.bukkit.plugin.Plugin}
     * 
     * @param plugin to register with
     */
    public void register(@NotNull Plugin plugin) {

        // If the server is running paper, we don't need to do reflection, which is
        // good.
        if (ServerVersion.isPaper()) {
            plugin.getServer().getCommandMap().register(plugin.getName(), this.build(plugin));
            return;
        }
        // However, if it's not running paper, we need to use reflection, which is
        // really annoying
        ((CommandMap) ReflectionUtil.getProtectedValue(plugin.getServer(), "commandMap")).register(plugin.getName(),
                this.build(plugin));
    }

    /**
     * Register the command with a {@link org.bukkit.plugin.Plugin}
     */
    public void register() {
        if (this.owner == null) {
            throw new NullPointerException("Owning plugin is null, did you construct this object without an owner?");
        }
        this.register(this.owner);
    }

    private void _playSound(CommandSender sender, NotificationType type) {
        if (!this.getPlaySound())
            return;
        SoundUtil.send(sender, type);
    }
}
