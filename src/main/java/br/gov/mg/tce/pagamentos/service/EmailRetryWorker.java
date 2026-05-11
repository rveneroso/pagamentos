package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.NotificationClient;
import br.gov.mg.tce.pagamentos.client.dto.EmailRequestDTO;
import br.gov.mg.tce.pagamentos.repository.EmailOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import br.gov.mg.tce.pagamentos.entity.EmailOutbox;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailRetryWorker {

    private final EmailOutboxRepository repository;
    private final NotificationClient notificationClient;

    // Executará a cada 30 segundos
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void reenviarEmails() {

        List<EmailOutbox> pendentes =
                repository.buscarPendentesParaProcessamento();

        for (EmailOutbox email : pendentes) {

            email.setProcessando(true);
            email.setLockedAt(LocalDateTime.now());
            repository.save(email);

            try {
                notificationClient.enviarEmail(
                        new EmailRequestDTO(
                                email.getDestinatario(),
                                email.getMensagem()
                        )
                );

                email.setEnviado(true);
                email.setEnviadoEm(LocalDateTime.now());

            } catch (Exception e) {
                email.setTentativas(email.getTentativas() + 1);
            } finally {
                email.setProcessando(false);
            }

            repository.save(email);
        }
    }
}
