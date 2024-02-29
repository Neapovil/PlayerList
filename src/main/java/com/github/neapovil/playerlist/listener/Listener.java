package com.github.neapovil.playerlist.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.neapovil.playerlist.PlayerList;

import net.kyori.adventure.text.Component;

public final class Listener implements org.bukkit.event.Listener
{
    private final PlayerList plugin = PlayerList.instance();

    @EventHandler
    private void playerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        if (plugin.config().enabled)
        {
            plugin.sendPlayerList(player);
        }

        if (plugin.config().hidePlayers)
        {
            final Component component = event.joinMessage();
            event.joinMessage(null);

            if (!player.hasPermission("playerlist.viewHiddenPlayers"))
            {
                player.sendMessage(component);
            }

            plugin.getServer().broadcast(component, "playerlist.viewHiddenPlayers");

            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.hidePlayers(player, true));

            for (Player i : plugin.getServer().getOnlinePlayers().toArray(Player[]::new))
            {
                plugin.hidePlayers(i, true);
            }
        }
    }

    @EventHandler
    private void playerQuit(PlayerQuitEvent event)
    {
        if (plugin.config().hidePlayers)
        {
            final Component component = event.quitMessage();
            event.quitMessage(null);

            plugin.getServer().broadcast(component, "playerlist.viewHiddenPlayers");
        }
    }
}
