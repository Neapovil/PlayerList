package com.github.neapovil.playerlist.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.neapovil.playerlist.PlayerList;

public final class Listener implements org.bukkit.event.Listener
{
    private final PlayerList plugin = PlayerList.instance();

    @EventHandler
    private void playerJoin(PlayerJoinEvent event)
    {
        if (plugin.config().enabled)
        {
            plugin.sendPlayerList(event.getPlayer());
        }
    }
}
