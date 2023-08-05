package ru.ramlabs.alldicesbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Bot {

    private final SecureRandom random = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(Bot.class);

    private final TelegramBot bot;

    private static final Pattern presetCommandPattern = Pattern.compile("/d(\\d+)(@.+)?");
    private static final Pattern diceCommandPattern = Pattern.compile("(/dice(@\\w+)?\\s+)?\\s*((\\d*[dD])?\\d+\\s*)+");

    public Bot(TelegramBot bot) {
        this.bot = bot;
    }

    public void handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        var message = update.message().text();

        var matcher = presetCommandPattern.matcher(message);
        if (matcher.matches()) {
            String result = switch (Integer.parseInt(matcher.group(1))) {
                case 2 -> switch (random.nextInt(1, 3)) {
                    case 1 -> "1 - Heads";
                    case 2 -> "2 - Tails";
                    default -> null;
                };
                case 6 -> {
                    var roll = random.nextInt(1, 7);
                    yield Character.toString('⚀' - 1 + roll) + " - " + roll;
                }
                case 20 -> Integer.toString(random.nextInt(1, 21));
                default -> null;
            };

            if (result != null) {
                var sendMessage = new SendMessage(update.message().chat().id(), result);
                sendMessage.replyToMessageId(update.message().messageId());
                var executed = bot.execute(sendMessage);
                logger.info("Result: {}", executed);
                return;
            }
        }

        var diceCommandMatcher = diceCommandPattern.matcher(message);

        if (diceCommandMatcher.matches()) {
            TreeMap<Integer, Integer> parseDices = CommandParser.parseDices(update.message().text());

            String result = "";

            if (!parseDices.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : parseDices.entrySet()) {
                    Integer dice = entry.getKey();
                    Integer count = entry.getValue();
                    result += "D" + dice + "\n";
                    int sum = 0;
                    for (Integer i = 0; i < count; i++) {
                        var roll = random.nextInt(1, dice + 1);
                        result += roll + "\n";
                        sum += roll;
                    }
                    if (count > 1) {
                        result += "Sum: " + sum + "\n";
                    }
                    result += "\n";
                }

                var sendMessage = new SendMessage(update.message().chat().id(), result);
                sendMessage.replyToMessageId(update.message().messageId());
                var executed = bot.execute(sendMessage);
                logger.info("Result: {}", executed);
            }
        }
    }

}
