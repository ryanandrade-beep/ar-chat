-- A&R Chat - estrutura inicial do banco

CREATE TABLE cliente (
    id              UUID           PRIMARY KEY,
    nome            VARCHAR(120)   NOT NULL,
    documento       VARCHAR(14)    NOT NULL UNIQUE,
    tipo_documento  VARCHAR(4)     NOT NULL CHECK (tipo_documento IN ('CPF', 'CNPJ')),
    tipo_plano      VARCHAR(10)    NOT NULL CHECK (tipo_plano IN ('PRE_PAGO', 'POS_PAGO')),
    saldo           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    limite_mensal   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    consumo_mes     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ativo           BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE destinatario (
    id          UUID         PRIMARY KEY,
    cliente_id  UUID         NOT NULL REFERENCES cliente (id),
    nome        VARCHAR(120) NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_destinatario_cliente_telefone UNIQUE (cliente_id, telefone)
);

CREATE TABLE conversa (
    id                    UUID         PRIMARY KEY,
    cliente_id            UUID         NOT NULL REFERENCES cliente (id),
    destinatario_id       UUID         NOT NULL REFERENCES destinatario (id),
    ultima_mensagem       VARCHAR(200),
    data_ultima_mensagem  TIMESTAMP WITH TIME ZONE,
    nao_lidas             INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_conversa_destinatario UNIQUE (destinatario_id)
);
CREATE INDEX idx_conversa_cliente ON conversa (cliente_id, data_ultima_mensagem);

CREATE TABLE mensagem (
    id              UUID          PRIMARY KEY,
    conversa_id     UUID          NOT NULL REFERENCES conversa (id),
    remetente_tipo  VARCHAR(12)   NOT NULL CHECK (remetente_tipo IN ('CLIENTE', 'DESTINATARIO')),
    conteudo        TEXT          NOT NULL,
    prioridade      VARCHAR(10)   NOT NULL CHECK (prioridade IN ('NORMAL', 'URGENTE')),
    status          VARCHAR(12)   NOT NULL,
    custo           NUMERIC(5, 2) NOT NULL,
    tentativas      INTEGER       NOT NULL DEFAULT 0,
    criada_em       TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizada_em   TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_mensagem_conversa ON mensagem (conversa_id, criada_em);
CREATE INDEX idx_mensagem_status ON mensagem (status);

CREATE TABLE transacao (
    id           UUID           PRIMARY KEY,
    cliente_id   UUID           NOT NULL REFERENCES cliente (id),
    mensagem_id  UUID           REFERENCES mensagem (id),
    tipo         VARCHAR(15)    NOT NULL CHECK (tipo IN ('CREDITO', 'DEBITO', 'ESTORNO', 'AJUSTE_LIMITE')),
    valor        NUMERIC(12, 2) NOT NULL,
    saldo_apos   NUMERIC(12, 2) NOT NULL,
    data_hora    TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_transacao_cliente ON transacao (cliente_id, data_hora);

CREATE TABLE administrador (
    id          UUID         PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    login       VARCHAR(50)  NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL
);
