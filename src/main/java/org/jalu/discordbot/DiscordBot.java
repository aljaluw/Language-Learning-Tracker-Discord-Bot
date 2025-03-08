package org.jalu.discordbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        String token = System.getenv("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Discord bot token is missing! Set the DISCORD_BOT_TOKEN environment variable.");
        }

        JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Tracking Books"))
                .addEventListeners(new CommandListener())
                .build();
    }
}
