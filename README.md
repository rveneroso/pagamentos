# 💳 Sistema de Pagamentos

Esta API REST foi desenvolvida para gerenciar fluxos de transferências financeiras entre usuários, com foco em **alta disponibilidade**, **resiliência** e **integridade transacional**. O projeto demonstra a aplicação de padrões avançados de arquitetura para ambientes de missão crítica.

## 🛠 Stack Tecnológica
* **Java 17**: Utilizado para garantir compatibilidade com as funcionalidades modernas de records e selagem de classes.
* **Spring Boot 3.2.5**: Base da aplicação, com suporte nativo a Jakarta EE.
* **Spring Data JPA**: Abstração de persistência com **H2 Database** para execução imediata e facilitada.
* **Spring Security**: Implementação de segurança Stateless via **API Key** customizada.
* **SpringDoc OpenAPI (Swagger)**: Documentação viva e interativa da API.
* **Lombok & Bean Validation**: Garantia de integridade dos dados com redução drástica de boilerplate.

## 🏗 Diferenciais de Arquitetura e Resiliência
* **Transactional Outbox Pattern**: Implementação de mensageria confiável. As notificações de pagamento são persistidas na mesma transação da regra de negócio, garantindo consistência eventual mesmo se o serviço de e-mail estiver offline no momento do pagamento.
* **Modelo de Domínio Rico**: As regras de negócio, cálculos de saldo e transições de estado estão encapsuladas diretamente nas entidades (`Usuario`, `Pagamento`), seguindo princípios de Clean Code e facilitando testes unitários puramente Java.
* **Processamento Assíncrono e Retentativas**: Utilização de workers agendados (`EmailOutboxProcessor` e `AuthorizationRelayWorker`) para gerenciar falhas temporárias em serviços externos (Autorização e Notificação), evitando que o usuário final sofra com instabilidades de terceiros.
* **Desacoplamento com DTOs**: Proteção total do contrato da API e das entidades de banco de dados, evitando a exposição de dados sensíveis e garantindo independência evolutiva.

## 🧪 Estratégia de Testes
O projeto possui uma cobertura rigorosa baseada na pirâmide de testes:
* **Testes de Unidade (JUnit 5)**: Validação das invariantes de negócio e cálculos matemáticos nas entidades, rodando de forma instantânea sem dependência de framework.
* **Testes de Integração de API (MockMvc)**: Validação de segurança, serialização JSON, filtros de API Key e contratos de Controller.
* **Testes de Orquestração (Mockito)**: Simulação de falhas de rede, negações e timeouts para validar a resiliência dos workers e services sob estresse.

## 🔑 Segurança e Acesso
A API utiliza autenticação por chave de acesso via Header customizado:
* **Header**: `x-api-key`
* **Valor Padrão**: `secret-key-123` *(Configurável via application-test.properties ou variáveis de ambiente)*

## 📖 Como Executar
1.  **Build**: Execute `./mvnw clean install` ou `mvn clean install`.
2.  **Run**: Execute `./mvnw spring-boot:run`.
3.  **Swagger UI**: Acesse: `http://localhost:8081/swagger-ui/index.html`

## 🕹 Roteiro de Teste (Cenários de Negócio)
Utilize o Swagger UI para validar o comportamento do sistema:

1.  **Massa de Dados**: O sistema inicia com o `ID 1` (João Silva - PF) com R$ 200,00 e o `ID 7` (Mercado Central - PJ) com saldo zero.
2.  **Fluxo de Sucesso**: Realize um pagamento de PF para PJ. O sistema debitará o pagador, creditará o recebedor e agendará uma notificação no Outbox.
3.  **Resiliência de Autorização**: Se o serviço externo de autorização falhar (timeout/erro 500), o pagamento ficará com status `ERRO_AUTORIZACAO`. O `AuthorizationRelayWorker` tentará reprocessá-lo automaticamente.
4.  **Validações Financeiras**:
    * **Saldo Insuficiente**: Retorna `409 Conflict`.
    * **Restrição Lojista**: Lojistas (PJ) são impedidos de iniciar pagamentos, agindo apenas como recebedores.
    * **Auto-pagamento**: O sistema impede transferências para o mesmo CPF/CNPJ.

---

## 🚀 Evoluções Futuras
1.  **Mensageria Distribuída**: Substituição do Outbox baseado em DB por **RabbitMQ** ou **Kafka** para suportar escalabilidade horizontal massiva.
2.  **Observabilidade**: Integração com **Prometheus** e **Grafana** via Micrometer para monitoramento em tempo real de throughput e taxas de erro.
3.  **Containers**: Dockerização da aplicação e criação de manifestos Helm para orquestração em clusters Kubernetes.

---
### Notas de Implementação
Este projeto foi estruturado para suportar o crescimento da volumetria sem comprometer a confiabilidade dos dados, aplicando padrões de design que visam a manutenibilidade e a evolução sustentável em um ecossistema corporativo de alta criticidade.