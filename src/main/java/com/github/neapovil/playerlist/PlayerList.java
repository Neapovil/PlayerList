package com.github.neapovil.playerlist;

import java.io.File;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FileConfig;

public final class PlayerList extends JavaPlugin implements Listener
{
    private static PlayerList instance;
    private FileConfig config;

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.toml", false);

        this.config = FileConfig.builder(new File(this.getDataFolder(), "config.toml"))
                .autoreload()
                .autosave()
                .sync()
                .build();
        this.config.load();

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    public static PlayerList getInstance()
    {
        return instance;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event)
    {
        event.getPlayer().setPlayerListHeaderFooter(this.config.get("general.header"), this.config.get("general.footer"));
    }
}
