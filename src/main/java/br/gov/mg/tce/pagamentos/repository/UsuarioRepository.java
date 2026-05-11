package br.gov.mg.tce.pagamentos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.gov.mg.tce.pagamentos.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Aplica Lock Pessimista de escrita (SELECT FOR UPDATE) para garantir a exclusividade no acesso ao saldo durante a transação,
    // prevenindo o problema de 'Lost Update' em cenários de alta concorrência.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    boolean existsByEmail(String email);
    boolean existsByNumeroDocumento(String numeroDocumento);

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
}