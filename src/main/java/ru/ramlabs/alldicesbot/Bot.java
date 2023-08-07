package ru.ramlabs.alldicesbot;

import com.github.ram042.json.Json;
import com.github.ram042.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Bot {

    private final SecureRandom random = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(Bot.class);

    private static final Pattern presetCommandPattern = Pattern.compile("/d(\\d+)(@.+)?");
    private static final Pattern diceCommandPattern = Pattern.compile("(/dice(@\\w+)?\\s+)?\\s*((\\d*[dD])?\\d+\\s*)+");

    public Bot() {
    }

    public JsonObject handleUpdate(JsonObject update) {
        if (!update.containsObject("message") || !update.getObject("message").containsString("text")) {
            return null;
        }

        var message = update.getObject("message").getString("text").string;

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
                return Json.object(
                        "method", "sendMessage",
                        "chat_id", update.getObject("message").getObject("chat").getNumber("id").longValue(),
                        "text", result,
                        "reply_to_message_id", update.getObject("message").getNumber("message_id").longValue()
                );
            }
        }

        var diceCommandMatcher = diceCommandPattern.matcher(message);

        if (diceCommandMatcher.matches()) {
            TreeMap<Integer, Integer> parseDices = CommandParser.parseDices(message);

            String result = "";

            if (!parseDices.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : parseDices.entrySet()) {
                    Integer dice = entry.getKey();
                    Integer count = entry.getValue();
                    result += "D" + dice + "\n";
                    long sum = 0;
                    for (int i = 0; i < count; i++) {
                        var roll = random.nextInt(1, dice + 1);
                        result += roll + "\n";
                        sum += roll;
                    }
                    if (count > 1) {
                        result += "Sum: " + sum + "\n";
                    }
                    result += "\n";
                }

                return Json.object(
                        "method", "sendMessage",
                        "chat_id", update.getObject("message").getObject("chat").getNumber("id").longValue(),
                        "text", result,
                        "reply_to_message_id", update.getObject("message").getNumber("message_id").longValue()
                );
            }
        }
        return null;
    }

}
