# 0005. Imagem Docker gerada na pipeline

**Status:** Aceita

## Contexto
O próximo passo do projeto é o deploy na AWS, e o ambiente de execução precisa ser o mesmo em qualquer lugar.

## Decisão
Toda pipeline do GitHub Actions gera a imagem Docker da aplicação depois que build, testes e verificações de qualidade passam. Só uma imagem que passou na CI segue para publicação e deploy.

## Alternativas consideradas
- **Deploy direto do JAR e do `dist`:** dependeria da configuração de cada servidor.

## Consequências
- Positivas: runtime padronizado, base pronta para o registry e o CD na AWS.
- Próximos passos: publicar as imagens com tags por commit e SemVer, e escanear as imagens antes do push.
