package ru.ramlabs;

import com.google.gson.Gson;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var tgBot = new TelegramBot(System.getenv("BOT_TOKEN"));
        var bot = new Bot(tgBot);
//        var token = generateSecret();
//        logger.info("Webhook token: {}", token);
        Gson gson = new Gson();

        Javalin.create()
                .post("/webhook", ctx -> {
//                    var tokenHeader = ctx.header("X-Telegram-Bot-Api-Secret-Token");
//                    if (tokenHeader == null || !tokenHeader.equals(token)) {
//                        ctx.result("UNAUTHORIZED");
//                        ctx.status(HttpStatus.UNAUTHORIZED);
//                        logger.info("UNAUTHORIZED request");
//                        return;
//                    }

                    var update = BotUtils.parseUpdate(ctx.body());

                    logger.info("Update {}", update);

                    var baseRequest = bot.handleUpdate(update);
                    if (baseRequest != null) {
                        ctx.result(baseRequest.toWebhookResponse());
                    }
                })
                .start(Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8888")));

        var setWebhookResult = tgBot.execute(new SetWebhook().url("https://tgbotdev.averageorange.ml/webhook")
//                .secretToken(token)
        );

        if (!setWebhookResult.isOk()) {
            logger.error("Cannot update webhook: {}", setWebhookResult);
        } else {
            logger.info("Updated webhook");
        }

    }


    public static String generateSecret() {
        var token = new char[32];
        var r = new Random();
        for (int i = 0; i < token.length; i++) {
            if (r.nextBoolean()) {
                token[i] = (char) r.nextInt('A', 'Z' + 1);
            } else {
                token[i] = (char) r.nextInt('a', 'z' + 1);
            }
        }
        return new String(token);
    }
}