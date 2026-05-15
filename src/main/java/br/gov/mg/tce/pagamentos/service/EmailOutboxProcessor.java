package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.NotificationClient;
import br.gov.mg.tce.pagamentos.client.dto.EmailRequestDTO;
import br.gov.mg.tce.pagamentos.entity.EmailOutbox;
import br.gov.mg.tce.pagamentos.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOutboxProcessor {

    private final EmailOutboxRepository repository;
    private final NotificationClient notificationClient;
    private final EmailOutboxService emailService;

    // Executa 1 minuto após o fim da execução anterior
    @Scheduled(fixedDelay = 60000)
    public void enviarEmails() {
        log.info("EmailOutboxProcessor.enviarEmails -  iniciando verificação de emails pendentes de envio.");
        List<EmailOutbox> pendentes = emailService.buscarEmailsParaProcessar();

        log.info("EmailOutboxProcessor.enviarEmails - encontrados {} registros", pendentes.size());
        for (EmailOutbox email : pendentes) {
            log.info("EmailOutboxProcessor.enviarEmails - processando e-mail ID: {}", email.getId());
            emailService.atualizarStatusInicioProcessamento(email);
            try {
                notificationClient.enviarEmail(new EmailRequestDTO(email.getDestinatario(), email.getMensagem()));
                emailService.registrarSucesso(email);
            } catch (Exception e) {
                emailService.registrarFalha(email);
            }
        }
    }
}
