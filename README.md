# FreteGo - Sistema de Gestão de Fretes

## Visão geral

O `FreteGo` é um sistema Java EE com JSP, Servlet, BO e DAO para operação de fretes, ocorrências de entrega, mensageria por outbox e dashboard em tempo real.

O projeto está organizado em três responsabilidades principais:

- legado operacional
- mensageria de integração
- dashboard em tempo real

## Arquitetura técnica

O sistema segue o padrão:

- `JSP + JavaScript` para interface
- `Servlet Controller` para entrada HTTP
- `BO` para regra de negócio
- `DAO` para persistência JDBC
- `PostgreSQL` como banco principal

O fluxo de integração funciona assim:

1. O usuário executa uma operação no legado.
2. O BO valida a regra.
3. O DAO grava a alteração de negócio.
4. Na mesma transação, o DAO grava um evento na tabela `evento_sistema`.
5. O módulo de mensageria lê os eventos pendentes do outbox.
6. O legado publica o payload bruto na API de mensageria.
7. A API de mensageria redistribui o evento para o dashboard via WebSocket/STOMP.
8. O dashboard recebe o evento, aplica um patch visual e depois reidrata os dados consultando novamente o legado.

## Estrutura de módulos

### 1. Cadastros

Módulos:

- cliente
- motorista
- veículo

Responsabilidades:

- CRUD
- validações de formulário
- validações de negócio no BO
- consulta e exclusão com tratamento de erro

### 2. Frete

Responsabilidades:

- cadastro de frete
- mudança de status
- detalhamento do frete
- timeline de ocorrências
- integração com veículo, motorista e clientes

### 3. Ocorrências de entrega

Responsabilidades:

- registrar histórico cronológico do frete
- armazenar município, UF, data e hora
- exigir descrição para `AVARIA`, `EXTRAVIO` e `OUTROS`
- exigir recebedor e documento para `ENTREGA_REALIZADA`

### 4. Mensageria

Responsabilidades:

- gravar eventos no outbox `evento_sistema`
- enviar eventos pendentes para a API de mensageria
- marcar sucesso e erro de publicação

### 5. Dashboard em tempo real

Responsabilidades:

- consultar o legado
- receber eventos via STOMP/WebSocket
- refletir transições operacionais em tempo real
- abrir o detalhe do frete
- concluir entrega com captura de nome e documento do recebedor

## Regras reais de negócio

### Frete

Status suportados:

- `EMITIDO`
- `SAIDA_CONFIRMADA`
- `EM_TRANSITO`
- `ENTREGUE`
- `NAO_ENTREGUE`
- `CANCELADO`

Transições válidas:

- `EMITIDO -> SAIDA_CONFIRMADA`
- `SAIDA_CONFIRMADA -> EM_TRANSITO`
- `EM_TRANSITO -> ENTREGUE`
- `EM_TRANSITO -> NAO_ENTREGUE`
- `EMITIDO -> CANCELADO`

Regras:

- o frete só pode ser criado com cliente remetente ativo
- o frete só pode ser criado com cliente destinatário ativo
- o frete só pode ser criado com motorista ativo
- o frete só pode ser criado com CNH válida na data de emissão
- o frete só pode ser criado com veículo `DISPONIVEL`
- o peso da carga não pode exceder a capacidade do veículo

### Veículo

Regras:

- o `RNTRC` deve ter exatamente `8 dígitos numéricos`
- o veículo só pode ser usado em frete quando o status for `DISPONIVEL`
- o veículo não pode ser marcado manualmente como disponível se houver frete com `SAIDA_CONFIRMADA` ou `EM_TRANSITO`

### Cliente

Regras:

- a camada BO valida CNPJ, tipo, status e campos obrigatórios
- a exclusão de cliente é barrada no BO quando houver frete em aberto
- o banco ainda mantém FK obrigatória de `frete` para `cliente`, então a exclusão física continua limitada pelo histórico preservado

### Ocorrência

Tipos suportados:

- `SAIDA_PATIO`
- `EM_ROTA`
- `TENTATIVA_ENTREGA`
- `ENTREGA_REALIZADA`
- `AVARIA`
- `EXTRAVIO`
- `OUTROS`

Regras:

- a ocorrência deve respeitar ordem cronológica
- a ocorrência não pode ser anterior à emissão do frete
- `descricao` é obrigatória para `AVARIA`, `EXTRAVIO` e `OUTROS`
- `nomeRecebedor` e `documentoRecebedor` são obrigatórios para `ENTREGA_REALIZADA`
- fretes finalizados não aceitam novas ocorrências

## Banco de dados

### Tabela `frete`

Campos principais:

- `id`
- `numero`
- `id_remetente`
- `id_destinatario`
- `id_motorista`
- `id_veiculo`
- `municipio_origem`
- `uf_origem`
- `municipio_destino`
- `uf_destino`
- `descricao_carga`
- `peso_kg`
- `volumes`
- `valor_frete`
- `aliquota_icms`
- `valor_icms`
- `valor_total`
- `status`
- `data_emissao`
- `data_previsao_entrega`
- `data_saida`
- `data_entrega`

### Tabela `ocorrencia_frete`

Campos:

- `id`
- `id_frete`
- `tipo`
- `data_hora`
- `municipio`
- `uf`
- `descricao`
- `nome_recebedor`
- `documento_recebedor`

### Tabela `evento_sistema`

Campos:

- `id`
- `tipo`
- `entidade`
- `entidade_id`
- `payload`
- `status`
- `tentativas`
- `data_criacao`
- `data_publicacao`
- `mensagem_erro`

## Contratos HTTP do legado

## 1. `FreteController`

URL base:

- `/FreteController`

### 1.1 Buscar fretes

Request:

```http
GET /FreteController?acao=buscar&filtro=
```

Response `200`:

```json
[
  {
    "id": 12,
    "numero": "FRT-2026-00012",
    "status": "EM_TRANSITO",
    "remetente": "Cliente A",
    "destinatario": "Cliente B",
    "motorista": "João Silva",
    "veiculo": "ABC1D23",
    "origem": "Fortaleza/CE",
    "destino": "Recife/PE",
    "descricaoCarga": "Eletrodomésticos",
    "pesoKg": 1200.0,
    "volumes": 18,
    "valorFrete": 8000.0,
    "aliquotaIcms": 12.0,
    "valorIcms": 960.0,
    "valorTotal": 8960.0,
    "dataEmissao": "2026-05-10T09:00",
    "dataPrevisaoEntrega": "2026-05-12",
    "dataSaida": "2026-05-10T11:00",
    "dataEntrega": ""
  }
]
```

Response `500`:

```json
{
  "mensagem": "Erro ao buscar fretes."
}
```

### 1.2 Obter um frete

Request:

```http
GET /FreteController?acao=obter&id=12
```

Response `200`:

```json
{
  "id": 12,
  "numero": "FRT-2026-00012",
  "status": "EM_TRANSITO",
  "remetente": "Cliente A",
  "destinatario": "Cliente B",
  "motorista": "João Silva",
  "veiculo": "ABC1D23",
  "origem": "Fortaleza/CE",
  "destino": "Recife/PE",
  "descricaoCarga": "Eletrodomésticos",
  "pesoKg": 1200.0,
  "volumes": 18,
  "valorFrete": 8000.0,
  "aliquotaIcms": 12.0,
  "valorIcms": 960.0,
  "valorTotal": 8960.0,
  "dataEmissao": "2026-05-10T09:00",
  "dataPrevisaoEntrega": "2026-05-12",
  "dataSaida": "2026-05-10T11:00",
  "dataEntrega": ""
}
```

Response `404`:

```json
{
  "mensagem": "Frete não encontrado."
}
```

### 1.3 Cadastrar frete

Request `application/x-www-form-urlencoded`:

```text
acao=cadastrar
remetenteId=1
destinatarioId=2
motoristaId=3
veiculoId=4
municipioOrigem=Fortaleza
ufOrigem=CE
municipioDestino=Recife
ufDestino=PE
descricaoCarga=Eletrodomésticos
pesoKg=1200
volumes=18
valorFrete=8000
aliquotaIcms=12
dataPrevisaoEntrega=2026-05-12
```

Resposta:

- sucesso usa redirect para `acao=detalhar&id={id}`
- erro volta para a JSP com `request.setAttribute("erro", mensagem)`

### 1.4 Atualizar status do frete

Request `application/x-www-form-urlencoded`:

```text
acao=confirmarSaida
id=12
```

ou

```text
acao=iniciarTransito
id=12
```

ou

```text
acao=naoEntregar
id=12
```

ou

```text
acao=cancelar
id=12
```

Response `200`:

```json
{
  "sucesso": true,
  "mensagem": "Status do frete atualizado com sucesso."
}
```

Response `400`:

```json
{
  "sucesso": false,
  "mensagem": "Transição de status inválida. Status atual: EMITIDO."
}
```

Response `500`:

```json
{
  "sucesso": false,
  "mensagem": "Erro inesperado ao atualizar status do frete."
}
```

### 1.5 Concluir entrega pelo dashboard

Request `application/x-www-form-urlencoded`:

```text
acao=entregar
id=12
tipoOcorrencia=ENTREGA_REALIZADA
nomeRecebedor=Maria Souza
documentoRecebedor=12345678900
```

Response `200`:

```json
{
  "sucesso": true,
  "mensagem": "Status do frete atualizado com sucesso."
}
```

### 1.6 Registrar ocorrência manual

Request `application/x-www-form-urlencoded`:

```text
acao=registrarOcorrencia
freteId=12
tipoOcorrencia=AVARIA
dataHoraOcorrencia=2026-05-10T14:35
municipioOcorrencia=Quixadá
ufOcorrencia=CE
descricaoOcorrencia=Embalagem lateral danificada
nomeRecebedor=
documentoRecebedor=
```

Comportamento:

- em ocorrência manual comum, o legado chama `freteBO.registrarOcorrencia(...)`
- em `ENTREGA_REALIZADA`, o legado redireciona para `freteBO.entregar(...)`

## 2. `MensageriaController`

URL base:

- `/MensageriaController`

### 2.1 Disparar envio dos eventos pendentes

Request `application/x-www-form-urlencoded`:

```text
acao=enviarPendentes
```

Response `200`:

```json
{
  "sucesso": true,
  "total": 5,
  "enviados": 5,
  "erros": 0,
  "mensagem": "Envio de mensageria concluído."
}
```

Response `400`:

```json
{
  "sucesso": false,
  "mensagem": "Mensageria desabilitada na configuração do sistema."
}
```

## Contrato HTTP entre legado e API de mensageria

O legado publica para:

- `POST http://localhost:8082/api/mensageria/eventos`

Configuração:

- `mensageria.api.baseUrl=http://localhost:8082`
- `mensageria.api.eventosPath=/api/mensageria/eventos`

Cabeçalhos enviados pelo legado:

```http
Content-Type: application/json; charset=UTF-8
Accept: application/json
```

### Payload real de evento de frete

Exemplo de `FRETE_CRIADO`:

```json
{
  "versao": "1.0",
  "evento": "FRETE_CRIADO",
  "origem": "SISTEMA_FRETES_WEB",
  "endpointMensageria": "http://localhost:8082/api/mensageria/eventos",
  "mensageriaHabilitada": true,
  "dataEvento": "2026-05-10T15:10:00",
  "frete": {
    "id": 12,
    "numero": "FRT-2026-00012",
    "status": "EMITIDO",
    "idRemetente": 1,
    "idDestinatario": 2,
    "idMotorista": 3,
    "idVeiculo": 4,
    "origem": "Fortaleza/CE",
    "destino": "Recife/PE",
    "pesoKg": 1200.0,
    "valorTotal": 8960.0
  }
}
```

### Payload real de `OCORRENCIA_FRETE_REGISTRADA`

```json
{
  "versao": "1.0",
  "evento": "OCORRENCIA_FRETE_REGISTRADA",
  "origem": "SISTEMA_FRETES_WEB",
  "endpointMensageria": "http://localhost:8082/api/mensageria/eventos",
  "mensageriaHabilitada": true,
  "dataEvento": "2026-05-10T15:15:00",
  "frete": {
    "id": 12,
    "numero": "FRT-2026-00012",
    "status": "EM_TRANSITO"
  },
  "ocorrencia": {
    "tipo": "AVARIA",
    "descricaoTipo": "Avaria",
    "dataHora": "2026-05-10T15:14:00",
    "municipio": "Quixadá",
    "uf": "CE",
    "descricao": "Embalagem lateral danificada.",
    "nomeRecebedor": "",
    "documentoRecebedor": ""
  }
}
```

### Regras reais do payload

- o legado não remonta JSON no momento do envio
- ele envia exatamente o conteúdo salvo em `evento_sistema.payload`
- `ENTREGA_REALIZADA` não gera `OCORRENCIA_FRETE_REGISTRADA`
- `ENTREGA_REALIZADA` gera `FRETE_ENTREGUE`, com bloco `ocorrencia`
- outros eventos de frete também podem carregar `ocorrencia`

## Contrato do WebSocket/STOMP

O dashboard não abre WebSocket no legado.

Ele abre WebSocket diretamente na API de mensageria:

- `ws://localhost:8082/ws-fretes`
- tópico: `/topic/fretes`

Esses valores são injetados na JSP por:

- `MensageriaConfig.websocketUrl()`
- `MensageriaConfig.websocketTopic()`

### Frame STOMP de conexão

Exemplo enviado pelo dashboard:

```text
CONNECT
accept-version:1.2,1.1
host:localhost:8082
heart-beat:10000,10000

\0
```

### Frame STOMP de subscribe

```text
SUBSCRIBE
id:fretes-dashboard
destination:/topic/fretes
ack:auto

\0
```

### Payload JSON recebido no `MESSAGE`

O corpo do frame `MESSAGE` é JSON. O dashboard tenta fazer `JSON.parse(frame.body)`.

Exemplo:

```json
{
  "versao": "1.0",
  "evento": "FRETE_EM_TRANSITO",
  "origem": "SISTEMA_FRETES_WEB",
  "dataEvento": "2026-05-10T15:20:00",
  "frete": {
    "id": 12,
    "numero": "FRT-2026-00012",
    "status": "EM_TRANSITO",
    "origem": "Fortaleza/CE",
    "destino": "Recife/PE",
    "pesoKg": 1200.0,
    "valorTotal": 8960.0
  }
}
```

### Regra real de consumo do dashboard

O dashboard não depende exclusivamente do WebSocket.

Fluxo:

1. recebe o evento em tempo real
2. extrai um patch parcial do bloco `frete`
3. atualiza a UI localmente
4. agenda uma hidratação
5. chama novamente `GET /FreteController?acao=buscar&filtro=`

Isso evita depender de payload WebSocket completo para toda a tela.

## Contrato da API externa de login

O login não é implementado no legado.

O frontend consome uma API externa:

- `POST http://localhost:8080/auth/login`
- `POST http://localhost:8080/auth/cadastro`

Essas URLs estão em:

- `src/main/webapp/js/auth.js`

### 1. Login

Request:

```json
{
  "email": "usuario@empresa.com",
  "senha": "123456"
}
```

Response esperada de sucesso:

```json
{
  "token": "jwt-ou-token-equivalente",
  "nome": "Usuário Exemplo",
  "email": "usuario@empresa.com"
}
```

Compatibilidade de resposta no frontend:

- o código aceita `nome` ou `name`
- se `nome` não vier, usa o prefixo do e-mail como fallback

Tratamento de erro:

- `401` ou `403` vira mensagem de credencial inválida
- qualquer `2xx` é tratado como sucesso
- qualquer outro status é tratado como falha de autenticação

### 2. Cadastro

Request:

```json
{
  "nome": "Usuário Exemplo",
  "email": "usuario@empresa.com",
  "senha": "123456"
}
```

Response esperada de sucesso:

```json
{
  "id": 10,
  "nome": "Usuário Exemplo",
  "email": "usuario@empresa.com"
}
```

Após o cadastro, o frontend faz login automático:

```json
{
  "email": "usuario@empresa.com",
  "senha": "123456"
}
```

### Persistência local no navegador

Em caso de sucesso, o frontend salva:

```json
{
  "nome": "Usuário Exemplo",
  "email": "usuario@empresa.com"
}
```

na chave:

- `localStorage["usuario"]`

E salva o token em:

- `localStorage["token"]`

## Regras de integração importantes

- o legado é a fonte de verdade dos fretes
- o outbox `evento_sistema` garante persistência antes da publicação
- a mensageria publica o `payload` bruto salvo no banco
- o dashboard usa WebSocket para latência baixa
- o dashboard usa hidratação HTTP para consistência
- a API externa de login é dependência separada do legado
- o login e o cadastro não passam por BO ou DAO deste projeto

## Fluxo técnico completo

### Cenário: frete sai do pátio

1. O usuário executa `confirmarSaida`.
2. `FreteController` chama `freteBO.confirmarSaida(...)`.
3. `FreteBO` cria o evento `FRETE_SAIDA_CONFIRMADA`.
4. `FreteDAO` grava a mudança no frete e no `evento_sistema` na mesma transação.
5. `MensageriaController` ou outra rotina dispara o envio dos pendentes.
6. `MensageriaBO` lê o outbox.
7. `MensageriaHttpClient` faz `POST` para a API de mensageria.
8. A API de mensageria retransmite ao dashboard via STOMP/WebSocket.
9. O dashboard atualiza a tela imediatamente.
10. O dashboard consulta o legado para consolidar o estado final.

### Cenário: entrega concluída

1. O dashboard abre modal e coleta `nomeRecebedor` e `documentoRecebedor`.
2. O frontend envia `acao=entregar`.
3. `FreteController` monta uma ocorrência de `ENTREGA_REALIZADA`.
4. `FreteBO.entregar(...)` valida os dados do recebedor.
5. `FreteDAO.atualizarStatus(...)` grava:
   - status do frete
   - data de entrega
   - ocorrência de entrega
   - evento `FRETE_ENTREGUE`
6. O outbox publica esse payload na API de mensageria.
7. O dashboard recebe o evento e reidrata a lista.

## Observações finais

- o projeto não expõe uma API REST completa para todos os módulos; ele mistura JSP e endpoints JSON utilitários
- o login é terceirizado e consumido apenas pelo frontend
- o contrato de mensageria é mais rico que o contrato JSON do dashboard
- o contrato do dashboard é parcial e tolera hidratação posterior
