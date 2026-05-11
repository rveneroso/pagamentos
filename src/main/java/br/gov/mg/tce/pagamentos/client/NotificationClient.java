package br.gov.mg.tce.pagamentos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import br.gov.mg.tce.pagamentos.client.dto.EmailRequestDTO;

@FeignClient(
        name = "notificationClient",
        url = "${clients.email.url}"
)
public interface NotificationClient {

    @PostMapping("/envio")
    void enviarEmail(@RequestBody EmailRequestDTO request);
}