package br.gov.mg.tce.pagamentos.entity;

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

}
