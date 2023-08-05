package ru.ramlabs.alldicesbot;

import com.google.gson.Gson;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        var tgBot = new TelegramBot(System.getenv("BOT_TOKEN"));
        var tgUrl = System.getenv("WEBHOOK_URL");
        var webhookToken = System.getenv("WEBHOOK_TOKEN");
        var bot = new Bot(tgBot);

        Javalin.create(javalinConfig -> {
                    javalinConfig.showJavalinBanner = false;
                })
                .post("/webhook", ctx -> {
                    var tokenHeader = ctx.header("X-Telegram-Bot-Api-Secret-Token");
                    if (tokenHeader == null || !tokenHeader.equals(webhookToken)) {
                        ctx.result("UNAUTHORIZED");
                        ctx.status(HttpStatus.UNAUTHORIZED);
                        logger.info("UNAUTHORIZED request");
                        return;
                    }

                    var update = BotUtils.parseUpdate(ctx.body());

                    logger.info("Update {}", update);

                    try {
                        bot.handleUpdate(update);
                    } catch (Exception e) {
                        logger.error("Cannot handle update {}", update, e);
                    }
                })
                .start(Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8888")));

        var setWebhookResult = tgBot.execute(new SetWebhook().url(tgUrl).secretToken(webhookToken));

        if (!setWebhookResult.isOk()) {
            logger.error("Cannot update webhook: {}", setWebhookResult);
        } else {
            logger.info("Updated webhook");
        }

    }
}