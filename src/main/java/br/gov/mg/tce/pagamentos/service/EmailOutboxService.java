package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.EmailOutbox;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailOutboxService {
    private final EmailOutboxRepository repository;
    private final TransactionTemplate transactionTemplate;

    public List<EmailOutbox> buscarEmailsParaProcessar() {
        return transactionTemplate.execute(status -> repository.buscarPendentesParaProcessamento());
    }

    public void atualizarStatusInicioProcessamento(EmailOutbox email) {
        transactionTemplate.executeWithoutResult(status -> {
            email.iniciarProcessamento();
            repository.save(email);
        });
    }

    public void registrarSucesso(EmailOutbox email) {
        transactionTemplate.executeWithoutResult(status -> {
            email.finalizarComSucesso();
            repository.save(email);
        });
    }

    public void registrarFalha(EmailOutbox email) {
        transactionTemplate.executeWithoutResult(status -> {
            email.registrarFalha();
            repository.save(email);
        });
    }

    public void registrarInteresseNotificacao(Usuario recebedor, BigDecimal valor) {
        EmailOutbox email = new EmailOutbox();
        email.setDestinatario(recebedor.getEmail());
        email.setMensagem("Pagamento recebido no valor de " + valor);
        email.setCriadoEm(LocalDateTime.now());

        repository.save(email);
    }

    public void agendarNotificacaoRecebimento(String email, BigDecimal valor) {
        EmailOutbox outbox = new EmailOutbox();
        outbox.setDestinatario(email);
        outbox.setMensagem("Pagamento recebido no valor de " + valor);
        outbox.setCriadoEm(LocalDateTime.now());
        repository.save(outbox);
    }
}
