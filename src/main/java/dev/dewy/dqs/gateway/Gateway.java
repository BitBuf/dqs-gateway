package dev.dewy.dqs.gateway;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.dewy.dqs.gateway.commands.discord.publicfacing.*;
import dev.dewy.dqs.gateway.commands.game.AuthCommand;
import dev.dewy.dqs.gateway.commands.game.ForgetCommand;
import dev.dewy.dqs.gateway.utils.GatewayConfig;
import dev.dewy.dqs.gateway.utils.SessionHandler;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Gateway extends Plugin implements Listener
{
    public static final String VERSION = "3.2.1";

    public static Gateway INSTANCE;

    public static JDA DISCORD;

    public static HashMap<String, UUID> authTokenMap = new HashMap<>();
    public static HashMap<String, ServerInfo> serverMap = new HashMap<>();

    public static SessionHandler sessionHandler = new SessionHandler("sessions.yml");
    public static ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(4);

    @Override
    public void onEnable()
    {
        INSTANCE = this;

        this.getProxy().getPluginManager().registerCommand(this, new AuthCommand());
        this.getProxy().getPluginManager().registerCommand(this, new ForgetCommand());

        this.getProxy().getPluginManager().registerListener(this, this);

        try
        {
            GatewayConfig.loadConfig(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        CommandClientBuilder commandClient = new CommandClientBuilder();

        commandClient.setPrefix("&");
        commandClient.setActivity(Activity.playing("for you!"));
        commandClient.setOwnerId("326039530971070474");

        commandClient.setHelpWord("EFFFFFFFFFFFFFFFFFFFFFFFWEFCWECFEWCVEWVBIWEIVJM");

        commandClient.addCommand(new dev.dewy.dqs.gateway.commands.discord.AuthCommand());

        commandClient.addCommands(
                new AboutCommand(),
                new BuyCommand(),
                new CreditsCommand(),
                new DocsCommand(),
                new FAQCommand(),
                new CopyrightCommand()
        );

        try
        {
            DISCORD = new JDABuilder(AccountType.BOT)
                    .setToken(GatewayConfig.getConfig().getString("discordToken"))
                    .addEventListeners(commandClient.build())
                    .build();
        }
        catch (LoginException e)
        {
            e.printStackTrace();

            System.exit(-1);
        }

        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getConfig().getServersCopy().entrySet())
        {
            if (!entry.getKey().startsWith("dqs_"))
            {
                continue;
            }

            String id = entry.getKey().replace("dqs_", "");
            serverMap.put(id, entry.getValue());
        }

        timerPool.scheduleAtFixedRate(() -> this.getProxy().getServerInfo("center").getPlayers().forEach(player -> player.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fUse the \247l/auth \247rcommand to get your DQS authentication code."))), 10L, 10L, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPing(ProxyPingEvent event)
    {
        ServerPing.Players players = event.getResponse().getPlayers();
        players.setMax(ProxyServer.getInstance().getConfig().getServersCopy().size() - 1);

        ServerPing.Protocol dqsProtocol = event.getResponse().getVersion();
        dqsProtocol.setName("DQS Protocol (" + VERSION + ") 1.12.2");
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event)
    {
        ProxyServer.getInstance().setReconnectHandler(new ReconnectHandler()
        {
            @Override
            public ServerInfo getServer(ProxiedPlayer player)
            {
                String id = player.getUniqueId().toString();

                for (Map.Entry<String, ServerInfo> server : Gateway.INSTANCE.getProxy().getServersCopy().entrySet())
                {
                    if (Gateway.sessionHandler.isUserCached(server.getKey(), id))
                    {
                        ServerInfo info = server.getValue();
                        CountDownLatch latch = new CountDownLatch(1);

                        final boolean[] up = { true };

                        info.ping(((result, error) ->
                        {
                            if (error != null)
                            {
                                up[0] = false;
                            }

                            latch.countDown();
                        }));

                        try
                        {
                            latch.await(10L, TimeUnit.SECONDS);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                        if (up[0])
                        {
                            return server.getValue();
                        }
                    }
                }

                new Thread(() ->
                {
                    try
                    {
                        Thread.sleep(6000L);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }).start();

                return ProxyServer.getInstance().getConfig().getServersCopy().get("center");
            }

            @Override
            public void setServer(ProxiedPlayer player)
            {

            }

            @Override
            public void save()
            {

            }

            @Override
            public void close()
            {

            }
        });
    }

    public boolean authorizeCode(String code, String channelId, String altCode)
    {
        String serverName = channelId;

        if (!altCode.equals("1"))
        {
            serverName = serverName + "_" + altCode;
        }

        if (!authTokenMap.containsKey(code))
        {
            Objects.requireNonNull(DISCORD.getTextChannelById(channelId)).sendMessage(new EmbedBuilder()
                    .setTitle("**DQS** - Invalid Authentication Code")
                    .setDescription("You've entered an invalid authentication code. Please join `dqs.dewy.dev` and use the `/auth` command to get one.\n\nTake a look [here](https://dqs.dewy.dev/features) for documentation and further assistance.")
                    .setColor(new Color(15221016)) // 10144497
                    .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                    .build()).queue();

            return false;
        }

        if (this.getProxy().getPlayer(authTokenMap.get(code)) == null)
        {
            Objects.requireNonNull(DISCORD.getTextChannelById(channelId)).sendMessage(new EmbedBuilder()
                    .setTitle("**DQS** - Invalid Authentication Circumstance")
                    .setDescription("You must be connected to `dqs.dewy.dev` to use the authentication commands.\n\nTake a look [here](https://dqs.dewy.dev/features) for documentation and furtherassistance.")
                    .setColor(new Color(15221016)) // 10144497
                    .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                    .build()).queue();

            return false;
        }

        ProxiedPlayer player = this.getProxy().getPlayer(authTokenMap.get(code));

        Objects.requireNonNull(DISCORD.getTextChannelById(channelId)).sendMessage(new EmbedBuilder()
                .setTitle("**DQS** - Negotiating Connection")
                .setDescription("Negotiating a connection to your DQS instance. Hang tight!")
                .setColor(new Color(10144497))
                .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                .build()).queue();

        player.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fNegotiating connection to your DQS instance..."));

        ServerInfo info = serverMap.get(serverName);

        if (info == null)
        {
            player.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247cYour DQS instance could not be found. Contact Dewy if you believe this is in error."));

            return false;
        }

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] vibeCheck = {true};

        info.ping((result, error) ->
        {
            if (error != null)
            {
                System.out.println(error);

                vibeCheck[0] = false;
            }

            latch.countDown();
        });

        try
        {
            latch.await(10L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (vibeCheck[0])
        {
            player.connect(info);

            Gateway.sessionHandler.cacheUser(info.getName(), player.getUniqueId().toString());

            return true;
        }

        player.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fA critical error has occurred connecting to your instance. Contact Dewy."));

        return false;
    }
}
