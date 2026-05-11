package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.mapper.UsuarioMapper;
import br.gov.mg.tce.pagamentos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.exception.BusinessException;
import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;

import br.gov.mg.tce.pagamentos.entity.Usuario;

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