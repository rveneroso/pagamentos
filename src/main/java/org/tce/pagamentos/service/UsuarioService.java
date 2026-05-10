package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.exception.BusinessException;
import org.tce.pagamentos.mapper.UsuarioMapper;
import org.tce.pagamentos.repository.UsuarioRepository;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;

import org.tce.pagamentos.entity.Usuario;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;


    public Usuario salvar(UsuarioRequestDTO dto) {

        Usuario usuario = UsuarioMapper.toEntity(dto);

        if (repository.existsByEmail(usuario.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (repository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new BusinessException("Número de documento já cadastrado");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        // Define o saldo inicial do usuário. Lojistas (PJ) terão saldo inicial zero já que não podem realizar pagamentos.
        usuario.setSaldo(
                usuario.getTipo() == TipoUsuario.PF
                        ? new BigDecimal("200.00")
                        : BigDecimal.ZERO
        );
        return repository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }
}