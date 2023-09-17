package ru.ramlabs.alldicesbot;

import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ReflectionUtils;

import static org.springframework.aot.hint.MemberCategory.*;

public class Hints implements RuntimeHintsRegistrar {
    @SneakyThrows
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses().stream()
                .filter(clazz -> clazz.getPackageName().equalsIgnoreCase("com.pengrad.telegrambot.model"))
                .map(ClassPath.ClassInfo::load)
                .forEach(clazz -> hints.reflection().registerType(clazz,
                        DECLARED_FIELDS, INVOKE_DECLARED_CONSTRUCTORS, INVOKE_DECLARED_METHODS));
        hints.resources()
                .registerPattern(".*/application.yml")
                .registerPattern(".*/logback.xml$");
    }
}
