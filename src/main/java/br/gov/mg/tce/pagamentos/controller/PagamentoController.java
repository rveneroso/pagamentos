package br.gov.mg.tce.pagamentos.controller;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.gov.mg.tce.pagamentos.dto.request.PagamentoRequestDTO;

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