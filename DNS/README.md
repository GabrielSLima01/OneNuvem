# Parte de DNS

Esta pasta contem uma implementacao simples de DNS para o trabalho.

O objetivo dela e permitir que um servidor de arquivos se registre com um nome e que os clientes consultem esse nome para descobrir o `host` e a `porta`.

## O que foi feito

- `DnsServer.java`: servidor principal do DNS.
- `DnsClient.java`: cliente simples para testar os comandos.
- `RegistroDns.java`: classe que guarda `host` e `porta`.

## Comandos suportados

- `REGISTER nome host porta`
- `LOOKUP nome`
- `REMOVE nome`
- `LIST`

## Ideia geral

Nesse trabalho, o DNS funciona como uma "lista telefonica" do sistema.

Em vez de o cliente saber direto em qual IP e porta o servidor principal esta rodando, ele pergunta primeiro para o DNS.

O DNS guarda pares no formato:

- `nome do servico -> host + porta`

Exemplo:

- `onenuvem -> localhost + 8080`

Entao a ideia e:

1. O servidor principal sobe.
2. O servidor principal se registra no DNS.
3. O cliente consulta o DNS usando o nome do servico.
4. O DNS responde onde esse servico esta.
5. O cliente usa essa resposta para se conectar no servidor correto.

## Como a logica funciona

### 1. `DnsServer.java`

Esse e o servidor principal do DNS.

Ele faz estas etapas:

1. Abre um `ServerSocket` na porta `5050` por padrao.
2. Fica em loop infinito esperando conexoes.
3. Cada vez que um cliente conecta, ele cria uma nova thread.
4. Essa thread le uma linha de texto enviada pelo cliente.
5. O servidor interpreta o comando e devolve uma resposta.

Ele usa um `ConcurrentHashMap` para guardar os registros.

Na pratica, essa estrutura funciona como uma tabela em memoria:

- chave: nome do servico
- valor: objeto `RegistroDns` com `host` e `porta`

Exemplo interno:

```text
"onenuvem" -> ("localhost", 8080)
```

### 2. `RegistroDns.java`

Essa classe e bem simples.

Ela so existe para guardar os dois dados de um registro:

- `host`
- `porta`

Ou seja, quando o DNS salva um servico, ele salva esse objeto como valor.

### 3. `DnsClient.java`

Esse arquivo e um cliente simples de teste.

Ele:

1. Abre um `Socket` ate o DNS.
2. Envia um comando em texto.
3. Le a resposta.
4. Mostra a resposta no console.

Ele foi feito mais para teste e demonstracao.

No projeto maior, os outros integrantes podem:

- reaproveitar a mesma ideia
- ou copiar a logica de abrir socket, enviar comando e ler resposta

## Formato das mensagens

O DNS trabalha com mensagens de texto simples, uma linha por requisicao.

Isso facilita a integracao com o restante do grupo, porque qualquer parte do sistema pode se comunicar com o DNS apenas enviando strings.

### Registrar um servico

Mensagem enviada:

```text
REGISTER onenuvem localhost 8080
```

Resposta esperada:

```text
OK Registro salvo
```

O que acontece:

- o DNS pega o nome `onenuvem`
- salva `localhost` e `8080`
- se ja existir esse nome, ele sobrescreve o valor anterior

### Consultar um servico

Mensagem enviada:

```text
LOOKUP onenuvem
```

Se existir:

```text
FOUND localhost 8080
```

Se nao existir:

```text
NOT_FOUND
```

O que acontece:

- o DNS procura o nome dentro do mapa
- se encontrar, devolve `FOUND host porta`
- se nao encontrar, devolve `NOT_FOUND`

### Remover um servico

Mensagem enviada:

```text
REMOVE onenuvem
```

Respostas possiveis:

```text
OK Registro removido
```

ou

```text
NOT_FOUND
```

### Listar registros

Mensagem enviada:

```text
LIST
```

Resposta vazia:

```text
EMPTY
```

Resposta com dados:

```text
LIST | onenuvem -> localhost:8080
```

## Fluxo completo no projeto

O resto do grupo pode pensar na integracao assim:

1. O servidor de arquivos inicia.
2. Ele abre sua propria porta, por exemplo `8080`.
3. Depois disso, ele conecta no DNS.
4. Envia `REGISTER nomeDoServico host porta`.
5. O cliente, antes de falar com o servidor, conecta no DNS.
6. Envia `LOOKUP nomeDoServico`.
7. Recebe `FOUND host porta`.
8. Usa esse `host` e essa `porta` para abrir conexao com o servidor real.

## Como os outros integrantes podem integrar

### Lado do servidor principal

Quando o servidor do grupo iniciar, ele precisa fazer algo equivalente a isso:

```text
REGISTER onenuvem localhost 8080
```

Se o servidor deles rodar em outra maquina ou outra porta, basta trocar os valores.

Exemplo:

```text
REGISTER onenuvem 192.168.0.15 8080
```

### Lado do cliente principal

Antes de conectar no servidor de arquivos, o cliente deve consultar o DNS:

```text
LOOKUP onenuvem
```

Se a resposta for:

```text
FOUND localhost 8080
```

entao o cliente deve abrir socket para:

- host: `localhost`
- porta: `8080`

### Contrato simples de integracao

Para o restante do grupo, o contrato da parte de DNS e basicamente este:

- entrada para registrar: `REGISTER nome host porta`
- entrada para consultar: `LOOKUP nome`
- saida de sucesso na consulta: `FOUND host porta`
- saida sem resultado: `NOT_FOUND`

Se eles seguirem esse formato, a integracao deve funcionar.

## Observacoes importantes

- Os registros ficam apenas em memoria.
- Se o servidor DNS for desligado, ele perde os registros salvos.
- Essa escolha foi intencional para manter o trabalho simples.
- O DNS atende varios clientes criando uma thread por conexao.
- O DNS nao usa banco de dados nem bibliotecas externas.
- Toda a comunicacao foi feita com `Socket`, `ServerSocket`, `BufferedReader` e `PrintWriter`.

## Resumo rapido para apresentar

Se voce quiser explicar rapido para os integrantes, pode falar assim:

"Nossa parte de DNS funciona como um servidor de nomes simples. O servidor principal se registra com um nome, host e porta. Depois o cliente consulta esse nome no DNS e recebe o endereco correto para abrir a conexao. Tudo foi feito com sockets em Java puro e os dados ficam guardados em memoria."

## Como compilar

```bash
javac DNS\*.java
```

## Como executar o servidor DNS

```bash
java -cp DNS DnsServer
```

Ou informando outra porta:

```bash
java -cp DNS DnsServer 5051
```

## Como testar com o cliente

Registrar um servidor:

```bash
java -cp DNS DnsClient REGISTER onenuvem localhost 8080
```

Consultar um servidor:

```bash
java -cp DNS DnsClient LOOKUP onenuvem
```

Listar os registros:

```bash
java -cp DNS DnsClient LIST
```

Remover um registro:

```bash
java -cp DNS DnsClient REMOVE onenuvem
```

## Exemplo de uso no trabalho

1. O servidor principal inicia.
2. Ele envia `REGISTER onenuvem localhost 8080` para o DNS.
3. O cliente envia `LOOKUP onenuvem`.
4. O DNS responde com o endereco do servidor.
5. O cliente usa esse endereco para se conectar no servidor.

Essa implementacao foi feita de forma basica, usando apenas bibliotecas padrao do Java.
# Registrar um serviço
java -cp DNS DnsClient REGISTER web 192.168.1.1 8080

# Consultar um serviço
java -cp DNS DnsClient LOOKUP web

# Listar todos
java -cp DNS DnsClient LIST

# Remover um serviço
java -cp DNS DnsClient REMOVE web