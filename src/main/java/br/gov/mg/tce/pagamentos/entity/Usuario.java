package br.gov.mg.tce.pagamentos.entity;

import br.gov.mg.tce.pagamentos.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;

import java.math.BigDecimal;

@Entity
@Table(name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "numeroDocumento")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nomeCompleto;

    @NotBlank
    private String numeroDocumento;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private BigDecimal saldo;

    // Dentro de Usuario.java
    public void debitar(BigDecimal valor) {
        this.validarSaldoSuficiente(valor);
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void validarPodeRealizarPagamento() {
        if (this.tipo == TipoUsuario.PJ) {
            throw new BusinessException("Lojistas não podem realizar pagamentos");
        }
    }

    public void validarSaldoSuficiente(BigDecimal valor) {
        if (this.saldo.compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para essa transação");
        }
    }

}
