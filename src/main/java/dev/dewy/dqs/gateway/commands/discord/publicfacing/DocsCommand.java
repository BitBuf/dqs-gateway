package dev.dewy.dqs.gateway.commands.discord.publicfacing;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.dewy.dqs.gateway.Gateway;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class DocsCommand extends Command
{
    public DocsCommand()
    {
        this.name = "docs";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply(new EmbedBuilder()
                .setTitle("**DQS** - Documentation")
                .setDescription("**Documentation:** https://dqs.dewy.dev/features")
                .setColor(new Color(10144497))
                .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                .build());
    }
}
