package org.tce.pagamentos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;
import org.tce.pagamentos.dto.response.UsuarioResponseDTO;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.mapper.UsuarioMapper;
import org.tce.pagamentos.service.UsuarioService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public UsuarioResponseDTO salvar(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario usuario = service.salvar(dto);

        return UsuarioMapper.toResponse(usuario);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar() {

        return service.listarTodos()
                .stream()
                .map(UsuarioMapper::toResponse)
                .toList();
    }
}