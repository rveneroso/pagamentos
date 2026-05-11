package br.gov.mg.tce.pagamentos.controller;

import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;
import br.gov.mg.tce.pagamentos.dto.response.UsuarioResponseDTO;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.exception.ErrorResponse;
import br.gov.mg.tce.pagamentos.mapper.UsuarioMapper;
import br.gov.mg.tce.pagamentos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários (Clientes e Lojistas)")
public class UsuarioController {

    private final UsuarioService service;

    @Operation(summary = "Cadastrar novo usuário", description = "Cria um novo usuário no sistema (PF ou PJ) com saldo inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: Documento ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO salvar(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario usuario = service.salvar(dto);
        return UsuarioMapper.toResponse(usuario);
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna a listagem completa de usuários cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return service.listarTodos()
                .stream()
                .map(UsuarioMapper::toResponse)
                .toList();
    }
}