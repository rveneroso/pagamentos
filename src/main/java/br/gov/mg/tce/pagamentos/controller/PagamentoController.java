package br.gov.mg.tce.pagamentos.controller;

import br.gov.mg.tce.pagamentos.dto.response.PagamentoResponseDTO;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.gov.mg.tce.pagamentos.dto.request.PagamentoRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "Endpoints para gestão e processamento de pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @Operation(summary = "Criar novo pagamento", description = "Inicia o fluxo de pagamento entre um pagador comum e um recebedor.")
    @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro de validação ou saldo insuficiente")
    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> criarPagamento(@RequestBody @Valid PagamentoRequestDTO dto) {
        Pagamento pagamento = pagamentoService.criarPagamento(dto);
        return ResponseEntity.accepted().body(new PagamentoResponseDTO(pagamento));
    }

    @Operation(summary = "Listar pagamentos", description = "Retorna todos os pagamentos registrados no sistema.")
    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listarTodos() {
        List<PagamentoResponseDTO> pagamentos = pagamentoService.listarTodos()
                .stream()
                .map(PagamentoResponseDTO::new) // Conversão rápida usando Method Reference
                .toList();
        return ResponseEntity.ok(pagamentos);
    }

    @Operation(summary = "Consulta pagamento", description = "Retorna o pagamento com o id informado.")
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(new PagamentoResponseDTO(pagamento));
    }

    @Operation(summary = "Consulta pagamentos por status", description = "Retorna todos os pagamentos cujo status seja aquele informado.")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PagamentoResponseDTO>> buscarPorStatus(
            @PathVariable StatusPagamento status) {

        List<PagamentoResponseDTO> pagamentos = pagamentoService.buscarPorStatus(status)
                .stream()
                .map(PagamentoResponseDTO::new)
                .toList();

        return ResponseEntity.ok(pagamentos);
    }




}