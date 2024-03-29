package com.github.neapovil.playerlist.runnable;

import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.neapovil.playerlist.PlayerList;

import net.kyori.adventure.text.Component;

public final class LatencyRunnable extends BukkitRunnable
{
    private final PlayerList plugin = PlayerList.instance();

    @Override
    public void run()
    {
        for (Player i : plugin.getServer().getOnlinePlayers().toArray(Player[]::new))
        {
            final int ping = ((CraftPlayer) i).getPing();
            final Component component = i.name()
                    .appendSpace()
                    .append(Component.text(ping));
            i.playerListName(component);
        }
    }
}
