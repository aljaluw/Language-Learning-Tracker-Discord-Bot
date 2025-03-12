package org.jalu.discordbot;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jalu.model.Book;

import java.awt.*;
import java.io.IOException;

public class CommandListener extends ListenerAdapter {
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final String API_BASE_URL = "http://localhost:8080/books";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("log")) {
            OptionMapping mediaTypeOption = event.getOption("media-type");
            OptionMapping titleOption = event.getOption("title");
            OptionMapping amountOption = event.getOption("amount");
            OptionMapping unitOption = event.getOption("unit");
            OptionMapping dateOption = event.getOption("date");
            OptionMapping commentOption = event.getOption("comment");
            assert mediaTypeOption != null;
            event.reply("media type: "+mediaTypeOption.getAsString()).queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String[] args = message.split(" ");

        if (message.equalsIgnoreCase("!hello")) {
            event.getChannel().sendMessage("Hello, "+ event.getAuthor().getAsMention()+ "!").queue();
        }

        if (message.equalsIgnoreCase("!books")) {
            getAllBooks(event);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("!book")) {
            getBookById(event, args[1]);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("!progress")) {
            updateReadingProgress(event, args[1], args[2]);
        } else if (args.length == 4 && args[0].equalsIgnoreCase("!characters-read")) {
            getTotalCharactersRead(event, args[1], args[2], args[3]);
        }
    }

    private void logMedia(SlashCommandInteractionEvent event) {
        Request request = new Request.Builder()
                .url(API_BASE_URL)
                .build();

    }

    private void getAllBooks(MessageReceivedEvent event) {
        Request request = new Request.Builder()
                .url(API_BASE_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getChannel().sendMessage("❌ Failed to fetch books: " + e.getMessage()).queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    event.getChannel().sendMessage("❌ Error fetching books: " + response.message()).queue();
                    return;
                }

                assert response.body() != null;
                String jsonResponse = response.body().string();
                Gson gson = new Gson();
                Book[] books = gson.fromJson(jsonResponse, Book[].class);

                if (books.length == 0) {
                    event.getChannel().sendMessage("📚 No books found!").queue();
                    return;
                }
                EmbedBuilder embed = new EmbedBuilder();

                for (Book book : books) {
                    embed.setTitle("Book List");
                    embed.setColor(Color.BLUE);
                    embed.setDescription("Here are the books you've been tracking:");
                    embed.addField("\uD83D\uDCD6 Title",book.getTitle(), false);
                    embed.addField("🖊️ Author", book.getAuthor() != null ? book.getAuthor() : "Unknown", false);
                    embed.addField("📚 Genre", book.getGenre(), false);
                    embed.addField("⭐ Difficulty", book.getDifficultyRating(), false);
                    embed.setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
                }

                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
        });
    }


    private void getBookById(MessageReceivedEvent event, String bookId) {
        Request request = new Request.Builder()
                .url(API_BASE_URL + "/" + bookId)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getChannel().sendMessage("Failed to fetch book: " + e.getMessage()).queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    event.getChannel().sendMessage("Error fetching book: " + response.message()).queue();
                    return;
                }
                assert response.body() != null;
                event.getChannel().sendMessage(response.body().string()).queue();
            }
        });
    }

    private void updateReadingProgress(MessageReceivedEvent event, String bookId, String charactersRead) {
        RequestBody formBody = new FormBody.Builder()
                .add("bookId", bookId)
                .add("charactersRead", charactersRead)
                .build();

        Request request = new Request.Builder()
                .url(API_BASE_URL + "/update-progress")
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getChannel().sendMessage("Failed to update progress: " + e.getMessage()).queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                event.getChannel().sendMessage(response.body().string()).queue();
            }
        });
    }

    private void getTotalCharactersRead(MessageReceivedEvent event, String bookId, String startDate, String endDate) {
        Request request = new Request.Builder()
                .url(API_BASE_URL + "/" + bookId + "/characters-read?startDate=" + startDate + "&endDate=" + endDate)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                event.getChannel().sendMessage("Failed to get total characters read: " + e.getMessage()).queue();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                event.getChannel().sendMessage("Total characters read: " + response.body().string()).queue();
            }
        });
    }
}
