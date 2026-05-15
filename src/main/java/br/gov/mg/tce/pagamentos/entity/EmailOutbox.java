package br.gov.mg.tce.pagamentos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class EmailOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destinatario;
    private String mensagem;

    private boolean enviado = false;

    private int tentativas = 0;

    private LocalDateTime criadoEm;
    private LocalDateTime enviadoEm;

    private boolean processando;
    private LocalDateTime lockedAt;

    public void iniciarProcessamento() {
        this.processando = true;
        this.lockedAt = LocalDateTime.now();
    }

    public void finalizarComSucesso() {
        this.enviado = true;
        this.enviadoEm = LocalDateTime.now();
        this.processando = false;
    }

    public void incrementarTentativa() {
        this.tentativas += 1;
    }

    public void registrarFalha() {
        this.tentativas++;
        this.processando = false;
    }
}
