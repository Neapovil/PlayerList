package com.github.neapovil.playerlist.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.neapovil.permissionsyaml.event.PlayerPermissionsChangeEvent;
import com.github.neapovil.playerlist.PlayerList;

public final class PermissionsYamlHook implements Listener
{
    @EventHandler
    private void onPlayerPermissionsChange(PlayerPermissionsChangeEvent event)
    {
        final PlayerList plugin = PlayerList.instance();
        event.player().ifPresent(player -> plugin.hidePlayers(player, !player.hasPermission("playerlist.viewHiddenPlayers")));
    }
}
