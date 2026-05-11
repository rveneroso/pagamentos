package org.tce.pagamentos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tce.pagamentos.client.NotificationClient;
import org.tce.pagamentos.client.dto.EmailRequestDTO;
import org.tce.pagamentos.entity.EmailOutbox;
import org.tce.pagamentos.repository.EmailOutboxRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailRetryWorkerTest {

    @Mock
    private EmailOutboxRepository repository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private EmailRetryWorker emailRetryWorker;

    @Test
    void shouldProcessAndSendEmailsWithSuccess() {
        EmailOutbox email = new EmailOutbox();
        email.setDestinatario("user@tce.org");
        email.setMensagem("Pagamento confirmado");
        email.setEnviado(false);
        email.setTentativas(0);

        when(repository.buscarPendentesParaProcessamento()).thenReturn(List.of(email));

        emailRetryWorker.reenviarEmails();

        // Verifica se o cliente de notificação foi chamado corretamente
        verify(notificationClient, times(1)).enviarEmail(any(EmailRequestDTO.class));

        // Verifica se o objeto foi salvo com os novos estados
        assertTrue(email.isEnviado());
        assertNotNull(email.getEnviadoEm());
        assertFalse(email.isProcessando());

        verify(repository, atLeastOnce()).save(email);
    }

    @Test
    void shouldIncrementAttemptsOnFailure() {
        EmailOutbox email = new EmailOutbox();
        email.setTentativas(1);
        email.setEnviado(false);

        when(repository.buscarPendentesParaProcessamento()).thenReturn(List.of(email));

        // Simula erro no serviço de email
        doThrow(new RuntimeException("External Service Down"))
                .when(notificationClient).enviarEmail(any(EmailRequestDTO.class));

        emailRetryWorker.reenviarEmails();

        assertEquals(2, email.getTentativas());
        assertFalse(email.isEnviado());
        assertFalse(email.isProcessando()); // O finally deve garantir que seja false
        verify(repository, atLeastOnce()).save(email);
    }

    @Test
    void shouldLockEmailWhileProcessing() {
        EmailOutbox email = new EmailOutbox();
        email.setProcessando(false);
        when(repository.buscarPendentesParaProcessamento()).thenReturn(List.of(email));

        // Criamos uma flag para capturar o estado interno no momento da chamada
        final boolean[] wasLocked = {false};

        // Configuramos o mock para interceptar o PRIMEIRO save
        doAnswer(invocation -> {
            EmailOutbox savedEmail = invocation.getArgument(0);
            // Se for o momento do lock (antes de enviar), capturamos o valor
            if (savedEmail.isProcessando()) {
                wasLocked[0] = true;
            }
            return savedEmail;
        }).when(repository).save(any(EmailOutbox.class));

        emailRetryWorker.reenviarEmails();

        assertTrue(wasLocked[0], "The email should have been marked as 'processando=true' before sending");
        assertFalse(email.isProcessando(), "The email should be 'processando=false' after the worker finishes");
    }
}