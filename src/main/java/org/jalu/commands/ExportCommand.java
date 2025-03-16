package org.jalu.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExportCommand extends ListenerAdapter {
    private static final String API_BASE_URL = "http://immersion-logger.gyb.my.id";
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("export")) {
            event.deferReply().queue(); // Acknowledge the command first
            exportLogs(event);
        }
    }

    private void exportLogs(SlashCommandInteractionEvent event) {
        Request request = new Request.Builder()
                .url(API_BASE_URL+ "/media/all-logs")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getHook().editOriginal("Failed to export logs").queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    event.getHook().editOriginal("Failed to export logs").queue();
                    return;
                }
                assert response.body() != null;
                String immersionLogs = response.body().string();

                JsonNode jsonArray = objectMapper.readTree(immersionLogs);

                File file = new File("output.txt");
                try (FileWriter writer = new FileWriter(file)) {
                    for (JsonNode jsonNode : jsonArray) {
                        String date = jsonNode.get("date").asText();
                        String mediaType = jsonNode.get("mediaType").asText();
                        String title = jsonNode.get("title").asText();
                        String amount = jsonNode.get("amount").asText();
                        String unit = jsonNode.get("unit").asText();

                        String formattedText = date + " | " + mediaType + " , " + title + " , " + amount + " , " + unit;
                        writer.write(formattedText + "\n");
                    }
                } catch (IOException e) {
                    event.getHook().editOriginal("Failed to write logs to file").queue();
                    return;
                }

                // Send the file to Discord
                event.getHook().sendFiles(FileUpload.fromData(file)).queue();
            }
        });
    }
}
