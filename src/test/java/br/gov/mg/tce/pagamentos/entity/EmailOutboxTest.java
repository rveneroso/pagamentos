package br.gov.mg.tce.pagamentos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailOutboxTest {

    @Test
    void shouldStartProcessing() {
        EmailOutbox email = new EmailOutbox();
        email.setProcessando(false);

        email.iniciarProcessamento();

        assertTrue(email.isProcessando(), "O campo 'processando' deveria ser true");
        assertNotNull(email.getLockedAt(), "O campo 'lockedAt' deveria estar preenchido");
    }

    @Test
    void shouldFinishWithSuccess() {
        EmailOutbox email = new EmailOutbox();
        email.setProcessando(true);
        email.setEnviado(false);

        email.finalizarComSucesso();

        assertTrue(email.isEnviado(), "O campo 'enviado' deveria ser true");
        assertFalse(email.isProcessando(), "O campo 'processando' deveria retornar false");
        assertNotNull(email.getEnviadoEm(), "O campo 'enviadoEm' deveria estar preenchidp");
    }

    @Test
    void shouldIncrementAttempts() {
        EmailOutbox email = new EmailOutbox();
        assertEquals(0, email.getTentativas());

        email.incrementarTentativa();
        email.incrementarTentativa();

        assertEquals(2, email.getTentativas(), "O contador de tentativas deveria ser 2");
    }

    @Test
    void shouldRegisterFailure() {
        EmailOutbox email = new EmailOutbox();
        email.setProcessando(true);
        email.setTentativas(1);

        email.registrarFalha();

        assertEquals(2, email.getTentativas(), "The attempts should increase to 2");
        assertFalse(email.isProcessando(), "The processing flag should be released (false) so the worker can retry");
    }
}