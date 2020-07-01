package dev.dewy.dqs.gateway.commands.discord.publicfacing;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.dewy.dqs.gateway.Gateway;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class AboutCommand extends Command
{
    public AboutCommand()
    {
        this.name = "about";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply(new EmbedBuilder()
                .setTitle("**DQS** - About")
                .setDescription("**About DQS:** https://dqs.dewy.dev/about")
                .setColor(new Color(10144497))
                .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                .build());
    }
}
