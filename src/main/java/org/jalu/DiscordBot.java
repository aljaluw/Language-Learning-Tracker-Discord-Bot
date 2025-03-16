package org.jalu;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jalu.commands.ExportCommand;
import org.jalu.commands.LogCommand;
import org.jalu.enums.MediaType;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {
    ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) throws LoginException, InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Discord bot token is missing! Set the DISCORD_BOT_TOKEN environment variable.");
        }

        JDA jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Tracking immersion logs"))
                .addEventListeners(new LogCommand(), new ExportCommand())
                .build().awaitReady();

        Guild guild = jda.getGuildById("968630580331810816");
        OptionData mediaTypeOption = new OptionData(OptionType.STRING, "media_type", "media type of log", true);
        OptionData unitTypeOption = new OptionData(OptionType.STRING, "unit", "unit of amount", true)
                .setAutoComplete(true);

        for (MediaType type : MediaType.values()) {
            mediaTypeOption.addChoice(type.getDisplayName(), type.name());
        }

        if (guild != null) {
            guild.updateCommands().addCommands(
                    Commands.slash("log", "Log immersion progress")
                            .addOptions(
                                    mediaTypeOption,
                                    new OptionData(OptionType.STRING, "title", "title of media being logged", true),
                                    new OptionData(OptionType.STRING, "amount", "amount of media being logged", true),
                                    unitTypeOption,
                                    new OptionData(OptionType.STRING, "date", "date of log", false),
                                    new OptionData(OptionType.STRING, "comment", "comment", false)
                            ),
                    Commands.slash("export", "export immersion logs")
            ).queue();
        }
    }
}
