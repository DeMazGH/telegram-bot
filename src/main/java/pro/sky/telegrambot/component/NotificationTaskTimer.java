package pro.sky.telegrambot.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class NotificationTaskTimer {

    private final NotificationTaskRepository notificationTaskRepository;
    private final SendHelper sendHelper;

    public NotificationTaskTimer(NotificationTaskRepository notificationTaskRepository, SendHelper sendHelper) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.sendHelper = sendHelper;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void task() {
        notificationTaskRepository.findByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(notificationTask -> {
                            sendHelper.sendMessage(notificationTask.getChatId(), notificationTask.getMessage());
                            notificationTaskRepository.delete(notificationTask);
                        }
                );
    }
}
