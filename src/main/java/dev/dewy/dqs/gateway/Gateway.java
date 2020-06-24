package dev.dewy.dqs.gateway;

import dev.dewy.dqs.gateway.utils.SessionHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Gateway extends Plugin
{
    public static Gateway INSTANCE;

    public static HashMap<String, UUID> authTokenMap = new HashMap<>();
    public static HashMap<String, ServerInfo> serverMap = new HashMap<>();

    public static SessionHandler sessionHandler = new SessionHandler("DQSSessions.yml");
    public static ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(4);

    public static String altCode;
}
