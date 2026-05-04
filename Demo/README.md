# Demo para apresentacao

Esta demo mostra a base de infraestrutura da OneNuvem sem regras de negocio.

Ela demonstra:

- cliente falando com um gateway;
- gateway/middleware escondendo a localizacao dos servidores;
- tres nos de armazenamento atendendo por socket TCP;
- upload replicado nos nos saudaveis;
- download funcionando mesmo se um no estiver desligado;
- atendimento concorrente com threads.

## 1. Compilar

Na raiz do projeto:

```powershell
javac DNS\*.java Middleware\*.java Requests\GatewayClient.java Demo\*.java
```

## 2. Subir os nos de armazenamento

Abra tres terminais na raiz do projeto.

Terminal 1:

```powershell
java -cp Demo DemoStorageNode server-1 5001
```

Terminal 2:

```powershell
java -cp Demo DemoStorageNode server-2 5002
```

Terminal 3:

```powershell
java -cp Demo DemoStorageNode server-3 5003
```

## 3. Subir o middleware

Em outro terminal:

```powershell
java -cp Middleware MiddlewareServer
```

O middleware le o arquivo `.env`, que aponta para os tres nos nas portas `5001`, `5002` e `5003`.

## 4. Rodar o cliente

Em outro terminal:

```powershell
java -cp ".;Demo" DemoClient localhost 8000
```

Saida esperada:

```text
Gateway: localhost:8000
Heartbeat OK
Upload OK
Arquivos visiveis pelo gateway: [apresentacao.txt]
Download: Conteudo de demonstracao da OneNuvem
```

## 5. Mostrar tolerancia a falha

Pare um dos tres nos com `Ctrl+C` e rode o cliente de novo:

```powershell
java -cp ".;Demo" DemoClient localhost 8000
```

O fluxo ainda deve funcionar, porque o middleware marca o no que falhou e usa os outros nos saudaveis.

## Rodando com Docker

Na raiz do projeto:

```powershell
docker compose build
docker compose up -d storage-node-1 storage-node-2 storage-node-3 middleware
```

Rodar o cenario completo:

```powershell
docker compose run --rm --no-deps demo-client
```

Para mostrar replicacao e tolerancia a falhas com o mesmo arquivo:

```powershell
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 upload apresentacao.txt "Arquivo replicado pela OneNuvem"
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 list
docker compose stop storage-node-1
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 download apresentacao.txt
```

O download deve continuar funcionando porque o upload foi replicado nos outros nos.
O `--no-deps` e importante para o Compose nao religar automaticamente o no parado durante o teste.

Para religar o no:

```powershell
docker compose start storage-node-1
```

Se todos os nos forem parados e depois religados, o middleware tenta falar com eles de novo nas proximas requisicoes:

```powershell
docker compose stop storage-node-1 storage-node-2 storage-node-3
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 upload falha.txt "nao deve salvar sem nos"
docker compose start storage-node-1 storage-node-2 storage-node-3
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 upload recuperado.txt "nos recuperados"
docker compose run --rm --no-deps demo-client DemoClient middleware 8000 download recuperado.txt
```

Os arquivos dos nos ficam em volumes Docker separados, entao reiniciar um container nao apaga o armazenamento daquele no.

Para encerrar a demo:

```powershell
docker compose down
```

Para apagar tambem os dados dos nos:

```powershell
docker compose down -v
```
