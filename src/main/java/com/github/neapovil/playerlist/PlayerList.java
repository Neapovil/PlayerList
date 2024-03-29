package com.github.neapovil.playerlist;

import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.neapovil.playerlist.listener.Listener;
import com.github.neapovil.playerlist.resource.Config;
import com.github.neapovil.playerlist.runnable.LatencyRunnable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class PlayerList extends JavaPlugin
{
    private static PlayerList instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Config config;

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        try
        {
            this.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (this.getServer().getPluginManager().getPlugin("Latency") != null)
        {
            new LatencyRunnable().runTaskTimer(this, 0, 20);
        }

        this.getServer().getPluginManager().registerEvents(new Listener(), this);

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new MultiLiteralArgument("setting", "header", "footer"))
                .withArguments(new GreedyStringArgument("text").replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    final String option = (String) info.previousArgs().get(0);

                    if (option.equals("header"))
                    {
                        return new String[] { this.config.header };
                    }

                    if (option.equals("footer"))
                    {
                        return new String[] { this.config.footer };
                    }

                    return new String[] {};
                })))
                .executes((sender, args) -> {
                    final String setting = (String) args.get("setting");
                    final String text = (String) args.get("text");

                    if (setting.equals("header"))
                    {
                        this.config.header = text;
                    }

                    if (setting.equals("footer"))
                    {
                        this.config.footer = text;
                    }

                    try
                    {
                        this.save();

                        sender.sendRichMessage("Changed " + setting + " message to:\n" + text);

                        if (this.config.enabled)
                        {
                            for (Player player : this.getServer().getOnlinePlayers())
                            {
                                this.sendPlayerList(player);
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        sender.sendRichMessage("<red>Unable to edit text");
                        this.getLogger().severe(e.getMessage());
                    }
                })
                .register();

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new LiteralArgument("enabled"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args.get("bool");

                    this.config.enabled = bool;

                    try
                    {
                        this.save();

                        sender.sendMessage("Playerlist status changed to: " + bool);

                        for (Player player : this.getServer().getOnlinePlayers().toArray(Player[]::new))
                        {
                            if (bool)
                            {
                                this.sendPlayerList(player);
                            }
                            else
                            {
                                player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        sender.sendRichMessage("<red>Unable to change status");
                        this.getLogger().severe(e.getMessage());
                    }
                })
                .register();

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new LiteralArgument("hidePlayers"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args.get("bool");

                    this.config.hidePlayers = bool;

                    try
                    {
                        this.save();

                        sender.sendMessage("Hide players status changed to: " + bool);

                        for (Player i : this.getServer().getOnlinePlayers().toArray(Player[]::new))
                        {
                            this.hidePlayers(i, bool);
                        }
                    }
                    catch (IOException e)
                    {
                        sender.sendRichMessage("<red>Unable to save status");
                        this.getLogger().severe(e.getMessage());
                    }
                })
                .register();
    }

    @Override
    public void onDisable()
    {
    }

    public static PlayerList instance()
    {
        return instance;
    }

    public Config config()
    {
        return this.config;
    }

    public void sendPlayerList(Player player)
    {
        final String header = this.config.header;
        final String footer = this.config.footer;

        final boolean papi = this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        final Component headercomponent = this.miniMessage.deserialize(header,
                papi ? com.github.neapovil.playerlist.hook.PlaceholderAPIHook.applyPlaceholders(player) : TagResolver.standard());
        final Component footercomponent = this.miniMessage.deserialize(footer,
                papi ? com.github.neapovil.playerlist.hook.PlaceholderAPIHook.applyPlaceholders(player) : TagResolver.standard());

        player.sendPlayerListHeaderAndFooter(headercomponent, footercomponent);
    }

    public void hidePlayers(Player player, boolean hide)
    {
        if (player.hasPermission("playerlist.viewHiddenPlayers"))
        {
            return;
        }

        for (Player i : this.getServer().getOnlinePlayers().toArray(Player[]::new))
        {
            if (!player.isOnline())
            {
                break;
            }

            if (i.getUniqueId().equals(player.getUniqueId()))
            {
                continue;
            }

            if (!i.isOnline())
            {
                continue;
            }

            if (hide)
            {
                player.hidePlayer(this, i);
            }
            else
            {
                player.showPlayer(this, i);
            }
        }
    }

    private void save() throws IOException
    {
        final String string = gson.toJson(this.config);
        Files.write(this.getDataFolder().toPath().resolve("config.json"), string.getBytes());
    }

    private void load() throws IOException
    {
        final String string = Files.readString(this.getDataFolder().toPath().resolve("config.json"));
        this.config = gson.fromJson(string, Config.class);
    }
}
