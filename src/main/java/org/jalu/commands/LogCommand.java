package org.jalu.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;
import org.jalu.dto.LogImmersionRequestDto;
import org.jalu.enums.MediaType;
import org.jalu.enums.MediaUnit;
import org.jetbrains.annotations.NotNull;
import org.jalu.model.Book;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.io.IOException;
import java.util.List;

public class LogCommand extends ListenerAdapter {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_BASE_URL = "http://immersion-logger.gyb.my.id";

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("log") && event.getFocusedOption().getName().equals("unit")) {
            String mediaTypeString = event.getOption("media_type") != null ? event.getOption("media_type").getAsString() : null;

            if (mediaTypeString == null) {
                event.replyChoices(new Command.Choice("Select media type first", "none")).queue();
                return;
            }

            try {
                MediaType mediaType = MediaType.valueOf(mediaTypeString.toUpperCase());
                List<Command.Choice> choices = new ArrayList<>();

                for (MediaUnit unit : mediaType.getValidUnits()) {
                    choices.add(new Command.Choice(unit.getDisplayName(), unit.name()));
                }

                event.replyChoices(choices).queue();
            } catch (IllegalArgumentException e) {
                event.replyChoices(new Command.Choice("Invalid media type", "none")).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("log")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            OptionMapping mediaTypeMapping = event.getOption("media_type");
            String mediaType = mediaTypeMapping != null ? mediaTypeMapping.getAsString() : null;

            OptionMapping titleMapping = event.getOption("title");
            String title = titleMapping != null ? titleMapping.getAsString() : null;

            OptionMapping amountMapping = event.getOption("amount");
            Integer amount = amountMapping != null ? amountMapping.getAsInt() : null;

            OptionMapping unitMapping = event.getOption("unit");
            String unit = unitMapping != null ? unitMapping.getAsString() : null;

            OptionMapping dateMapping = event.getOption("date");
            LocalDate date = dateMapping != null ? LocalDate.parse(dateMapping.getAsString()) : LocalDate.now();

            OptionMapping commentMapping = event.getOption("comment");
            String comment = commentMapping != null ? commentMapping.getAsString() : "No comment";

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle("Logged immersion");
            embed.setColor(Color.CYAN);
            embed.setDescription("Details of log");
            assert title != null;
            embed.addField("Title", title, false);
            assert mediaType != null;
            embed.addField("Media Type", mediaType, false);
            embed.addField("Amount", String.valueOf(amount), false);
            assert unit != null;
            embed.addField("Unit", unit, false);
            embed.addField("Date", String.valueOf(date), false);
            embed.addField("Comment", comment, false);

            event.deferReply().queue();

            LogImmersionRequestDto dto = new LogImmersionRequestDto();
            dto.setMediaType(mediaType);
            dto.setTitle(title);
            dto.setAmount(amount);
            dto.setUnit(unit);
            dto.setDate(String.valueOf(date));
            dto.setComment(comment);

            logMedia(event, dto);
        }
    }

    private void logMedia(SlashCommandInteractionEvent event, LogImmersionRequestDto dto) {
        String logImmersionRequest;
        try {
            logImmersionRequest = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(logImmersionRequest, okhttp3.MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(API_BASE_URL + "/media/log")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getHook().editOriginal("Failed to log media: " + e.getMessage()).queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle("Successfully logged immersion details!");
                    embed.setColor(Color.CYAN);
                    embed.setDescription("Details of log");
                    embed.addField("Title", dto.getTitle(), false);
                    embed.addField("Media Type", dto.getMediaType(), false);
                    embed.addField("Amount", String.valueOf(dto.getAmount()), false);
                    embed.addField("Unit", dto.getUnit(), false);
                    embed.addField("Date", dto.getDate(), false);
                    embed.addField("Comment", dto.getComment(), false);

                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                } else {
                    event.getHook().editOriginal("Failed to log media: " + response.message()).queue();
                }
            }
        });
    }
}
