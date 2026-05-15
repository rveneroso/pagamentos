package br.gov.mg.tce.pagamentos.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("tce-async-");
        executor.setCorePoolSize(5); // Número de tarefas que podem executar simultâneamente
        executor.setMaxPoolSize(10); // Número máximo de threads que podem ser criadas
        executor.setQueueCapacity(100); // Define quantas tarefas podem ficar na fila antes de começar a rejeitar novos pedidos

        // Essa configuração evita que o Spring lance uma RejectedExecutionException, o que interromperia o fluxo de pagamento abruptamente.
        // // O uso de CallerRunsPolicy, diz que, se o pool de threads estiver lotado, a thread que está tentando agendar um novo
        // pagamento, deve ela mesma executar o trabalho.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("tce-scheduled-");
        scheduler.setPoolSize(5); // Define 5 threads para agendamentos
        scheduler.initialize();
        return scheduler;
    }
}