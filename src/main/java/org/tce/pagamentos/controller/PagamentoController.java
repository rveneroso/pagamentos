package org.tce.pagamentos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tce.pagamentos.dto.request.PagamentoRequestDTO;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.service.PagamentoService;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<Pagamento>> listarTodos() {

        List<Pagamento> pagamentos = pagamentoService.listarTodos();

        return ResponseEntity.ok(pagamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscarPorId(@PathVariable Long id) {

        Pagamento pagamento = pagamentoService.buscarPorId(id);

        return ResponseEntity.ok(pagamento);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pagamento>> buscarPorStatus(
            @PathVariable StatusPagamento status) {

        List<Pagamento> pagamentos =
                pagamentoService.buscarPorStatus(status);

        return ResponseEntity.ok(pagamentos);
    }
}