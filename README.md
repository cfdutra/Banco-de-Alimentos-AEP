# Sistema de Banco de Alimentos

AEP do 4º semestre (2026.2) - UniCesumar - Engenharia de Software / Análise e Desenvolvimento de Sistemas.
Autores: Carlos Felipe Duta e Maria Vitória Arana Almeida.

**ODS 2 - Fome Zero e Agricultura Sustentável.** O sistema organiza as doações de alimentos, controla as
validades e indica o que deve ser distribuído primeiro, reduzindo o desperdício.

## Tecnologias

- Java 17 ou superior (orientação a objetos: classe abstrata, herança, polimorfismo com `@Override`, composição 1:N)
- MySQL 8 (XAMPP ou WAMP) com JDBC
- Maven (baixa o driver do MySQL automaticamente)
- Arquitetura em camadas simplificada: Model - DAO - Main

## Requisitos

1. O sistema deve permitir o cadastro de doadores (pessoa física ou jurídica), com nome, telefone e e-mail.
2. O sistema deve permitir o registro de uma doação de alimentos, associada a um doador, contendo tipo de alimento, quantidade em quilogramas e data de validade.
3. O sistema deve permitir consultar o histórico de doações de um doador específico.
4. O sistema deve permitir atualizar os dados cadastrais de um doador.
5. O sistema deve permitir remover o cadastro de um doador.
6. O sistema deve alertar (ou indicar) quais alimentos estão próximos da validade, para priorizar a distribuição.
7. O sistema deve manter o relacionamento entre doadores e suas respectivas doações, evitando a perda do histórico de arrecadação.
8. O sistema deve realizar validações nos dados inseridos, como impedir o cadastro de quantidades iguais ou menores que zero e garantir que uma doação esteja vinculada a um doador cadastrado.
9. O sistema deve permitir ordenar os alimentos pela data de validade, facilitando a identificação daqueles que precisam ser distribuídos primeiro.
10. O sistema deve permitir visualizar a quantidade total de alimentos registrada no sistema.
11. O sistema deve permitir identificar a situação de um alimento, como disponível, distribuído ou vencido.
12. O sistema deve permitir adicionar ou alterar informações de uma doação já registrada, quando necessário.
13. O sistema deve impedir o cadastro de uma doação caso algum dos dados obrigatórios não seja informado.

## Cronograma

| Data | Atividade | Responsável |
|------|-----------|-------------|
| 05/09 | Definição do tema (banco de alimentos), descoberta, diagrama de classe | Carlos Felipe Duta |
| 05/09 | Partes interessadas, regra de negócio, requisitos e diagrama DER, concepção e alinhamento com o ODS | Maria Vitória Arana Almeida |
| 06/09 | Estrutura do repositório GitHub e README | Carlos Felipe Duta |
| 07/09 | Justificativa técnica e revisão dos diagramas | Maria Vitória Arana Almeida |
| 08/09 | Montagem do PDF da 1ª entrega | Carlos Felipe Duta |
| 09/09 | Revisão geral do PDF e do repositório | Carlos Felipe Duta |
| 10/09 | Ajustes finais / folga de segurança | Maria Vitória Arana Almeida |
| 11/09 | Entrega da 1ª etapa | Carlos Felipe Duta |
| A definir | Implementação do CRUD em Java + conexão MySQL (2ª entrega) | Todos |
| A definir | Testes finais e entrega do código-fonte (2ª entrega) | Todos |

## Estrutura do repositório

```
Banco-de-Alimentos-AEP/
|-- database/script-criacao.sql      # criação do banco e das tabelas
|-- src/main/java/bancoalimentos/
|   |-- model/   Pessoa (abstrata), Doador, DoacaoAlimento
|   |-- dao/     ConexaoDB, DoadorDAO, DoacaoAlimentoDAO
|   `-- Main.java                    # menu de console
|-- pom.xml
`-- README.md
```

## Como executar

**Pré-requisitos:** JDK 17+, Maven e MySQL (XAMPP/WAMP) rodando na porta 3306.

1. Inicie o MySQL (painel do XAMPP/WAMP).
2. Crie o banco e as tabelas executando o script (pelo phpMyAdmin, MySQL Workbench ou terminal):
   ```
   mysql -u root -p < database/script-criacao.sql
   ```
3. Na raiz do projeto, compile e execute:
   ```
   mvn compile exec:java
   ```
4. Por padrão a conexão usa `localhost:3306`, usuário `root` e senha vazia (padrão do XAMPP).
   Para mudar, defina as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD` antes de executar.

Funciona em Windows, Linux e macOS, pois só depende de Java, Maven e MySQL.

## Regras de negócio implementadas

- Toda doação está vinculada a um doador cadastrado (chave estrangeira).
- Quantidade (kg) sempre maior que zero; validade e demais campos são obrigatórios.
- Alimentos a até 7 dias do vencimento são sinalizados com prioridade.
- Alimentos com validade passada são identificados como vencidos; só alimentos disponíveis podem ser distribuídos.
- Um doador com doações vinculadas não pode ser removido.
