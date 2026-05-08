package org.tce.pagamentos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class ApiKeyFilter extends OncePerRequestFilter {

    private String apiKey;

    private static final String HEADER_NAME = "x-api-key";

    public ApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestKey = request.getHeader(HEADER_NAME);

        // Verifica se o header está presente na requisição e se seu valor é o da api-key configurada na aplicação.
        if (requestKey == null || !requestKey.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("API Key inválida ou ausente");
            return;
        }

        // Uma vez que a api-key foi validada, cria uma autenticação válida sem credenciais e sem roles.
        var auth = new UsernamePasswordAuthenticationToken("api-client", null, Collections.emptyList());
        // Adiciona a autenticação acima ao contexto de segurança do Spring
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    // Permite que a console de gerenciado do h2 e rotas de error funcionem sem o header x-api-key
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/h2-console") || path.equals("/error");
    }
}