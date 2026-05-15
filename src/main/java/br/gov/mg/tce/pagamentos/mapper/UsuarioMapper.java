package br.gov.mg.tce.pagamentos.mapper;

import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;
import br.gov.mg.tce.pagamentos.dto.response.UsuarioResponseDTO;
import br.gov.mg.tce.pagamentos.entity.Usuario;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioRequestDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setNomeCompleto(dto.getNomeCompleto());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setTipo(dto.getTipo());

        return usuario;
    }

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getNumeroDocumento(),
                usuario.getEmail(),
                usuario.getTipo()
        );
    }

}