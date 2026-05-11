package br.gov.mg.tce.pagamentos.validation.validator;

import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DocumentoTipoValidatorTest {

    private DocumentoTipoValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        // Antes de executar cada método, cria o validator real, um mock do contexto e simula o comportamento interno do Bean Validation.
        validator = new DocumentoTipoValidator();
        context = mock(ConstraintValidatorContext.class);

        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(builder);

        when(builder.addPropertyNode(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));
    }

    // Cenários em que o dado está inconsistente e a validação não se aplica.
    @Test
    void shouldReturnTrueWhenDtoIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueWhenTipoIsNull() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNumeroDocumento("123");

        assertTrue(validator.isValid(dto, context));
    }

    @Test
    void shouldReturnTrueWhenDocumentIsNull() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PF);

        assertTrue(validator.isValid(dto, context));
    }

    @Test
    void shouldReturnTrueWhenDocumentIsEmpty() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PF);
        dto.setNumeroDocumento("   ");

        assertTrue(validator.isValid(dto, context));
    }

    // Testes com CPF
    @ParameterizedTest
    @ValueSource(strings = {
            "abc12345678",
            "1234567890",
            "11111111111"
    })
    void shouldReturnFalseWhenCpfIsInvalid(String documento) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PF);
        dto.setNumeroDocumento(documento);

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void shouldReturnTrueWhenCpfIsValid() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PF);
        dto.setNumeroDocumento("52998224725");

        assertTrue(validator.isValid(dto, context));
    }

    // Testes com CNPJ
    @Test
    void shouldReturnFalseWhenCnpjIsWrongLength() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PJ);
        dto.setNumeroDocumento("12345678901");

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void shouldReturnFalseWhenCnpjIsInvalid() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PJ);
        dto.setNumeroDocumento("11111111111111");

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void shouldReturnTrueWhenCnpjIsValid() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setTipo(TipoUsuario.PJ);
        dto.setNumeroDocumento("11444777000161");

        assertTrue(validator.isValid(dto, context));
    }
}