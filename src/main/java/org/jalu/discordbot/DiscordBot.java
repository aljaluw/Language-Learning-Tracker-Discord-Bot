package org.jalu.discordbot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException, InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Discord bot token is missing! Set the DISCORD_BOT_TOKEN environment variable.");
        }

        JDA jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Tracking Books"))
                .addEventListeners(new CommandListener())
                .build().awaitReady();

        Guild guild = jda.getGuildById("968630580331810816");

        if (guild != null) {
            guild.upsertCommand("log", "logs immersion progress")
                    .addOption(OptionType.STRING, "media-type", "type of media being logged", true)
                    .addOption(OptionType.STRING, "title", "title of media being logged", true)
                    .addOption(OptionType.INTEGER, "amount", "amount of media being logged", true)
                    .addOption(OptionType.STRING, "unit", "unit of amount", true)
                    .addOption(OptionType.STRING, "date", "date of log", false)
                    .addOption(OptionType.STRING, "comment", "any comment?", false)
                    .queue();

        }
    }
}
