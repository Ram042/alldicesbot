package ru.ramlabs.alldicesbot;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@ImportRuntimeHints(Hints.class)
@RestController
@Slf4j
public class App {

    public static void main(String[] args) {
        var app = new SpringApplication(App.class);
        app.setDefaultProperties(Map.of(
                "server.port",
                Optional.ofNullable(System.getenv("PORT")).orElse("8888")
        ));
        app.run(args);
    }

    private final Bot bot;
    private final String webhookToken;
    private final String botToken;
    private final String webhookUrl;
    private final ObjectMapper objectMapper;

    public App(
            Bot bot,
            @Value("${WEBHOOK_TOKEN}") String webhookToken,
            @Value("${BOT_TOKEN}") String botToken,
            @Value("${WEBHOOK_URL}") String webhookUrl,
            ObjectMapper objectMapper
    ) {
        this.bot = bot;
        this.webhookToken = webhookToken;
        this.botToken = botToken;
        this.webhookUrl = webhookUrl;
        this.objectMapper = objectMapper;
        initHook();
    }

    public record SetWebhookParams(
            String url,
            String secret_token
    ) {

    }

    private void initHook() {
        CompletableFuture.runAsync(() -> {
            try {
                var request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.telegram.org/bot" + botToken + "/setWebhook"))
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(
                                new SetWebhookParams(webhookUrl, webhookToken
                                ))))
                        .header("content-type", "application/json")
                        .build();

                var response = HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());

                log.info("Updated webhook {}", response.body());
            } catch (Exception e) {
                log.warn("Cannot update webhook", e);
            }
        });
    }

    @PostMapping(
            path = "/webhook",
            consumes = "application/json",
            produces = "application/json"
    )
    @JsonRawValue
    public String webhook(@RequestBody Update update, HttpServletRequest request) {
        if (!webhookToken.equals(request.getHeader("X-Telegram-Bot-Api-Secret-Token"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad token");
        }
        log.info("Update: {}", update);
        var sendMessage = bot.handleUpdate(update);

        if (sendMessage != null) {
            return sendMessage.toWebhookResponse();
        } else {
            return null;
        }
    }
}