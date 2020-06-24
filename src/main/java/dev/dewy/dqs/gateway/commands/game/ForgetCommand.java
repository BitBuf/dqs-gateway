package dev.dewy.dqs.gateway.commands.game;

import dev.dewy.dqs.gateway.Gateway;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ForgetCommand extends Command
{
    public ForgetCommand()
    {
        super("forget");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender instanceof ProxiedPlayer))
        {
            sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247cYou must be a proxied player to run this command."));

            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (player.getServer().getInfo().getName().equalsIgnoreCase("center"))
        {
            sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247cYou must be connected to a DQS instance to run this command."));

            return;
        }

        Gateway.sessionHandler.purgeUserCache(player.getServer().getInfo().getName());
        player.disconnect(new TextComponent("\2477[\247b\247lDQS Authentication\247r\2477]\n\n\247fYou have signed out of the DQS authentication service. You'll need to re-auth to connect to your instance again."));
    }
}
