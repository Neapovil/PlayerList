package com.github.neapovil.playerlist;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.neapovil.core.Core;
import com.github.neapovil.playerlist.listener.Listener;
import com.github.neapovil.playerlist.resource.ConfigResource;
import com.github.neapovil.playerlist.runnable.LatencyRunnable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import io.papermc.paper.util.MCUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class PlayerList extends JavaPlugin
{
    private static PlayerList instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ConfigResource configResource;
    public final Path configPath = this.getDataFolder().toPath().resolve("config.json");

    @Override
    public void onEnable()
    {
        instance = this;

        this.load();

        if (this.getServer().getPluginManager().getPlugin("Latency") != null)
        {
            new LatencyRunnable().runTaskTimer(this, 0, 20);
        }

        if (this.getServer().getPluginManager().getPlugin("Permissions") != null)
        {
            this.getServer().getPluginManager().registerEvents(new com.github.neapovil.playerlist.hook.PermissionsHook(), this);
        }

        if (this.getServer().getPluginManager().getPlugin("PermissionsYaml") != null)
        {
            this.getServer().getPluginManager().registerEvents(new com.github.neapovil.playerlist.hook.PermissionsYamlHook(), this);
        }

        this.getServer().getPluginManager().registerEvents(new Listener(), this);

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new MultiLiteralArgument("setting", "header", "footer"))
                .withArguments(new GreedyStringArgument("text").replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    final String option = (String) info.previousArgs().get(0);

                    if (option.equals("header"))
                    {
                        return new String[] { this.configResource.header };
                    }

                    if (option.equals("footer"))
                    {
                        return new String[] { this.configResource.footer };
                    }

                    return new String[] {};
                })))
                .executes((sender, args) -> {
                    final String setting = (String) args.get("setting");
                    final String text = (String) args.get("text");

                    if (setting.equals("header"))
                    {
                        this.configResource.header = text;
                    }

                    if (setting.equals("footer"))
                    {
                        this.configResource.footer = text;
                    }

                    this.save().whenCompleteAsync((result, ex) -> {
                        if (ex == null)
                        {
                            sender.sendRichMessage("Changed " + setting + " message to:\n" + text);

                            if (this.configResource.enabled)
                            {
                                for (Player player : this.getServer().getOnlinePlayers())
                                {
                                    this.sendPlayerList(player);
                                }
                            }
                        }
                    }, MCUtil.MAIN_EXECUTOR);
                })
                .register();

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new LiteralArgument("enabled"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args.get("bool");

                    this.configResource.enabled = bool;

                    this.save().whenCompleteAsync((result, ex) -> {
                        if (ex == null)
                        {
                            sender.sendMessage("Playerlist status changed to: " + bool);

                            for (Player player : this.getServer().getOnlinePlayers())
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
                    }, MCUtil.MAIN_EXECUTOR);
                })
                .register();

        new CommandAPICommand("playerlist")
                .withPermission("playerlist.command")
                .withArguments(new LiteralArgument("hidePlayers"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args.get("bool");

                    this.configResource.hidePlayers = bool;

                    this.save().whenCompleteAsync((result, ex) -> {
                        if (ex == null)
                        {
                            sender.sendMessage("Hide players status changed to: " + bool);

                            for (Player i : this.getServer().getOnlinePlayers())
                            {
                                this.hidePlayers(i, bool);
                            }
                        }
                    }, MCUtil.MAIN_EXECUTOR);
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

    public ConfigResource config()
    {
        return this.configResource;
    }

    public void sendPlayerList(Player player)
    {
        final String header = this.configResource.header;
        final String footer = this.configResource.footer;

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

    private CompletableFuture<String> load()
    {
        final Core core = Core.instance();
        return core.loadResource(this, this.configPath).whenCompleteAsync((result, ex) -> {
            if (result != null)
            {
                this.configResource = this.gson.fromJson(result, ConfigResource.class);
            }
        }, MCUtil.MAIN_EXECUTOR);
    }

    private CompletableFuture<Void> save()
    {
        final Core core = Core.instance();
        final String string = gson.toJson(this.configResource);
        return core.saveResource(this.configPath, string);
    }
}
