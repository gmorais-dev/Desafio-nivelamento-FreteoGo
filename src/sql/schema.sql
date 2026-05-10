CREATE TABLE cliente (
    id                 SERIAL       PRIMARY KEY,
    razao_social       VARCHAR(200) NOT NULL,
    nome_fantasia      VARCHAR(200),
    cnpj               VARCHAR(18)  NOT NULL UNIQUE,
    inscricao_estadual VARCHAR(20),
    tipo               VARCHAR(20)  NOT NULL,
    logradouro         VARCHAR(200),
    numero             INT,
    complemento        VARCHAR(100),
    bairro             VARCHAR(100),
    municipio          VARCHAR(100),
    uf                 VARCHAR(2),
    cep                VARCHAR(10),
    telefone           VARCHAR(20),
    email              VARCHAR(150),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE motorista (
    id                 SERIAL       PRIMARY KEY,
    nome               VARCHAR(200) NOT NULL,
    cpf                VARCHAR(14)  NOT NULL UNIQUE,
    data_nascimento    DATE,
    telefone           VARCHAR(20),
    cnh_numero         VARCHAR(20)  NOT NULL UNIQUE,
    cnh_categoria      VARCHAR(5),
    cnh_validade       DATE,
    tipo_vinculo       VARCHAR(30),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE veiculo (
    id                 SERIAL       PRIMARY KEY,
    placa              VARCHAR(10)  NOT NULL UNIQUE,
    rntrc              VARCHAR(20),
    ano_fabricacao      INT,
    tipo               VARCHAR(50),
    tara_kg            FLOAT,
    capacidade_kg      FLOAT,
    volume_m3          FLOAT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL'
);

CREATE TABLE frete (
    id                    SERIAL       PRIMARY KEY,
    numero                VARCHAR(30)  NOT NULL UNIQUE,
    id_remetente          INT          NOT NULL,
    id_destinatario       INT          NOT NULL,
    id_motorista          INT          NOT NULL,
    id_veiculo            INT          NOT NULL,
    municipio_origem      VARCHAR(100),
    uf_origem             VARCHAR(2),
    municipio_destino     VARCHAR(100),
    uf_destino            VARCHAR(2),
    descricao_carga       VARCHAR(500),
    peso_kg               FLOAT,
    volumes               INT,
    valor_frete           FLOAT,
    aliquota_icms         FLOAT,
    valor_icms            FLOAT,
    valor_total           FLOAT,
    status                VARCHAR(30)  NOT NULL DEFAULT 'EMITIDO',
    data_emissao          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_previsao_entrega DATE,
    data_saida            TIMESTAMP,
    data_entrega          TIMESTAMP,

    CONSTRAINT fk_frete_remetente    FOREIGN KEY (id_remetente)    REFERENCES cliente   (id),
    CONSTRAINT fk_frete_destinatario FOREIGN KEY (id_destinatario) REFERENCES cliente   (id),
    CONSTRAINT fk_frete_motorista    FOREIGN KEY (id_motorista)    REFERENCES motorista (id),
    CONSTRAINT fk_frete_veiculo      FOREIGN KEY (id_veiculo)      REFERENCES veiculo   (id)
);

CREATE TABLE evento_sistema (
    id                    SERIAL       PRIMARY KEY,
    tipo                  VARCHAR(50)  NOT NULL,
    entidade              VARCHAR(50)  NOT NULL,
    entidade_id           INT          NOT NULL,
    payload               TEXT         NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PENDENTE',
    tentativas            INT          NOT NULL DEFAULT 0,
    data_criacao          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_publicacao       TIMESTAMP,
    mensagem_erro         VARCHAR(500)
);

CREATE TABLE ocorrencia_frete (
    id                    SERIAL       PRIMARY KEY,
    id_frete              INT          NOT NULL,
    tipo                  VARCHAR(50)  NOT NULL,
    data_hora             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    municipio             VARCHAR(100) NOT NULL,
    uf                    VARCHAR(2)   NOT NULL,
    descricao             VARCHAR(500),
    nome_recebedor        VARCHAR(200),
    documento_recebedor   VARCHAR(30),

    CONSTRAINT fk_ocorrencia_frete FOREIGN KEY (id_frete) REFERENCES frete (id)
);

CREATE INDEX idx_cliente_cnpj          ON cliente          (cnpj);
CREATE INDEX idx_motorista_cpf         ON motorista        (cpf);
CREATE INDEX idx_veiculo_placa         ON veiculo          (placa);
CREATE INDEX idx_frete_numero          ON frete            (numero);
CREATE INDEX idx_frete_status          ON frete            (status);
CREATE INDEX idx_frete_remetente       ON frete            (id_remetente);
CREATE INDEX idx_frete_destinatario    ON frete            (id_destinatario);
CREATE INDEX idx_ocorrencia_id_frete   ON ocorrencia_frete (id_frete);
CREATE INDEX idx_evento_sistema_status ON evento_sistema   (status);
