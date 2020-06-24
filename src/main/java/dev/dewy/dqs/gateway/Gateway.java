package dev.dewy.dqs.gateway;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.dewy.dqs.gateway.commands.game.AuthCommand;
import dev.dewy.dqs.gateway.commands.game.ForgetCommand;
import dev.dewy.dqs.gateway.utils.GatewayConfig;
import dev.dewy.dqs.gateway.utils.SessionHandler;
import net.dv8tion.jda.api.AccountType;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Gateway extends Plugin implements Listener
{
    public static final String VERSION = "3.0.0";

    public static Gateway INSTANCE;

    public static JDA DISCORD;

    public static HashMap<String, UUID> authTokenMap = new HashMap<>();
    public static HashMap<String, ServerInfo> serverMap = new HashMap<>();

    public static SessionHandler sessionHandler = new SessionHandler("DQSSessions.yml");
    public static ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(4);

    public static String altCode;

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

        timerPool.scheduleAtFixedRate(() ->
        {
            this.getProxy().getServerInfo("center").getPlayers().forEach(player ->
            {
                player.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fUse the \247l/auth \247rcommand to get your DQS authentication code."));
            });
        }, 10L, 10L, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPing(ProxyPingEvent event)
    {
        ServerPing.Players players = event.getResponse().getPlayers();
        players.setMax(ProxyServer.getInstance().getConfig().getServersCopy().size() - 1);

        ServerPing.Protocol dqsProtocol = event.getResponse().getVersion();
        dqsProtocol.setName("DQS Protocol (3.0.0) 1.12.2");
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
}
