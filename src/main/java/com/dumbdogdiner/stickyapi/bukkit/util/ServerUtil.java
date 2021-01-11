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
package com.dumbdogdiner.stickyapi.bukkit.util;

import com.destroystokyo.paper.Title;
import com.dumbdogdiner.stickyapi.common.translation.Translation;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ServerUtil {
    private ServerUtil() {}

    /**
     * Get the server's TPS over the last 15 minutes (1m, 5m, 15m)
     * 
     * @return {@link java.util.ArrayList}
     * @since 2.0 (in 1.x, this method did not work correctly!)
     */
    public static double[] getRecentTps() {
        Object minecraftServer = ReflectionUtil.getProtectedValue(Bukkit.getServer(), "console");

        return ReflectionUtil.getProtectedValue(minecraftServer, "recentTps");
    }

    /**
     * Broadcast a colored and formatted message to a {@link Server}.
     * 
     * @param message The message to broadcast
     * @param args    The format arguments, if any
     */
    public static void broadcastMessage(@NotNull String message, @Nullable String... args) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(
                    new TextComponent(Translation.translateColors("&", String.format(message, (Object) args))));
        }
    }

    /**
     * Send a title to all online players
     * 
     * @param title The {@link com.destroystokyo.paper.Title} to send
     */
    public static void broadcastTitle(@NotNull Title title) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendTitle(title);
        }
    }
}
