package org.tce.pagamentos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tce.pagamentos.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);
    boolean existsByNumeroDocumento(String numeroDocumento);
}