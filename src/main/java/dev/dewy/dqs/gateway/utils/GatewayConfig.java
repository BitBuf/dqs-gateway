package dev.dewy.dqs.gateway.utils;

import dev.dewy.dqs.gateway.Gateway;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class GatewayConfig
{
    private static Configuration INSTANCE;

    public static Configuration getConfig()
    {
        if (INSTANCE != null)
        {
            return INSTANCE;
        }

        try
        {
            return loadConfig(Gateway.INSTANCE);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        throw new IllegalStateException("The plugin configuration file is broken. Fix this, n o w.");
    }

    public static Configuration loadConfig(Plugin plugin) throws IOException
    {
        createConfig(plugin);
        INSTANCE = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "gateway.yml"));

        return INSTANCE;
    }

    private static void createConfig(Plugin plugin)
    {
        File file;

        if (!plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdir();
        }

        if (!(file = new File(plugin.getDataFolder(), "gateway.yml")).exists())
        {
            try
            {
                try (InputStream in = plugin.getResourceAsStream("default.yml"))
                {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
