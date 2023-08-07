package ru.ramlabs.alldicesbot;

import com.github.ram042.json.Json;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        var tgBot = new TelegramBot(System.getenv("BOT_TOKEN"));
        var tgUrl = System.getenv("WEBHOOK_URL");
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
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            }

        });

        server.start();
        Thread.yield();

        var setWebhookResult = tgBot.execute(new SetWebhook().url(tgUrl).secretToken(webhookToken));

        if (!setWebhookResult.isOk()) {
            logger.error("Cannot update webhook: {}", setWebhookResult);
        } else {
            logger.info("Updated webhook");
        }

    }
}