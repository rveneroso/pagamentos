package org.tce.pagamentos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tce.pagamentos.dto.request.PagamentoRequestDTO;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.service.PagamentoService;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<Pagamento> criarPagamento(@RequestBody @Valid PagamentoRequestDTO dto) {

        Pagamento pagamento = pagamentoService.criarPagamento(dto);

        return ResponseEntity.accepted().body(pagamento);
    }
}