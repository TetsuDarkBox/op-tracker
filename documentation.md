# 🎴 OP Tracker API - Core System v1.0

O **OP Tracker** é um ecossistema backend desenvolvido em **Spring Boot 3** para centralizar a gestão de coleções e trocas do *One Piece Card Game*. Este sistema foi desenhado para ser escalável, seguro e orientado a performance.

---

## 🏗️ 1. Arquitetura e Design Patterns

A API segue princípios de **Clean Code** e **SOLID**, estruturada em camadas distintas:

* **REST Controllers:** Exposição de endpoints seguindo o padrão Richardson Maturity Model.
* **Service Layer:** Onde reside a "inteligência" do navio. Gere transações (@Transactional) e regras de negócio.
* **Data Access Layer (JPA):** Abstração completa do SQL utilizando Spring Data JPA e TiDB Cloud.
* **DTO Pattern:** Isolamento total das entidades. O cliente nunca toca na estrutura da Base de Dados.
* **Global Exception Handling:** Sistema centralizado que converte exceções Java em respostas JSON profissionais e traduzidas (I18n).

---

## 🔐 2. Ecossistema de Segurança (JWT & BCrypt)

A segurança foi implementada para garantir que os ativos (cartas e dados) dos utilizadores estejam protegidos.

### Fluxo de Autenticação:
1.  **Registo:** A password é "salteada" e encriptada com **BCrypt** antes de tocar no disco.
2.  **Login:** O servidor valida as credenciais e emite um **JWT (JSON Web Token)** assinado com uma chave secreta.
3.  **Autorização:** O `JwtAuthenticationFilter` interseta cada pedido, valida a assinatura do token e injeta o utilizador no `SecurityContext` do Spring.



---

## 📊 3. Modelo de Dados Relacional

O esquema de base de dados foi normalizado para suportar milhares de utilizadores sem perda de performance:

### Entidade `User` (Agregador)
* **One-to-One (`UserProfile`):** Dados de exibição para a comunidade.
* **One-to-One (`UserAddress`):** Informação logística protegida.
* **One-to-One (`UserStats`):** Motor de reputação para trocas seguras.

| Variável | Tipo | Restrição | Descrição |
| :--- | :--- | :--- | :--- |
| `username` | String | Unique, Not Null | Identificador único no sistema. |
| `email` | String | Unique, Not Null | Para comunicações e recuperação. |
| `role` | Enum | Not Null | USER, MODERATOR, ADMIN. |
| `active` | Boolean | Default: True | Controlo de ativação de conta. |



---

## 📡 4. Especificação da API (v1)

### 🔑 Autenticação
| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Público | Cria conta e inicializa sub-módulos. |
| `POST` | `/api/auth/login` | Público | Valida credenciais e retorna JWT. |

### 👤 Perfil & Utilizador (Em Desenvolvimento)
| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Privado | Retorna os dados do utilizador logado. |
| `PUT` | `/api/users/profile` | Privado | Atualiza Bio, Avatar e Links Sociais. |

---

## 🧪 5. Estratégia de Testes

Para garantir que o sistema não falha durante um "Puro de Cartas", implementámos:

* **Testes de Integração:** Correm num perfil isolado (`spring.profiles.active=test`).
* **H2 Database:** Simula o comportamento do TiDB em memória para testes ultra-rápidos.
* **MockMvc:** Testa a camada Controller sem necessidade de um servidor Tomcat real.

---

## ⚙️ 6. Configuração de Ambiente

Para rodar o projeto localmente, as seguintes variáveis/configurações são necessárias no `application.properties`:

```properties
# TiDB Cloud / MySQL Connection
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

# JWT Configuration
jwt.secret=${JWT_SECRET_KEY}
jwt.expiration=86400000 # 24 Horas

---

## ⚙️ 7. Estrutura de Diretórios

com.optracker.api
├── config/             # Beans de Segurança e Filtros JWT
├── controller/         # Handlers de entrada (REST)
├── dto/                # Records de Request/Response
├── entity/             # Objetos de Persistência (JPA)
├── enums/              # Definições de Tipos (Roles, Conditions)
├── exception/          # Tratamento de Erros Global
├── repository/         # Interfaces de Acesso a Dados
└── service/            # Lógica de Negócio e Segurança