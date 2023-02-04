package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public void create(long chatId, String message, LocalDateTime dateTime) {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setChatId(chatId);
        notificationTask.setMessage(message);
        notificationTask.setDateTime(dateTime.truncatedTo(ChronoUnit.MINUTES));
        notificationTaskRepository.save(notificationTask);
    }
}
