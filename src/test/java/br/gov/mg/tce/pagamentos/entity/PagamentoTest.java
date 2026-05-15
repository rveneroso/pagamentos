package br.gov.mg.tce.pagamentos.entity;

import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {

    @Test
    void shouldCreateNewPagamentoWithSuccess() {
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123");
        pagador.setSaldo(new BigDecimal("1000.00"));

        Usuario recebedor = new Usuario();
        recebedor.setNumeroDocumento("456");

        Pagamento pagamento = Pagamento.novoPagamento(pagador, recebedor, new BigDecimal("100.00"));

        assertNotNull(pagamento);
        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
        assertEquals(new BigDecimal("100.00"), pagamento.getValor());
        assertNotNull(pagamento.getDataCriacao());
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        Usuario u1 = new Usuario();
        Usuario u2 = new Usuario();

        assertThrows(BusinessException.class, () ->
                Pagamento.novoPagamento(u1, u2, BigDecimal.ZERO));

        assertThrows(BusinessException.class, () ->
                Pagamento.novoPagamento(u1, u2, new BigDecimal("-10.00")));
    }

    @Test
    void shouldThrowExceptionWhenUsersAreSame() {
        Usuario u1 = new Usuario();
        u1.setNumeroDocumento("123456");

        assertThrows(BusinessException.class, () ->
                Pagamento.novoPagamento(u1, u1, new BigDecimal("10.00")));
    }

    @Test
    void shouldConcluirPaymentCorrectly() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.AUTORIZADO);

        pagamento.concluir();

        assertEquals(StatusPagamento.CONCLUIDO, pagamento.getStatus());
        assertNotNull(pagamento.getDataProcessamento());
        assertNull(pagamento.getMensagemErro());
    }

    @Test
    void shouldThrowExceptionWhenAuthorizingInvalidStatus() {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.CONCLUIDO);

        assertThrows(IllegalStateException.class, () -> pagamento.autorizar());
    }

    @Test
    void shouldValidateIfCanBeAuthorized() {
        Pagamento p1 = new Pagamento();
        p1.setStatus(StatusPagamento.PENDENTE);
        assertTrue(p1.podeSerAutorizado());

        Pagamento p2 = new Pagamento();
        p2.setStatus(StatusPagamento.ERRO_AUTORIZACAO);
        assertTrue(p2.podeSerAutorizado());

        Pagamento p3 = new Pagamento();
        p3.setStatus(StatusPagamento.CONCLUIDO);
        assertFalse(p3.podeSerAutorizado());
    }
}