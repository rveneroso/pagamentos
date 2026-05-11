package br.gov.mg.tce.pagamentos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    private ApiKeyFilter apiKeyFilter;

    private final String VALID_KEY = "super-secret-key";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        apiKeyFilter = new ApiKeyFilter(VALID_KEY);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenApiKeyIsValid() throws ServletException, IOException {
        when(request.getHeader("x-api-key")).thenReturn(VALID_KEY);

        apiKeyFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("api-client", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturnUnauthorizedWhenApiKeyIsInvalid() throws ServletException, IOException {
        when(request.getHeader("x-api-key")).thenReturn("wrong-key");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        apiKeyFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("API Key inválida ou ausente"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldReturnUnauthorizedWhenApiKeyIsMissing() throws ServletException, IOException {
        when(request.getHeader("x-api-key")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        apiKeyFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldNotFilterInternalPaths() {
        when(request.getRequestURI()).thenReturn("/h2-console/index.html");

        boolean shouldNotFilterH2 = apiKeyFilter.shouldNotFilter(request);

        assertTrue(shouldNotFilterH2, "Deveria ignorar o path do H2 Console");

        when(request.getRequestURI()).thenReturn("/error");

        assertTrue(apiKeyFilter.shouldNotFilter(request), "Deveria ignorar o path de erro");
    }
}