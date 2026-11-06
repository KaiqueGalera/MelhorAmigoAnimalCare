# VetClínica — Melhor Amigo Animal Care

Sistema de gestão de atendimentos para clínicas veterinárias, feito no trabalho prático de
*Verificação, Validação e Teste de Software* (IFSP São Carlos).

O atendimento passa por estas etapas: abertura (pronto atendimento ou a partir de um agendamento), registro clínico
(sinais vitais, anamnese, diagnóstico), prescrições, exames e conclusão. O back-end também permite cancelar
e reabrir em até 24h.

| Parte | Tecnologia | Pasta |
|---|---|---|
| Back-end | Spring Boot 3, Java 21, SQLite (`JdbcTemplate`), JWT | raiz (`src/`) |
| Front-end | React 19 + Vite 8 + React Router 7 | [`frontend/`](frontend/) |

## Pré-requisitos

- **JDK 21** (não basta um JRE: o Maven precisa do compilador). Confira com `java -version` e `javac -version`.
- **Node.js 20.19+ ou 22.12+** (exigência do Vite 8). Confira com `node --version`.

## Como executar

### 1. Back-end (porta 8080)

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

- O banco SQLite é criado automaticamente no arquivo `atendimentos.db`, na raiz.
- Se o `java` do PATH não for um JDK 21, informe o caminho: `JAVA_HOME=/caminho/do/jdk-21 ./mvnw spring-boot:run`.
- Swagger UI: http://localhost:8080/api/v1/api-docs

### 2. Front-end (porta 5173)

```bash
cd frontend
npm install
npm run dev                     # http://localhost:5173
```

Em desenvolvimento, o Vite encaminha `/api` para `http://localhost:8080`, então não é preciso configurar CORS.
Detalhes em [`frontend/README.md`](frontend/README.md).

### 3. Uso

1. Acesse http://localhost:5173 e crie uma conta em **Cadastre-se**. Todos os endpoints exigem login.
2. Entre com o e-mail e a senha cadastrados.
3. Abra um atendimento (pronto atendimento ou com agendamento). Os identificadores de animal, agendamento e exame
   são gerados automaticamente, porque o sistema não tem cadastro de animais nem catálogo de exames.
4. Registre os dados clínicos, prescrições e exames. A conclusão exige sinais vitais, pelo menos um diagnóstico
   e nenhuma prescrição em aberto.

## Testes

```bash
./mvnw test                                                    # todos os testes
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage   # mutação: target/pit-reports/index.html
```

Suítes em `src/test/java/br/ifsp/demo/suites/`: `UnitTestsSuite`, `TDDTestSuite`, `FunctionalTestSuite`,
`StructuralTestSuite` e `MutationTestSuite`.

- **Cobertura estrutural:** 100% de branches nas classes do domínio (`model` e `service`). No IntelliJ:
  *Run 'UnitTestsSuite' with Coverage* com **Branch coverage** (modo *Tracing*) ativado.
- **Mutação:** 100% de *test strength* e nenhum mutante equivalente.
