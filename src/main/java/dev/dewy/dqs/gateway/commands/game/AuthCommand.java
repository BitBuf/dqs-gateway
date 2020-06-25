package dev.dewy.dqs.gateway.commands.game;

import dev.dewy.dqs.gateway.Gateway;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Random;

public class AuthCommand extends Command
{
    public AuthCommand()
    {
        super("auth");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender instanceof ProxiedPlayer))
        {
            sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247cYou must be a proxied player to run this command."));

            return;
        }

        if (!((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase("center"))
        {
            sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247cYou must be connected to the DQS gateway server to run this command."));

            return;
        }

        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; ++i)
        {
            code.append(random.nextInt(9));
        }

        Gateway.authTokenMap.put(code.toString(), ((ProxiedPlayer) sender).getUniqueId());

        sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fHere's your six-digit code: \247l" + code.toString() + "\247r."));
        sender.sendMessage(new TextComponent("\2477[\247b\247lDQS\247r\2477] \247fUse the &auth command in your \247lchannel\247r to authenticate."));
    }
}
