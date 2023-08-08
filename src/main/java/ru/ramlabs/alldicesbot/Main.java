package ru.ramlabs.alldicesbot;

import com.github.ram042.json.Json;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        var botToken = System.getenv("BOT_TOKEN");
        var webhookUrl = System.getenv("WEBHOOK_URL");
        var webhookToken = System.getenv("WEBHOOK_TOKEN");
        var bot = new Bot();

        var socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(),
                Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8888")));
        logger.info("Listening {}", socketAddress);
        HttpServer server = HttpServer.create(socketAddress, 50);

        server.createContext("/webhook", exchange -> {
            var token = exchange.getRequestHeaders().get("X-Telegram-Bot-Api-Secret-Token");
            if (token == null || token.size() != 1 || !token.get(0).equals(webhookToken)) {
                var msg = "UNAUTHORIZED";
                new PrintStream(exchange.getResponseBody()).print(msg);
                exchange.sendResponseHeaders(401, msg.length());
            }

            var request = new String(exchange.getRequestBody().readAllBytes());
            logger.info("Update {}", request);

            try {
                var sendMessage = bot.handleUpdate(Json.parse(request).getAsObject());
                if (sendMessage != null) {
                    var response = sendMessage.toString().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("content-type", "application/json");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                }
            } catch (Exception e) {
                logger.error("Cannot handle update", e);
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });

        server.start();
        Thread.yield();

        var request = HttpRequest.newBuilder()
                .uri(new URI("https://api.telegram.org/bot" + botToken + "/setWebhook"))
                .POST(HttpRequest.BodyPublishers.ofString(Json.object(
                        "url", webhookUrl,
                        "secret_token", webhookToken
                ).toString()))
                .header("content-type","application/json")
                .build();

        var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Updated webhook {}", response.body());

    }
}