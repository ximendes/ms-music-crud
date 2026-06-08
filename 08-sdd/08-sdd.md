Para uma apresentação curta e focada em desenvolvedores, eu concentraria em 5 tópicos que realmente explicam o valor do SDD no contexto de IA sem dispersar.

# 1. O que é SDD e por que ele surgiu

Esse é o fundamento da apresentação.

## O que abordar

* Spec-Driven Development = desenvolvimento guiado por especificações
* A spec vira a “fonte de verdade”
* IA consome specs melhor do que requisitos espalhados
* Código deixa de ser o centro → contexto/especificação vira o centro

## Mensagem principal

“LLMs funcionam melhor quando o contexto é estruturado.”

## Comparação importante

* Antes:

  * Dev interpreta requisito
  * IA gera código parcialmente
* Com SDD:

  * A spec guia humanos e agentes

---

# 2. Como agentes/LLMs utilizam contexto

Esse provavelmente é o tópico mais importante tecnicamente.

## O que estudar

* Context window: https://platform.claude.com/docs/pt-BR/about-claude/glossary#context-window
* System prompts
* instructions.md
* agent.md
* examples
* memória/contexto compartilhado

## Explicação importante

A IA não “entende o sistema”.
Ela apenas responde ao contexto recebido.

Então:

* Specs ruins → código ruim
* Contexto inconsistente → comportamento inconsistente

## Frase forte

“A qualidade da IA é limitada pela qualidade do contexto.”

---

# 3. Prompt Engineering vs Spec Engineering

Esse tópico costuma gerar bastante interesse.

## O que explicar

### Prompt Engineering

* instruções pontuais
* improviso
* dependente do usuário

### Spec Engineering

* contexto persistente
* padronização
* reutilização
* escalabilidade

## Exemplo bom

Mostrar:

* um prompt solto
* depois uma spec estruturada

E comparar a qualidade da saída.

## Mensagem principal

“O mercado está migrando de prompts para sistemas orientados a contexto.”

---

# 4. Estrutura prática de um projeto SDD

Esse é o tópico “mão na massa”.

## O que mostrar

Estrutura simples:

```txt
/project
 ├── README.md
 ├── architecture.md
 ├── instructions.md
 ├── agent.md
 ├── api-spec.yaml
 ├── prompts/
 └── examples/
```

## Explicar o papel de cada arquivo

Especialmente:

* README → visão geral
* instructions → regras globais
* agent → comportamento do agente
* examples → few-shot learning
* OpenAPI/JSON Schema → contratos

## Isso conecta muito bem com devs

Porque sai da teoria e entra em implementação real.

---

# 5. Benefícios e limitações reais

Fechar com maturidade técnica é importante.

## Benefícios

* Menos ambiguidade
* Código mais consistente
* Melhor onboarding
* Melhor automação
* Melhor uso de IA

## Limitações

* Specs desatualizadas
* Contexto excessivo
* Hallucination
* Governança
* “Garbage in, garbage out”

## Fechamento forte

“SDD não substitui engenharia de software.
Ele aumenta a importância dela.”

---

# Ordem ideal dos slides

1. Problema atual do uso de IA no desenvolvimento
2. O que é SDD
3. Como LLMs usam contexto
4. Prompt vs Spec Engineering
5. Estrutura real de projeto SDD
6. Benefícios
7. Limitações
8. Conclusão/futuro

---

# O que eu MAIS estudaria no seu caso

Se tiver pouquíssimo tempo:

1. Context engineering
2. Como LLMs consomem contexto
3. Estrutura de specs
4. Prompt vs Spec Engineering
5. Exemplos reais de projeto

Esses tópicos vão deixar sua apresentação muito mais técnica e relevante para desenvolvedores.
