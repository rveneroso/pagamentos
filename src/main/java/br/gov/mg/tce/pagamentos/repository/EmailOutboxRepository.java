package br.gov.mg.tce.pagamentos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.gov.mg.tce.pagamentos.entity.EmailOutbox;

import java.util.List;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

     // Garante que apenas uma instância do Worker capture e bloqueie os e-mails para envio,
     // evitando disparos duplicados.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmailOutbox e WHERE e.enviado = false AND e.processando = false")
    List<EmailOutbox> buscarPendentesParaProcessamento();
}