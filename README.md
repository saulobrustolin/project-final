
# 0. Projeto
A ideia principal do projeto foi desenvolver uma aplicação simples para controle e metas financeiras. A dor surgiu do mercado de aplicativos deste nicho, onde a maioria das funcionalidades são pagas ou repletas de anúncios de recompensa (ainda acredito ser a melhor opção, em comparação a um plano mensal/anual). Com isso, desenvolvi esse app que faz o básico, mas bem feito.
    * Tecnologias utilizadas: **Spring Boot + Java 21 + MongoDB + React + TypeScript + Playwright + Docker + Kafka**.

# 1. Instalação
* Para executar o projeto é necessário ter instalado o **Node.js, OpenJDK c/Java 21 e Docker**. Além disso, será necessário ter uma conta no Resend (uma plataforma de envio de e-mails) para criar a sua KEY - fique tranquilo que é 100% free para até 3k de envios por mês e iremos utilizar bem menos do que isso.

Lista com o passo a passo para executar o projeto:
1. Fazer clone de todo o repositório e submodulos contidos. Faça isso executando o código abaixo:
```bash
git clone --recurse-submodules https://github.com/saulobrustolin/project-final
```
2. Após isso é necessário configurar os arquivos de `env` do projeto (tanto da pasta raiz, quando a pasta do frontend).
    * Para isso é necessário **COPIAR** os arquivos `.env.example` e posteriormente renomear para `.env`;
    * Todos os arquivos `.env.example` contam com a estrutura de variáveis necessárias para o projeto, porém sem os valores definidos.
    * Agora será necessário definir o valor de cada variável. Não foi pré-definido por questões de segurança e dinamicidade, visto que você pode utilizar servidores de terceiro ou localmente.
3. Definidos os valores das variáveis de ambiente podemos rodar o projeto, mas antes disso vale ressaltar que no arquivo `init-db.js` foi definido um usuário 100% fictício como seed ao projeto, fique a vontade para alterar da forma que desejar.
    * E-mail: `joaogomes@gmail.com`
    * Senha: `senha123$`
    * Todos os dados utilizados neste projeto são valores fictícios, nenhum dados é verdadeiro, seja CPF ou e-mail.
4. Para rodar o projeto basta executar o seguinte comando na raiz do projeto:
```bash
docker compose --profile production up -d --build
```
5. Após executado, a interface estará disposta na rota padrão `http://localhost` (caso não tenha alterado no `docker-compose.yaml`);

# 2. Execução dos testes
* Temos 2 caminhos de execução, sendo:
    1. **Testes unitários e integração** e
    2. **Testes de sistema**;

## 2.1 Execução dos testes de sistema:
Lista com o passo a passo para execução dos testes de sistema:
1. Na raiz do projeto iremos executar (no terminal):
```bash
cd frontend
```
2. Posteriormente a isso precisamos instalar a biblioteca Playwright (framework utilizado nos testes), para isso basta executar o código abaixo (também no terminal):
```bash
npx playwright install
```
3. Terminado a instalação do framework basta rodar os testes através do seguinte comando no terminal:
```bash
npx playwright test --ui
```
* Se você é adepto a uma interface, a flag `--ui` irá resolver isso para você. Caso contrário, basta remover a flag e os testes irão rodar puramente no terminal.

## 2.2 Execução dos testes unitários e integração
Lista com o passo a passo para execução dos testes de sistema:
1. Na raiz do projeto iremos executar (no terminal):
```bash
mvn clean test
```
* Basta isto para realizar os testes de backend.