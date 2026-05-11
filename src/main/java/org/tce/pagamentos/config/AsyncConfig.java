package org.tce.pagamentos.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); // Número de tarefas que podem executar simultâneamente
        executor.setMaxPoolSize(10); // Número máximo de threads que podem ser criadas
        executor.setQueueCapacity(100); // Define quantas tarefas podem ficar na fila antes de começar a rejeitar novos pedidos
        executor.initialize();

        return executor;
    }
}