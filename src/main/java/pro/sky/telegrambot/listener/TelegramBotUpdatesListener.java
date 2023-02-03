package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH.mm");

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String text = update.message().text();
            Long chatId = update.message().chat().id();

            if ("/start".equals(text)) {
                sendMessage(chatId, "Привет, " + update.message().chat().username() + "! " +
                        "Для планрования задачи отправь её в формате:\n**01.01.2022 20:00 Сделать домашнюю работу**");
            } else {
                Matcher matcher = PATTERN.matcher(text);
                LocalDateTime dateTime;
                if (matcher.matches() && (dateTime = parse(matcher.group(1))) != null) {
                    String message = matcher.group(3);
                    notificationTaskService.create(chatId, message, dateTime);
                    sendMessage(chatId, "Задача запланирована!");
                } else {
                    sendMessage(chatId, "Некорректный формат сообщения!");
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String text, ParseMode parseMode) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.parseMode(parseMode);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error(sendResponse.toString());
        }
    }

    private void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, ParseMode.MarkdownV2);
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
