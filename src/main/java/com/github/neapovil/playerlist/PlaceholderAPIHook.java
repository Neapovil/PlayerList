package com.github.neapovil.playerlist;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIHook
{
    public static String applyPlaceholders(Player player, String text)
    {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
