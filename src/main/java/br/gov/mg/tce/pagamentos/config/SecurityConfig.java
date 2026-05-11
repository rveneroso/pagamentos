package br.gov.mg.tce.pagamentos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${security.api-key}")
    private String apiKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Instancia ApiKeyFilter passando o valor configurado na aplicação
        ApiKeyFilter apiKeyFilter = new ApiKeyFilter(apiKey);

        return http
                // Desabilita o Cross-Site Request Forgery (desnecessário quando se usa api-key)
                .csrf(csrf -> csrf.disable())
                // Necessário para usar a console do H2
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Sem criação de sessões nem armazenamento de informações do usuário em memória
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Libera o acesso à console do H2 sem autenticação
                        .requestMatchers("/h2-console/**").permitAll()
                        // Exige autenticação para quaisquer outras rotas
                        .anyRequest().authenticated()
                )
                // Executa ApiKeyFilter antes de validar a autenticação do usuário
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
