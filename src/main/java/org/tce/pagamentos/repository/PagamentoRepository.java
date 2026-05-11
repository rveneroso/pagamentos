package org.tce.pagamentos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.enums.StatusPagamento;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByStatus(StatusPagamento status);
}