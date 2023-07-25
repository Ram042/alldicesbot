package ru.ramlabs;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Bot {

    private final SecureRandom random = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(Bot.class);

    private final TelegramBot bot;

    public Bot(TelegramBot bot) {
        this.bot = bot;
    }

    public BaseRequest handleUpdate(Update update) {
        var ref = new Object() {
            String result = "";
        };

        var parseDices = parseDices(update.message().text());
        if (!parseDices.isEmpty()) {
            parseDices.forEach((dice, count) -> {
                ref.result += "d" + dice + "\n";
                for (Integer i = 0; i < count; i++) {
                    ref.result += random.nextInt(1, dice + 1) + "\n";
                }
                ref.result += "\n";
            });


            var executed = bot.execute(new SendMessage(update.message().chat().id(),
                    ref.result
            ));

            if (executed.isOk()) {
                logger.info("OK");
            } else {
                logger.warn("Err {}", executed.message());
            }
        }

        return null;
    }

    TreeMap<Integer, Integer> parseDices(String string) {

        var pattern = Pattern.compile("(\\d*d\\d+)");
        var matcher = pattern.matcher(string);

        List<String> dices = new LinkedList<>();
        TreeMap<Integer, Integer> parsedDices = new TreeMap<>();

        while (matcher.find()) {
            dices.add(matcher.group());
        }

        if (!dices.isEmpty()) {
            for (String dice : dices) {
                int d;
                int i;

                var split = dice.split("d");
                try {
                    if (split[0].length() == 0) {
                        d = Integer.parseInt(split[1]);
                        i = 1;
                    } else {
                        d = Integer.parseInt(split[1]);
                        i = Integer.parseInt(split[0]);
                    }
                } catch (NumberFormatException e) {
                    return new TreeMap<>();
                }

                if (i == 0 || i > 32 || d > Integer.MAX_VALUE / 2 || d < 2) {
                    return new TreeMap<>();
                }

                if (parsedDices.values().stream().collect(Collectors.summarizingInt(value -> value)).getSum() + i > 32) {
                    return new TreeMap<>();
                }

                if (parsedDices.containsKey(d)) {
                    return new TreeMap<>();
                }

                parsedDices.put(d, i);
            }
        }

        return parsedDices;
    }

}
