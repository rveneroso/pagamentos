package org.tce.pagamentos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tce.pagamentos.dto.UsuarioDTO;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.service.UsuarioService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public Usuario salvar(@RequestBody @Valid UsuarioDTO dto) {
        return service.salvar(dto);
    }

    @GetMapping
    public List<Usuario> listar() {
        return service.listarTodos();
    }
}