package br.gov.mg.tce.pagamentos.entity;

import br.gov.mg.tce.pagamentos.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pagador_id", nullable = false)
    private Usuario pagador;

    @ManyToOne
    @JoinColumn(name = "recebedor_id", nullable = false)
    private Usuario recebedor;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    private String mensagemErro;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataProcessamento;

    public void autorizar() {
        if (this.status != StatusPagamento.PENDENTE && this.status != StatusPagamento.ERRO_AUTORIZACAO) {
            throw new IllegalStateException("Pagamento em status inválido para autorização");
        }
        this.status = StatusPagamento.AUTORIZADO;
    }

    public void finalizarComErro(String mensagem, StatusPagamento novoStatus) {
        this.status = novoStatus;
        this.mensagemErro = mensagem;
    }

    public void marcarComoProcessando() {
        this.status = StatusPagamento.PROCESSANDO;
    }

    public boolean podeSerAutorizado() {
        return this.status == StatusPagamento.PENDENTE ||
                this.status == StatusPagamento.ERRO_AUTORIZACAO;
    }

    public void concluir() {
        this.status = StatusPagamento.CONCLUIDO;
        this.dataProcessamento = LocalDateTime.now();
        this.mensagemErro = null;
    }

    public static Pagamento novoPagamento(Usuario pagador, Usuario recebedor, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor a ser pago deve ser maior que zero");
        }
        if (pagador.getNumeroDocumento().equals(recebedor.getNumeroDocumento())) {
            throw new BusinessException("Pagador e recebedor devem ser pessoas diferentes");
        }

        pagador.validarPodeRealizarPagamento();
        pagador.validarSaldoSuficiente(valor);

        Pagamento pagamento = new Pagamento();
        pagamento.setPagador(pagador);
        pagamento.setRecebedor(recebedor);
        pagamento.setValor(valor);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataCriacao(LocalDateTime.now());
        return pagamento;
    }

}
