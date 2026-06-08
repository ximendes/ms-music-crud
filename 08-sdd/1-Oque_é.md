# 1. Problema atual do uso de IA no desenvolvimento

Com o avanço dos agentes de IA, gerar código se tornou cada vez mais fácil.


Hoje, o desafio não é mais gerar código, e sim gerar o código correto, seguindo contexto, regras de negócio, padrões arquiteturais e expectativas do sistema.

Código sem contexto não resolve problema.

Quem nunca escreveu um prompt esperando resultado X e recebeu Y?

```
Crie um endpoint de pagamento em Java.
```
Resultado provável
- sem validação
- sem idempotência
- sem retry
- sem observabilidade
- sem regras de negócio

Isso acontece porque LLMs não entendem o sistema.
Eles apenas respondem ao contexto fornecido.

E é justamente aí que entra o SDD (Spec-Driven Development).

---

# O que é SDD?

De forma simplificada, SDD é uma abordagem onde o desenvolvimento é guiado por especificações claras e estruturadas.

A especificação deixa de ser apenas documentação e passa a ser a principal fonte de verdade do sistema.

Essa ideia já existia antes da IA.

Times de negócio e desenvolvimento sempre precisaram alinhar:
- regras de negócio
- fluxos
- contratos
- requisitos funcionais e não funcionais

A diferença agora é que agentes de IA também consomem esse contexto.

E IA consome muito melhor:
- contexto estruturado
- regras explícitas
- exemplos claros
- contratos bem definidos

do que requisitos espalhados em:
- tickets
- mensagens
- documentação desatualizada
- conhecimento implícito

---

# Principais ideias do SDD

- Spec-Driven Development = desenvolvimento guiado por especificações
- A spec vira a principal fonte de verdade
- Contexto passa a ser tão importante quanto código
- IA utiliza specs para gerar, validar e revisar implementações
- Código deixa de ser o centro; contexto e especificação ganham protagonismo

---

# Mensagem principal

“LLMs funcionam melhor quando o contexto é estruturado.”

---

# Comparação prática

## Antes
- Dev interpreta requisito
- Contexto fica distribuído
- IA ajuda parcialmente
- Resultado depende muito do prompt

## Com SDD
- A spec centraliza o contexto
- Humanos e agentes seguem a mesma referência
- A IA consegue gerar resultados mais consistentes
- O sistema fica mais previsível e escalável


# 2. Como agentes/LLMs utilizam contexto

