package com.github.neapovil.playerlist;

import java.io.File;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FileConfig;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public final class PlayerList extends JavaPlugin implements Listener
{
    private static PlayerList instance;
    private FileConfig config;

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        this.config = FileConfig.builder(new File(this.getDataFolder(), "config.json"))
                .autoreload()
                .autosave()
                .build();
        this.config.load();

        this.getServer().getPluginManager().registerEvents(this, this);

        new CommandAPICommand("tablist")
                .withPermission("playerlist.command")
                .withArguments(new MultiLiteralArgument("header", "footer"))
                .withArguments(new GreedyStringArgument("text"))
                .executes((sender, args) -> {
                    final String setting = (String) args[0];
                    final String text = (String) args[1];

                    this.config.set("config." + setting, text);

                    sender.sendMessage("Changed " + setting + " message to: " + text);
                })
                .register();

        new CommandAPICommand("tablist")
                .withPermission("playerlist.command")
                .withArguments(new LiteralArgument("enabled"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args[0];

                    this.config.set("config.enabled", bool);

                    sender.sendMessage("Playerlist status changed to: " + bool);
                })
                .register();
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
        if (!((boolean) this.config.get("config.enabled")))
        {
            return;
        }

        final String header = ChatColor.translateAlternateColorCodes('&', this.config.get("config.header"));
        final String footer = ChatColor.translateAlternateColorCodes('&', this.config.get("config.footer"));

        event.getPlayer().sendPlayerListHeaderAndFooter(Component.text(header), Component.text(footer));
    }
}
