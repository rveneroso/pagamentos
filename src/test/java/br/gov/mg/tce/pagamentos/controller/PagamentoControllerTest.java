package br.gov.mg.tce.pagamentos.controller;

import br.gov.mg.tce.pagamentos.dto.request.PagamentoRequestDTO;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.service.PagamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "security.api-key=secret-key-123")
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagamentoService pagamentoService;

    private final String API_KEY_HEADER = "x-api-key";
    private final String VALID_KEY = "secret-key-123";

    @Test
    void shouldCreatePagamentoWithSuccess() throws Exception {
        PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();
        requestDTO.setPagadorDocumento("52998224725");
        requestDTO.setRecebedorDocumento("11222333000181");
        requestDTO.setValor(new BigDecimal("100.00"));

        Pagamento pagamentoSalvo = new Pagamento();
        pagamentoSalvo.setId(1L);

        when(pagamentoService.criarPagamento(any(PagamentoRequestDTO.class))).thenReturn(pagamentoSalvo);

        mockMvc.perform(post("/pagamentos")
                        .header(API_KEY_HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenDtoIsInvalid() throws Exception {
        PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();

        mockMvc.perform(post("/pagamentos")
                        .header(API_KEY_HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWithoutApiKey() throws Exception {
        PagamentoRequestDTO requestDTO = new PagamentoRequestDTO();

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAllPagamentos() throws Exception {
        Pagamento p1 = new Pagamento(); p1.setId(1L);
        Pagamento p2 = new Pagamento(); p2.setId(2L);
        when(pagamentoService.listarTodos()).thenReturn(java.util.List.of(p1, p2));

        mockMvc.perform(get("/pagamentos")
                        .header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldReturnPagamentoById() throws Exception {
        Long idExistente = 1L;
        Pagamento pagamento = new Pagamento();
        pagamento.setId(idExistente);

        when(pagamentoService.buscarPorId(idExistente)).thenReturn(pagamento);

        mockMvc.perform(get("/pagamentos/{id}", idExistente)
                        .header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente));
    }

    @Test
    void shouldReturnPagamentoByStatus() throws Exception {
        var statusBusca = StatusPagamento.CONCLUIDO;
        Pagamento p = new Pagamento();
        p.setId(10L);
        p.setStatus(statusBusca);

        when(pagamentoService.buscarPorStatus(statusBusca)).thenReturn(java.util.List.of(p));

        mockMvc.perform(get("/pagamentos/status/{status}", statusBusca)
                        .header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void shouldReturnErrorWhenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/pagamentos/status/STATUS_QUE_NAO_EXISTE")
                        .header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isBadRequest());
    }
}