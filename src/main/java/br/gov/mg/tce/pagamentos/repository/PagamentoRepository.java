package br.gov.mg.tce.pagamentos.repository;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByStatus(StatusPagamento status);
    List<Pagamento> findByStatusOrderByDataCriacaoAsc(StatusPagamento status);
}