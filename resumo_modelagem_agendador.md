# Refatoração da integração AWS EventBridge Scheduler

## 1. Contexto e objetivo

O projeto já possui:

- CRUD de agendamentos implementado.
- Persistência no DynamoDB.
- Integração com AWS EventBridge Scheduler implementada.
- Regras de negócio e modelos existentes.

Entretanto, a implementação atual da integração com o EventBridge apresenta problemas de organização e manutenção, como:

- Muitas responsabilidades concentradas em uma única classe ou método.
- Excesso de condicionais (`if/else` e `switch`) dentro dos mesmos métodos.
- Mistura de regras de negócio com detalhes de infraestrutura.
- Complexidade desnecessária na construção das requisições AWS.
- Possível duplicação de lógica.
- Baixa coesão e dificuldade de testar componentes isoladamente.

**O objetivo não é criar uma nova integração, mas revisar e refatorar a implementação existente, tornando o código mais simples, coeso, legível, testável e fácil de manter.**

A refatoração deve preservar o comportamento funcional atual, exceto quando forem identificados bugs ou inconsistências. Qualquer alteração de comportamento deverá ser previamente discutida e aprovada.

## 2. Princípio fundamental: entender antes de alterar

Antes de modificar qualquer código:

1. Analise integralmente a implementação atual da integração com o EventBridge.
2. Identifique todas as classes envolvidas.
3. Entenda o fluxo completo de criação, atualização, pausa, reativação e exclusão.
4. Identifique as responsabilidades de cada classe e método.
5. Identifique condicionais excessivas e responsabilidades misturadas.
6. Identifique dependências entre domínio, aplicação e infraestrutura.
7. Analise os testes existentes e o comportamento esperado.
8. Identifique oportunidades de refatoração sem introduzir abstrações desnecessárias.

Não presuma que a implementação atual está errada em todos os aspectos. Preserve as partes que já apresentam boa separação de responsabilidades.

## 3. Objetivo da refatoração

Busque uma implementação orientada pelos seguintes princípios:

- Single Responsibility Principle (SRP).
- Separation of Concerns.
- Clean Code.
- Clean Architecture.
- Baixo acoplamento.
- Alta coesão.
- Composição em vez de condicionais excessivas.
- Testabilidade.
- Simplicidade.

Evite criar uma arquitetura excessivamente fragmentada.

A intenção não é transformar cada `if` em uma classe ou criar abstrações para operações triviais.

A intenção é que cada componente tenha uma responsabilidade clara e que os métodos expressem suas intenções sem concentrar múltiplas decisões e transformações.

## 4. Separação de responsabilidades desejada

Avalie a possibilidade de organizar a implementação em responsabilidades como:

**Application Service / Use Case**
- Orquestra o fluxo da operação.
- Coordena persistência e integração.
- Não conhece detalhes do AWS SDK.

**Port / Gateway**
- Define o contrato de interação com o serviço de agendamento.
- Não expõe tipos específicos da AWS.

**EventBridge Scheduler Adapter**
- Implementa a comunicação com a AWS.
- Executa operações de criação, atualização e exclusão.
- Trata particularidades da API do Scheduler.

**Cron Expression Factory**
- Responsável exclusivamente pela conversão de recorrências para expressões cron.
- Não executa chamadas AWS.
- Não acessa repositórios.

**Request Mapper / Factory**
- Responsável por construir os objetos de requisição do AWS SDK.
- Mantém a montagem dos requests separada da execução das chamadas externas.

**Configuration**
- Centraliza configurações do cliente AWS, grupo de agendamento, tópico SNS e Execution Role.

Essas são responsabilidades de referência, não uma exigência de criação de classes com esses nomes.

Primeiro identifique o que já existe e reutilize componentes adequados.

## 5. Convenções que devem ser preservadas

### Nomenclatura

Preserve os padrões existentes de:

- Classes Java.
- Interfaces.
- Records.
- Enums.
- Métodos.
- Atributos.
- DTOs.
- Entidades.
- Atributos e chaves do DynamoDB.
- Estrutura de pacotes.

Não renomeie componentes existentes sem uma justificativa técnica e minha aprovação.

### Representação dos dias da semana

O projeto já utiliza um array numérico para representar os dias da semana:

| Valor | Dia |
|---|---|
| 1 | Domingo |
| 2 | Segunda-feira |
| 3 | Terça-feira |
| 4 | Quarta-feira |
| 5 | Quinta-feira |
| 6 | Sexta-feira |
| 7 | Sábado |

Essa convenção deve ser preservada em todo o domínio e nos contratos existentes.

A conversão para os valores aceitos pelo AWS EventBridge deve ocorrer exclusivamente na camada de infraestrutura.

Exemplo:

`[1, 3, 5]` deve gerar `SUN,TUE,THU` na expressão cron.

Não introduza outra representação para os dias da semana sem necessidade.

## 6. Análise crítica das condicionais

Um dos principais objetivos é reduzir a complexidade dos métodos atuais.

Analise especialmente métodos que:

- Executam vários `if/else` para decidir como criar um agendamento.
- Misturam DAILY, WEEKLY e MONTHLY em um único fluxo complexo.
- Constroem expressões cron e requests AWS no mesmo método.
- Tratam criação, atualização e exclusão com responsabilidades sobrepostas.
- Misturam validação, transformação e execução de chamadas externas.
- Possuem múltiplas responsabilidades dependendo do status do agendamento.

Considere alternativas como:

- Métodos privados especializados.
- Factories.
- Mappers.
- Estratégias específicas por tipo de recorrência, quando justificadas.
- Composição de componentes pequenos.
- Polimorfismo, quando realmente reduzir o acoplamento.

**Não elimine condicionais apenas por estética.** Um `switch` simples e coeso pode ser melhor do que uma hierarquia desnecessária de classes.

O critério é reduzir complexidade e melhorar a clareza, não apenas reduzir a quantidade de linhas ou condicionais.

## 7. Requisitos funcionais e técnicos

A refatoração deve preservar as funcionalidades existentes:

- Criação de agendamentos.
- Atualização de agendamentos.
- Exclusão de agendamentos.
- Pausa e reativação, quando suportadas.
- Recorrência diária, semanal e mensal.
- Recorrência semanal com múltiplos dias.
- Timezone IANA.
- Payload JSON genérico.
- Integração com SNS como target do EventBridge Scheduler.

Considere também:

- AWS SDK for Java 2.x.
- FlexibleTimeWindow.
- Execution Role.
- Idempotência.
- Retry e tratamento de throttling.
- Exceções específicas da AWS.
- Consistência entre DynamoDB e EventBridge.

Não modifique regras funcionais ou contratos externos como parte de uma simples refatoração sem antes discutir o impacto.

## 8. Uso obrigatório da skill grillme

Utilize a skill `grillme` para esclarecer dúvidas antes de tomar decisões que possam impactar o projeto.

Questione-me quando encontrar:

- Mais de uma abordagem possível de refatoração.
- Necessidade de alterar contratos.
- Necessidade de modificar o modelo de domínio.
- Ambiguidades na implementação atual.
- Regras de negócio não documentadas.
- Possíveis bugs cujo comportamento esperado não esteja claro.
- Oportunidades de simplificação que alterem o comportamento.
- Necessidade de introduzir novas abstrações.

Ao fazer perguntas:

1. Explique o problema identificado.
2. Apresente as alternativas possíveis.
3. Explique brevemente os impactos de cada alternativa.
4. Recomende uma abordagem quando houver fundamento técnico.
5. Aguarde minha decisão.

Não assuma comportamentos ou regras de negócio com base apenas na interpretação do código.

Não faça perguntas sobre decisões que já estejam claramente documentadas.

## 9. Estratégia de execução

Execute a tarefa nas seguintes etapas:

### Etapa 1 — Diagnóstico da implementação atual

Apresente:

- Classes envolvidas.
- Fluxo atual de execução.
- Responsabilidades identificadas.
- Principais pontos de complexidade.
- Condicionais excessivas.
- Acoplamentos.
- Duplicações.
- Problemas de testabilidade.
- Oportunidades de melhoria.

Não altere código nesta etapa.

### Etapa 2 — Proposta de refatoração

Apresente uma proposta objetiva contendo:

- Estrutura sugerida de classes.
- Responsabilidade de cada componente.
- Métodos que serão simplificados.
- Responsabilidades que serão extraídas.
- Componentes que serão mantidos.
- Componentes que serão modificados ou removidos.
- Benefícios esperados.

Priorize a menor mudança estrutural capaz de resolver os problemas identificados.

### Etapa 3 — Esclarecimentos

Utilize a skill `grillme` para resolver dúvidas e validar decisões que não estejam estabelecidas.

Aguarde minhas respostas antes de prosseguir com as decisões dependentes delas.

### Etapa 4 — Refatoração

Implemente as mudanças aprovadas.

Regras:

- Preserve o comportamento funcional.
- Preserve os contratos existentes.
- Evite mudanças fora do escopo.
- Não duplique componentes.
- Não introduza abstrações desnecessárias.
- Não altere a persistência sem autorização.
- Mantenha o código compatível com as versões existentes do projeto.

### Etapa 5 — Testes e validação

Execute os testes existentes e crie ou adapte testes para validar:

- Conversão dos dias da semana.
- Expressões cron.
- Recorrências DAILY, WEEKLY e MONTHLY.
- Construção das requisições AWS.
- Criação, atualização e exclusão.
- Pausa e reativação.
- Tratamento de exceções.
- Idempotência, conforme a implementação existente.

Garanta que os testes não dependam de chamadas reais à AWS.

### Etapa 6 — Comparação final

Apresente uma comparação entre a implementação anterior e a refatorada.

Destaque:

- Responsabilidades antes misturadas que foram separadas.
- Métodos simplificados.
- Condicionais removidas ou reorganizadas.
- Abstrações introduzidas e suas justificativas.
- Melhorias de testabilidade.
- Comportamentos preservados.
- Possíveis limitações restantes.

## 10. Restrições

- Não recrie a integração do zero.
- Não recrie o CRUD.
- Não substitua os padrões existentes por convenções próprias.
- Não altere o modelo de dados sem autorização.
- Não faça uma refatoração massiva sem diagnóstico.
- Não adicione design patterns sem necessidade real.
- Não transforme métodos simples em estruturas complexas.
- Não altere o comportamento funcional silenciosamente.
- Não implemente antes de apresentar o diagnóstico e a proposta.

## Primeira ação

Leia e analise a implementação atual da integração com o AWS EventBridge Scheduler.

**Não escreva ou modifique código ainda.**

Apresente um diagnóstico técnico, identifique os pontos de maior complexidade e proponha uma refatoração incremental, priorizando simplicidade, coesão e separação de responsabilidades.

Utilize a skill `grillme` para esclarecer as decisões necessárias antes de implementar.







---
#passo a passo
# Projeto: Refatoração da integração AWS EventBridge Scheduler

Atue como um engenheiro de software sênior, com experiência em Java, Spring Boot, AWS, Clean Architecture, SOLID e refatoração de sistemas existentes.

## Objetivo

Refatorar a integração existente com AWS EventBridge Scheduler, melhorando:

- Separação de responsabilidades.
- Coesão das classes e métodos.
- Legibilidade.
- Testabilidade.
- Manutenibilidade.
- Simplicidade da implementação.

A integração e o CRUD já existem. Não recrie funcionalidades nem redesenhe o domínio sem necessidade.

## Regras obrigatórias

1. Preserve os padrões de nomenclatura Java e DynamoDB existentes.
2. Preserve os contratos e o comportamento funcional atual.
3. Não faça alterações fora do escopo aprovado.
4. Evite abstrações e design patterns desnecessários.
5. Não concentre múltiplas responsabilidades em um único método.
6. Utilize a skill `grillme` para esclarecer dúvidas e decisões não documentadas.
7. Não assuma requisitos ou comportamentos.
8. Não implemente etapas futuras sem autorização.

### Convenção dos dias da semana

O projeto utiliza números de 1 a 7:

- 1 = Domingo
- 2 = Segunda-feira
- 3 = Terça-feira
- 4 = Quarta-feira
- 5 = Quinta-feira
- 6 = Sexta-feira
- 7 = Sábado

Preserve essa representação. A conversão para cron AWS deve ficar na infraestrutura.

## Execução por etapas

O trabalho deve ser obrigatoriamente dividido em:

1. Diagnóstico.
2. Planejamento.
3. Refatoração incremental.
4. Validação.
5. Consolidação.

Execute somente a etapa solicitada.

Não avance automaticamente para a próxima etapa.

## Persistência de contexto

Utilize a pasta `docs/refactoring/` para registrar o progresso.

Mantenha atualizados:

- `00-context.md`
- `01-diagnosis.md`
- `02-refactoring-plan.md`
- `03-decisions.md`
- `04-progress.md`
- `05-validation.md`

Ao concluir cada etapa:

1. Registre o que foi realizado.
2. Registre as decisões tomadas.
3. Registre as dúvidas pendentes.
4. Registre os arquivos alterados.
5. Identifique a próxima ação.
6. Atualize `04-progress.md`.

Não considere uma decisão aprovada sem confirmação explícita.

## Recuperação de contexto

No início de cada nova sessão:

1. Leia este documento.
2. Leia `04-progress.md`.
3. Leia os documentos da etapa em andamento.
4. Confira o estado atual do Git.
5. Verifique se existem alterações não registradas.
6. Retome do último checkpoint confirmado.

Não repita etapas concluídas sem necessidade.

## Controle de execução

Trabalhe com mudanças pequenas e verificáveis.

Ao finalizar cada etapa, apresente um resumo e aguarde minha autorização para continuar.

Se o contexto disponível estiver insuficiente, interrompa a execução, leia os arquivos de contexto e faça perguntas utilizando GrillMe quando necessário.

---

Execute somente a Etapa 1 — Diagnóstico.

Analise a implementação atual da integração com EventBridge Scheduler. Identifique responsabilidades misturadas, métodos complexos, condicionais excessivas, acoplamentos e oportunidades de simplificação.

Não altere código.

Registre os resultados em `01-diagnosis.md` e atualize `04-progress.md`.

Ao finalizar, apresente o diagnóstico e aguarde minha autorização.

---


Execute somente a Etapa 2 — Planejamento.

Com base no diagnóstico aprovado, proponha uma refatoração incremental, priorizando simplicidade e separação de responsabilidades.

Identifique classes a criar, modificar ou remover, justificando cada alteração.

Utilize GrillMe para esclarecer decisões pendentes.

Não altere código.

Registre o plano em `02-refactoring-plan.md` e atualize o progresso. Aguarde minha aprovação.


---


Execute a próxima unidade de trabalho aprovada no plano de refatoração.

Antes de alterar código, releia o plano e as decisões registradas.

Implemente somente essa unidade, preservando o comportamento existente.

Execute os testes pertinentes, revise o diff e atualize `04-progress.md`.

Não avance para outra unidade sem minha autorização.


---


Execute a etapa de validação da refatoração.

Verifique compilação, testes, regressões, responsabilidades, acoplamentos e aderência ao plano aprovado.

Não faça novas refatorações durante esta etapa sem minha autorização.

Registre os resultados em `05-validation.md` e atualize o progresso.


---


Execute a consolidação final.

Revise os arquivos de contexto, documente a arquitetura final, as decisões tomadas, as alterações realizadas, os testes executados e as pendências.

Atualize `04-progress.md` e `05-validation.md`.

Apresente um relatório final comparando a implementação anterior com a refatorada.
