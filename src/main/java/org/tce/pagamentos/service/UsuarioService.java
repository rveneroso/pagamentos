package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tce.pagamentos.repository.UsuarioRepository;
import org.tce.pagamentos.dto.UsuarioDTO;

import org.tce.pagamentos.entity.Usuario;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;


    public Usuario salvar(UsuarioDTO dto) {

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.getNomeCompleto());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipo(dto.getTipo());

        if (repository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (repository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new RuntimeException("Número de documento já cadastrado");
        }

        return repository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }
}