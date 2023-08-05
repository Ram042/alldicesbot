package ru.ramlabs.alldicesbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandParser {
    private static final Pattern whitespacePattern = Pattern.compile("\\s+");
    private static final Pattern dicePattern = Pattern.compile("(?:(\\d*)[dD])?(\\d+)");

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParser.class);

    private CommandParser() {
    }

    public static TreeMap<Integer, Integer> parseDices(String string) {

        TreeMap<Integer, Integer> result = new TreeMap<>();

        if (string.startsWith("/dice")) {
            string = string.substring(string.indexOf(' '));
        }

        var matcher = dicePattern.matcher(string);
        while (matcher.find()) {
            int count = matcher.group(1) == null ? 1 : switch (matcher.group(1)) {
                case "" -> 1;
                default -> Integer.parseInt(matcher.group(1));
            };
            var dice = Integer.parseInt(matcher.group(2));

            result.compute(dice, (k, v) -> v == null ? count : v + count);
        }

        if (result.values().stream().collect(Collectors.summarizingInt(value -> value)).getSum() > 32) {
            LOGGER.warn("Illegal dice count");
            return new TreeMap<>();
        }

        if (result.values().stream().anyMatch(i -> i <= 0 | i > Integer.MAX_VALUE / 2)) {
            LOGGER.warn("Dice is to large");
            return new TreeMap<>();
        }

        return result;
    }
}
