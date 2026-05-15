package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.EmailOutbox;
import br.gov.mg.tce.pagamentos.repository.EmailOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOutboxServiceTest {

    @Mock
    private EmailOutboxRepository repository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private EmailOutboxService emailService;

    @BeforeEach
    void setUp() {
        // Usa-se lenient() para evitar o erro de UnnecessaryStubbing
        // quando o teste não utiliza um dos métodos do TransactionTemplate
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        lenient().doAnswer(invocation -> {
            Consumer<?> consumer = invocation.getArgument(0);
            consumer.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void shouldFetchPendingEmails() {
        EmailOutbox email = new EmailOutbox();
        when(repository.buscarPendentesParaProcessamento()).thenReturn(List.of(email));

        List<EmailOutbox> result = emailService.buscarEmailsParaProcessar();

        assertEquals(1, result.size());
        verify(transactionTemplate).execute(any());
    }

    @Test
    void shouldUpdateStatusToProcessing() {
        EmailOutbox email = spy(new EmailOutbox());

        emailService.atualizarStatusInicioProcessamento(email);

        verify(email).iniciarProcessamento();
        verify(repository).save(email);
    }

    @Test
    void shouldRegisterSuccess() {
        EmailOutbox email = spy(new EmailOutbox());

        emailService.registrarSucesso(email);

        verify(email).finalizarComSucesso();
        verify(repository).save(email);
    }

    @Test
    void shouldRegisterFailure() {
        EmailOutbox email = spy(new EmailOutbox());

        emailService.registrarFalha(email);

        verify(email).registrarFalha();
        verify(repository).save(email);
    }

    @Test
    void shouldScheduleNotification() {
        BigDecimal valor = new BigDecimal("150.00");
        String emailDestino = "contribuinte@mg.gov.br";

        emailService.agendarNotificacaoRecebimento(emailDestino, valor);

        verify(repository).save(argThat(outbox ->
                outbox.getDestinatario().equals(emailDestino) &&
                        outbox.getMensagem().contains("150.00")
        ));
    }
}