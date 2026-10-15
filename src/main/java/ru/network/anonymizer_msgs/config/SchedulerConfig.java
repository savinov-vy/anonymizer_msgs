package ru.network.anonymizer_msgs.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // Количество фоновых потоков для таймеров
        scheduler.setThreadNamePrefix("MafiaTimer-");
        scheduler.initialize();
        return scheduler;
    }
}
