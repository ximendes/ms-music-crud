#!/bin/bash

set -e

# Alias para executar script fora do container localstack
#AWS="aws --profile localstack --no-cli-pager"

# Alias para executar script dentro container localstack
AWS="awslocal"
TABLE_NAME="Music"

echo "🔍 Verificando se a tabela '$TABLE_NAME' existe..."

if $AWS dynamodb describe-table --table-name "$TABLE_NAME" >/dev/null 2>&1; then
  echo "✅ Tabela '$TABLE_NAME' já existe. Nada a fazer."
else
  echo "🚀 Tabela não encontrada. Criando..."

  $AWS dynamodb create-table \
    --table-name "$TABLE_NAME" \
    --attribute-definitions \
      AttributeName=Artist,AttributeType=S \
      AttributeName=SongTitle,AttributeType=S \
    --key-schema \
      AttributeName=Artist,KeyType=HASH \
      AttributeName=SongTitle,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --table-class STANDARD

  # Non-key attributes, such as Year, are stored with each item and are not
  # declared in create-table unless they are used by a key or index.

  echo "⏳ Aguardando a tabela ficar ativa..."

  $AWS dynamodb wait table-exists --table-name "$TABLE_NAME"

  echo "✅ Tabela '$TABLE_NAME' criada com sucesso!"
fi

#Delete Table
#aws --profile localstack dynamodb delete-table --table-name Music
