package br.gov.mg.tce.pagamentos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "authorizationClient",
        url = "${clients.authorization.url}"
)
public interface AuthorizationClient {

    @GetMapping("/validarUsuario/{documento}")
    Boolean autorizar(@PathVariable String documento);
}