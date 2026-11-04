# 0002. JWT stateless com perfis e ownership

**Status:** Aceita

## Contexto
A SPA consome uma API REST separada. Há três perfis (USER, USERRH e ADMIN), e um RH só pode alterar as vagas sob sua responsabilidade.

## Decisão
Autenticação stateless com JWT Bearer, validado em toda requisição por um filtro do Spring Security. A autorização acontece em duas camadas: perfil por rota na SecurityFilterChain e ownership verificado no Service. O frontend usa as claims só para navegação.

## Alternativas consideradas
- **Sessão no servidor:** exigiria sessão compartilhada para escalar horizontalmente os containers.

## Consequências
- Positivas: API sem estado, pronta para várias instâncias; regras de acesso centralizadas no backend.
- Negativas: revogar um token antes da expiração exige mecanismo extra, por isso a expiração deve ser curta.
