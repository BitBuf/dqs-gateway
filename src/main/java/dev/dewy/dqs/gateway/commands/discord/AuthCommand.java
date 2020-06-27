package dev.dewy.dqs.gateway.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.dewy.dqs.gateway.Gateway;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Objects;

public class AuthCommand extends Command
{
    public AuthCommand()
    {
        this.name = "auth";
        this.aliases = new String[] {"authorize", "authorise", "verify", "authenticate"};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if (event.getTextChannel().getParent() != null && event.getTextChannel().getParent().getName().equalsIgnoreCase("dqs"))
        {
            try
            {
                String[] args = event.getArgs().split("\\s+");

                if (args.length != 2)
                {
                    event.reply(new EmbedBuilder()
                            .setTitle("**DQS** - Invalid Command Arguments")
                            .setDescription("Please enter valid arguments for this command. Take a look [here](https://dqs.dewy.dev/features) for documentation and assistance.")
                            .setColor(new Color(15221016))
                            .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                            .build());

                    return;
                }

                if (args[0].length() != 6)
                {
                    event.reply(new EmbedBuilder()
                            .setTitle("**DQS** - Invalid Command Arguments")
                            .setDescription("Please enter a **six-digit** authentication code.. Take a look [here](https://dqs.dewy.dev/features) for documentation and assistance.")
                            .setColor(new Color(15221016))
                            .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                            .build());

                    return;
                }

                String authCode = args[0];
                String altCode = args[1];

                if (Gateway.INSTANCE.authorizeCode(authCode, event.getTextChannel().getId(), altCode))
                {
                    Gateway.authTokenMap.remove(authCode);
                }
            }
            catch (Throwable t)
            {
                t.printStackTrace();

                event.reply(new EmbedBuilder()
                        .setTitle("**DQS** - Error")
                        .setDescription("An exception occurred whilst executing this command. Debug information has been sent to Dewy to be fixed in following updates. Sorry about any inconvenience!")
                        .setColor(new Color(15221016))
                        .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                        .build());

                Objects.requireNonNull(event.getJDA().getUserById("326039530971070474")).openPrivateChannel().queue((privateChannel ->
                        privateChannel.sendMessage(new EmbedBuilder()
                                .setTitle("**DQS** - Error Report (" + Objects.requireNonNull(event.getAuthor().getName()) + ")")
                                .setDescription("A " + t.getClass().getSimpleName() + " was thrown during the execution of an auth command.\n\n**Cause:**\n\n```" + t.getMessage() + "```")
                                .setColor(new Color(15221016))
                                .setAuthor("DQS " + Gateway.VERSION, null, "https://i.imgur.com/pcSOd3K.png")
                                .build()).queue()));
            }
        }
    }
}
