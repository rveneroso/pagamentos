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

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setId(usuario.getId());
        dto.setNomeCompleto(usuario.getNomeCompleto());
        dto.setNumeroDocumento(usuario.getNumeroDocumento());
        dto.setEmail(usuario.getEmail());
        dto.setTipo(usuario.getTipo());

        return dto;
    }

}