## Backend Setup (Java Spring Boot)

**Decisões Técnicas:**
* **Framework:** Spring Boot 3. Escolhido pela robustez, facilidade de configuração e vasto ecossistema para aplicações Enterprise.
* **Build Tool:** Maven. Padrão da indústria para gestão de dependências Java.
* **Dependências Chave:**
    * `Spring Web`: Para expor endpoints RESTful.
    * `Spring Data JPA`: Abstração de acesso a dados (ORM) para agilizar o desenvolvimento.
    * `Lombok`: Redução de código boilerplate (Getters/Setters).
    * `PostgreSQL Driver`: Conector para a base de dados relacional escolhida.

**Estrutura de Pastas:**
O projeto foi dividido em `frontend` e `backend` (monorepo structure) para manter o código organizado mas no mesmo repositório git.

## Dia 2: API e Conexão à Base de Dados

**Progresso:**
* **Modelo de Dados:** Criada a entidade `Card` com suporte a tipos complexos (subtypes) e metadados do jogo (Life, Power, Attribute).
* **Database Seeding:** Implementado `DataSeeder` para povoar a base de dados automaticamente se estiver vazia.
* **API REST:** Implementado `CardController` expondo endpoints `GET` para consumo externo.
* **Teste:** Endpoint `/api/cards` testado com sucesso, retornando JSON.

**Próximo Passo:** Inicializar Frontend em React.

## Dia 3: Populando a Base de Dados

**Desafio:** Inserir centenas de cartas manualmente no código (`new Card`) não é escalável.
**Solução:** Implementar um "JSON Loader".
**Implementação:**
* Criar ficheiro `cards.json` com dados brutos.
* Atualizar `DataSeeder` para usar `ObjectMapper` (Jackson Library) e deserializar o JSON para objetos Java.
* Salvar lista completa via `cardRepository.saveAll()`.