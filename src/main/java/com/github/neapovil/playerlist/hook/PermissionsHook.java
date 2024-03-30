package com.github.neapovil.playerlist.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.neapovil.permissions.event.PlayerPermissionsChangeEvent;
import com.github.neapovil.playerlist.PlayerList;

public final class PermissionsHook implements Listener
{
    private final PlayerList plugin = PlayerList.instance();

    @EventHandler
    private void onPlayerPermissionsChange(PlayerPermissionsChangeEvent event)
    {
        event.player().ifPresent(player -> plugin.hidePlayers(player, !player.hasPermission("playerlist.viewHiddenPlayers")));
    }
}
