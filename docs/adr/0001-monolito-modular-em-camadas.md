# 0001. Monolito modular em camadas

**Status:** Aceita

## Contexto
O AppRH tem um domínio pequeno (usuários, empresas e vagas), um único desenvolvedor e precisa evoluir rápido sem custo operacional alto.

## Decisão
O backend é um monolito modular organizado em camadas: Security → Controller → Service → Repository. O Spring Web MVC atua só na borda HTTP; regras de negócio, transações e ownership ficam nos Services. O Controller nunca acessa o Repository diretamente e nunca expõe entidades, só DTOs.

## Alternativas consideradas
- **Microsserviços:** rede entre módulos, deploys múltiplos e consistência distribuída sem ganho para o tamanho atual.
- **Controller → Repository direto:** espalharia regra de negócio pelos controllers e acoplaria HTTP à persistência.

## Consequências
- Positivas: um deploy, transações locais, Services testáveis com JUnit e Mockito sem subir servidor.
- Negativas: escala como uma unidade só. A separação por pacotes mantém aberta a extração de um módulo no futuro.
