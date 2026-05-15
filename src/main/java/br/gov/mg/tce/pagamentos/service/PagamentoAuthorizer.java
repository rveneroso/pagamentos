package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.AuthorizationClient;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PagamentoAuthorizer {

    private final AuthorizationClient authorizationClient;

    public boolean obterAutorizacao(Pagamento pagamento) {
        try {
            log.debug("Solicitando autorização externa para o pagamento: {}", pagamento.getId());
            return authorizationClient.autorizar(pagamento.getPagador().getNumeroDocumento());
        } catch (Exception ex) {
            log.error("Falha técnica ao comunicar com o serviço de autorização", ex);
            // Lança uma exceção específica ou deixa subir para o orquestrador decidir o status (ex: ERRO_AUTORIZACAO)
            throw ex;
        }
    }
}
