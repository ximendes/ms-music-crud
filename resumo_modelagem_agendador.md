# Resumo arquitetural --- Agendador de eventos (Java + DynamoDB)

## Objetivo

Construir um agendador genérico no qual cada usuário pode cadastrar N
agendamentos, com diferentes tipos de eventos, recorrências e contratos
de payload.

Arquitetura considerada: Java, DynamoDB, Amazon EventBridge Scheduler,
SNS e SQS.

## 1. Modelo conceitual

Cada agendamento é uma entidade independente (`ScheduledEvent`),
composta por:

-   Identificação do usuário e do agendamento.
-   Nome e identificador do produto/agrupamento (`productKey`).
-   Status.
-   Regra de recorrência.
-   Payload específico do evento.
-   Versão do contrato.
-   Informações de auditoria.

Um agendamento pode ter vários dias de execução, mas representa apenas
um schedule no EventBridge. Por exemplo, segunda, terça e quarta às
09:00 é um único agendamento recorrente.

## 2. Contrato de domínio Java

### ScheduledEvent

``` java
public record ScheduledEvent(
    String id,
    String userId,
    String name,
    String productKey,
    ScheduleStatus status,
    Recurrence recurrence,
    String payload,
    Integer schemaVersion,
    Instant createdAt,
    Instant updatedAt
) {}
```

### Recurrence

``` java
public record Recurrence(
    Frequency frequency,
    LocalTime time,
    ZoneId timezone,
    List<DayOfWeek> daysOfWeek,
    Integer dayOfMonth,
    LocalDate startDate,
    LocalDate endDate
) {}
```

### Enums

``` java
public enum Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}
```

``` java
public enum ScheduleStatus {
    PROVISIONING,
    ACTIVE,
    PAUSED,
    CANCELLED
}
```

O estado de sincronização com o EventBridge deve ser separado do estado
de negócio. Uma opção é manter `syncStatus`, com valores como `PENDING`,
`SYNCED` e `FAILED`.

## 3. Exemplos de recorrência

### Semanal --- segunda, terça e quarta às 09:00

``` json
{
  "frequency": "WEEKLY",
  "time": "09:00",
  "timezone": "America/Sao_Paulo",
  "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY"],
  "startDate": "2026-11-09",
  "endDate": null
}
```

Expressão cron do EventBridge:

``` text
cron(0 9 ? * MON,TUE,WED *)
```

### Mensal --- todo dia 10 às 08:00

``` json
{
  "frequency": "MONTHLY",
  "time": "08:00",
  "timezone": "America/Sao_Paulo",
  "dayOfMonth": 10,
  "startDate": "2026-11-10",
  "endDate": null
}
```

Expressão cron:

``` text
cron(0 8 10 * ? *)
```

### Diário --- todos os dias às 09:00

``` json
{
  "frequency": "DAILY",
  "time": "09:00",
  "timezone": "America/Sao_Paulo",
  "startDate": "2026-11-09",
  "endDate": null
}
```

Expressão cron:

``` text
cron(0 9 * * ? *)
```

### Regras definidas

-   Recorrência semanal aceita múltiplos dias.
-   Recorrência mensal aceita dias entre 1 e 31.
-   Se o dia não existir no mês, a execução é ignorada. Exemplo: dia 31
    em fevereiro.
-   `startDate` e `endDate` delimitam a validade da recorrência.
-   O timezone utiliza identificadores IANA, como `America/Sao_Paulo`.
-   A recorrência deve ser validada antes de persistir ou provisionar o
    schedule.

## 4. Modelagem no DynamoDB

Tabela sugerida: `ScheduledEvents`.

Um item por agendamento, usando uma chave composta:

```text
PK = USER#123
SK = HUB_RELATORIO#abc-123
```

O prefixo da `SK` é o identificador do produto/agrupamento, e não um
`eventType` genérico. A modelagem adotada não mantém um atributo
`eventType` separado: o `productKey` identifica o agrupamento e também
pode ser utilizado pela aplicação para direcionar o processamento.

  Atributo                     Tipo DynamoDB   Descrição
  ---------------------------- --------------- ---------------------------------
  `PK`                         String          `USER#{userId}`
  `SK`                         String          `{productKey}#{scheduleId}`
  `scheduleId`                 String          UUID do agendamento
  `name`                       String          Nome amigável
  `status`                     String          Estado do agendamento
  `syncStatus`                 String          Estado da sincronização com AWS
  `recurrence`                 Map             Regra de recorrência
  `payload`                    String          JSON serializado
  `schemaVersion`              Number          Versão do contrato do payload
  `timezone`                   String          Fuso horário IANA
  `eventBridgeScheduleName`    String          Nome no EventBridge
  `eventBridgeScheduleGroup`   String          Grupo no EventBridge
  `createdAt`                  Number          Timestamp de criação
  `updatedAt`                  Number          Timestamp de atualização

### Exemplo de item

``` json
{
  "PK": "USER#123",
  "SK": "HUB_RELATORIO#abc-123",
  "scheduleId": "abc-123",
  "name": "Gerar relatório",
  "status": "ACTIVE",
  "syncStatus": "SYNCED",
  "schemaVersion": 1,
  "timezone": "America/Sao_Paulo",
  "recurrence": {
    "frequency": "WEEKLY",
    "time": "09:00",
    "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY"],
    "startDate": "2026-11-09"
  },
  "payload": "{\"reportId\":\"123\",\"format\":\"PDF\"}",
  "eventBridgeScheduleName": "abc-123",
  "eventBridgeScheduleGroup": "user-schedules",
  "createdAt": 1793900000,
  "updatedAt": 1793900000
}
```

Com essa chave, é possível atender a dois padrões de consulta usando `Query`:

- Todos os agendamentos do usuário: consultar pela `PK = USER#{userId}`.
- Somente os agendamentos de um produto: consultar pela mesma `PK` e
  aplicar `begins_with(SK, "HUB_RELATORIO#")`, por exemplo.

O padrão geral da chave de ordenação é `SK = {productKey}#{scheduleId}`.
Exemplos: `HUB_RELATORIO#abc-123`, `HUB_PAGAMENTOS#def-456` e
`HUB_EMAIL#ghi-789`. O prefixo identifica o produto/agrupamento responsável
pelo agendamento; o sufixo identifica o agendamento.

Não é necessário criar um GSI inicialmente para esses dois padrões de
consulta. Adicione índices secundários apenas quando surgir outro padrão
de acesso que não possa ser atendido pelas chaves existentes.

Como a `SK` contém o `productKey`, a aplicação deve conhecê-lo para montar
a chave completa em operações como `GetItem` e `UpdateItem`. O atributo
`productKey` pode existir no modelo Java, mas não precisa ser duplicado
como atributo DynamoDB se já estiver representado na `SK`.

## 5. Decisão: String ou Map para o payload?

**Decisão: armazenar o payload como String contendo JSON serializado.**

Justificativas:

-   O agendador não interpreta o conteúdo do payload.
-   O JSON é transportado entre componentes.
-   Não há necessidade de consultas sobre propriedades internas.
-   Não existem atualizações parciais: quando necessário, o payload
    inteiro será sobrescrito.
-   Evita conversão recursiva entre `JsonNode` e `AttributeValue`.

Na aplicação Java:

  Responsabilidade           Representação
  -------------------------- --------------------
  Persistência               `String`
  Manipulação dinâmica       Jackson `JsonNode`
  Processamento de negócio   DTO específico

O `schemaVersion` permite controlar a evolução dos contratos de cada
tipo de evento. O payload deve ser validado como JSON na entrada, mesmo
que seja armazenado como texto.

## 6. Separação entre domínio e infraestrutura

O domínio não deve conhecer a sintaxe cron do EventBridge.

  -----------------------------------------------------------------------
  Componente                          Responsabilidade
  ----------------------------------- -----------------------------------
  `Recurrence`                        Representa a regra de negócio

  `EventBridgeCronFactory`            Converte recorrência em expressão
                                      cron do EventBridge

  `SchedulerPort`                     Define as operações de agendamento
                                      necessárias à aplicação

  `EventBridgeSchedulerAdapter`       Implementa a integração com AWS
  -----------------------------------------------------------------------

Exemplo de fluxo:

``` text
Recurrence
    |
    v
EventBridgeCronFactory
    |
    v
cron(0 9 ? * MON,TUE,WED *)
    |
    v
EventBridgeSchedulerAdapter
```

A aplicação depende da interface `SchedulerPort`, não diretamente do SDK
da AWS. A conversão de recorrência para cron fica na infraestrutura.

## 7. Operações e ciclo de vida

  Operação    DynamoDB     EventBridge
  ----------- ------------ -----------------------------
  Criar       PutItem      CreateSchedule
  Consultar   GetItem      ---
  Listar      Query        ---
  Atualizar   UpdateItem   UpdateSchedule
  Pausar      UpdateItem   UpdateSchedule (`DISABLED`)
  Reativar    UpdateItem   UpdateSchedule (`ENABLED`)
  Cancelar    UpdateItem   DeleteSchedule

O cancelamento pode ser lógico no DynamoDB (`CANCELLED`), preservando
histórico, enquanto o schedule é removido do EventBridge.

O usuário só pode gerenciar seus próprios agendamentos. O `userId` deve
vir da identidade autenticada, não de um valor confiado no corpo da
requisição. Cada operação deve validar a propriedade do agendamento.

## 8. Arquitetura de execução

``` text
Java Scheduler API
        |
        v
     DynamoDB
        |
        | Provisionamento / sincronização
        v
EventBridge Scheduler
        |
        v
       SNS
        |
        v
       SQS
        |
        v
Java Consumer
        |
        v
Identifica o produto pelo productKey e interpreta o payload
```

O consumidor utiliza o identificador do produto (`productKey`, por exemplo
`HUB_RELATORIO`) para direcionar o processamento e desserializa o payload
para o DTO correspondente. Se um produto precisar distinguir vários tipos
de evento internamente, essa distinção pode fazer parte do contrato do
payload ou de um atributo específico, conforme a necessidade de negócio.

## 9. Pontos de atenção

1.  **Consistência:** DynamoDB e EventBridge não compartilham uma
    transação distribuída. Implementar operações idempotentes, retry e
    reconciliação. Transactional Outbox com DynamoDB Streams é uma opção
    para coordenar persistência e provisionamento.
2.  **Timezone:** utilizar identificadores IANA e considerar as regras
    de fuso horário ao converter datas de início e término.
3.  **Precisão:** o EventBridge Scheduler tem precisão de invocação de
    60 segundos; não deve ser tratado como um mecanismo de execução com
    precisão de milissegundos.
4.  **Dia mensal:** uma expressão cron para o dia 31 não executa nos
    meses que não possuem esse dia, alinhando-se à política de ignorar a
    ocorrência.
5.  **Idempotência:** o consumer deve tolerar entregas repetidas; uma
    mensagem duplicada não pode provocar efeitos de negócio duplicados,
    como dois pagamentos.
6.  **Falhas de entrega:** configurar retry policy e DLQ no EventBridge
    Scheduler e monitorar falhas.
7.  **Limite DynamoDB:** cada item tem limite de 400 KB, incluindo nomes
    e valores dos atributos.
8.  **Validação:** validar frequência, dias da semana, dia do mês,
    timezone, intervalo de datas e JSON do payload antes de persistir.

## 10. Decisões arquiteturais consolidadas

  Decisão               Escolha
  --------------------- ----------------------------------------
  Entidade principal    `ScheduledEvent`
  Recorrência           Objeto estruturado `Recurrence`
  Payload               String JSON
  Manipulação JSON      Jackson `JsonNode`
  Persistência          DynamoDB
  Particionamento       Por usuário (`USER#{userId}`)
  Ordenação             Por produto e agendamento (`{productKey}#{scheduleId}`)
  Consulta por produto  `Query` + `begins_with(SK, "{productKey}#")`
  Recorrência semanal   Múltiplos dias em um schedule
  Recorrência mensal    Ignorar dias inexistentes
  Gerenciamento         Pelo próprio usuário
  Agendamento externo   EventBridge Scheduler
  Integração            Port & Adapter
  Versionamento         `schemaVersion`
  Sincronização         Estado separado, retry e reconciliação

## Conclusão

A modelagem mantém o agendador independente dos contratos de negócio dos
eventos. Novos produtos/agrupamentos, como `HUB_RELATORIO`,
`HUB_PAGAMENTOS` e `HUB_EMAIL`, podem ser adicionados sem alterar a
estrutura principal da tabela DynamoDB. O prefixo da `SK` permite listar
os agendamentos de um produto específico sem GSI, enquanto a `PK` permite
listar todos os agendamentos do usuário.

A regra de recorrência permanece expressa em conceitos de domínio
(`WEEKLY`, `MONDAY`, `09:00`), enquanto a infraestrutura traduz essa
regra para a tecnologia de agendamento utilizada.
