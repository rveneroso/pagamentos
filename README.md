# Sistema de Usuários - Pagamentos

API REST desenvolvida em Spring Boot para gerenciamento de usuários (Pessoa Física e Jurídica).

## Tecnologias

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database
- Bean Validation
- Lombok

## Funcionalidades

- Cadastro de usuários (PF/PJ)
- Validação de CPF/CNPJ
- Validação de e-mail único
- Criptografia de senha (BCrypt)
- API REST com tratamento global de exceções

## Como executar

```bash
mvn spring-boot:run