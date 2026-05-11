# 💳 Sistema de Pagamentos

Esta API REST foi desenvolvida para gerenciar fluxos de transferências financeiras entre usuários, com foco em segurança, extensibilidade e documentação rigorosa. O projeto demonstra a aplicação de padrões modernos de desenvolvimento em um ambiente de missão crítica.

## 🛠 Stack Tecnológica
*   **Java 17**: Escolhido para garantir total compatibilidade com o ecossistema de mocks e serviços de autorização externos.
*   **Spring Boot 3.2.5**: Utilização da versão mais estável do framework para suporte nativo a Jakarta EE.
*   **Spring Data JPA**: Abstração de persistência com **H2 Database** (em memória) para facilitar a execução imediata.
*   **Spring Security**: Implementação de segurança Stateless via **API Key**.
*   **SpringDoc OpenAPI (Swagger)**: Documentação viva e interativa da API.
*   **Lombok & Bean Validation**: Redução de boilerplate e garantia de integridade dos dados.

## 🏗 Diferenciais de Arquitetura
*   **Desacoplamento com DTOs**: Implementação do Pattern DTO para garantir que os dados sejam íntegros e independentes das entidades de banco de dados durante todo o ciclo de vida da requisição, evitando a exposição de dados sensíveis e protegendo o contrato da API.
*   **Filtro de Segurança Customizado**: Implementação de um `ApiKeyFilter` que intercepta requisições e valida o acesso antes mesmo de atingir a camada de Controller.
*   **Design RESTful Puro**: Endpoints estruturados sobre substantivos, utilizando verbos HTTP para definir ações.
*   **Resiliência no Processamento**: Estrutura preparada para lidar com falhas temporárias de autorização através de status de erro controlados.

## 🔑 Segurança e Acesso
A API utiliza autenticação por chave de acesso. Para realizar testes:
*   **Header**: `x-api-key`
*   **Valor**: `chave-secreta-configurada` *(Verifique o valor real em seu application.yaml)*

## 📖 Como Executar
1.  **Build**: Execute `./mvnw clean install` ou `mvn clean install`.
2.  **Run**: Execute `./mvnw spring-boot:run`.
3.  **Swagger UI**: Acesse a interface interativa em: `http://localhost:8081/swagger-ui/index.html`

## 🧪 Instruções de Teste
Para validar as regras de negócio e a integração com serviços externos, utilize o roteiro abaixo no Swagger UI:

1.  **Autorização**: Clique no botão **Authorize** e insira a `x-api-key`. Sem este passo, as requisições retornarão `401 Unauthorized`.
2.  **Massa de Dados Automática**: O banco de dados H2 é populado na inicialização via `import.sql`. Utilize os seguintes registros:
    *   **Usuário Comum (Pagador)**: `ID 1` (João Silva) - Saldo: R$ 200,00.
    *   **Lojista (Recebedor)**: `ID 7` (Mercado Central LTDA) - Saldo inicial: R$ 0,00.
3.  **Cenários de Validação**:
    *   **Sucesso e Autorização**: Realize um `POST /pagamentos` com dados válidos. A operação depende da resposta do serviço externo de autorização; caso o mock retorne "Não Autorizado", a transação será revertida para garantir a consistência.
    *   **Regra de Saldo**: Tente um pagamento com valor superior ao saldo do `ID 1`. A API retornará `409 Conflict`.
    *   **Restrição de Lojista**: Tente realizar um pagamento partindo do `ID 7`. O sistema impedirá a transação (Lojistas apenas recebem).
    *   **Validação de Documento**: O campo `numeroDocumento` aceita 11 dígitos (PF) ou 14 dígitos (PJ).

---

## 🚀 Evoluções Futuras & Escalabilidade
Para suportar cenários de alta volumetria, o projeto foi desenhado prevendo as seguintes evoluções:
1.  **Mensageria e Assincronismo**: Migração para um modelo baseado em eventos com **RabbitMQ** ou **Kafka**.
2.  **Estratégia de Sharding**: Implementação de Sharding por ID de usuário para paralelismo de processamento.
3.  **Observabilidade**: Exportação de métricas para **Prometheus** e **Grafana** via **Micrometer Tracing**.

---

### Notas de Implementação
O projeto foi estruturado com foco em clareza e manutenibilidade, aplicando padrões de projeto que visam a colaboração em equipe e a evolução sustentável do software. A escolha das tecnologias e a organização das camadas buscam alinhar o desenvolvimento às boas práticas do ecossistema Java corporativo.