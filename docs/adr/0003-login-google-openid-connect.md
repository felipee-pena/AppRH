# 0003. Login com Google via OpenID Connect

**Status:** Aceita

## Contexto
Usuários querem entrar sem criar senha, e o login federado é padrão de mercado. O sistema já tem JWT próprio, perfis e ownership que não devem mudar.

## Decisão
Adicionar login com Google usando OAuth 2.0 + OpenID Connect. O backend valida o ID token (assinatura, emissor, audiência, expiração e e-mail verificado) e emite o mesmo JWT da aplicação. O OAuth é uma nova porta de entrada; a autorização continua igual.

## Alternativas consideradas
- **Usar o token do Google em todas as chamadas da API:** acoplaria a API ao provedor e às regras dele.

## Consequências
- Positivas: menos senhas para gerenciar, perfis e matriz de acesso preservados.
- Regras obrigatórias: vínculo de conta só com e-mail verificado pelo provedor; perfil nunca vem da requisição (novo cadastro nasce USER); `client_secret` só no backend.
