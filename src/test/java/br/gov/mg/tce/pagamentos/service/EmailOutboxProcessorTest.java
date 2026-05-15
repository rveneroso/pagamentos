package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.NotificationClient;
import br.gov.mg.tce.pagamentos.client.dto.EmailRequestDTO;
import br.gov.mg.tce.pagamentos.entity.EmailOutbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOutboxProcessorTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private EmailOutboxService emailService;

    @InjectMocks
    private EmailOutboxProcessor emailOutboxProcessor;

    @Test
    void shouldProcessAndSendEmailsSuccessfully() {
        EmailOutbox email = new EmailOutbox();
        email.setId(1L);
        email.setDestinatario("teste@tce.mg.gov.br");
        email.setMensagem("Corpo do e-mail");

        when(emailService.buscarEmailsParaProcessar()).thenReturn(List.of(email));

        emailOutboxProcessor.enviarEmails();

        verify(emailService, times(1)).atualizarStatusInicioProcessamento(email);
        verify(notificationClient, times(1)).enviarEmail(any(EmailRequestDTO.class));
        verify(emailService, times(1)).registrarSucesso(email);
        verify(emailService, never()).registrarFalha(any());
    }

    @Test
    void shouldRegisterFailureWhenClientFails() {
        EmailOutbox email = new EmailOutbox();
        email.setId(2L);

        when(emailService.buscarEmailsParaProcessar()).thenReturn(List.of(email));
        doThrow(new RuntimeException("API Out")).when(notificationClient).enviarEmail(any());

        emailOutboxProcessor.enviarEmails();

        verify(emailService, times(1)).atualizarStatusInicioProcessamento(email);
        verify(emailService, times(1)).registrarFalha(email);
        verify(emailService, never()).registrarSucesso(any());
    }

    @Test
    void shouldNotProcessWhenNoEmailsFound() {
        when(emailService.buscarEmailsParaProcessar()).thenReturn(List.of());

        emailOutboxProcessor.enviarEmails();

        verifyNoInteractions(notificationClient);
        verify(emailService, never()).registrarSucesso(any());
        verify(emailService, never()).registrarFalha(any());
    }
}