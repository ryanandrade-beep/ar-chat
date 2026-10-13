-- Atendimentos: cada conversa passa por ciclos de atendimento (aguardando, em andamento, em espera, finalizado)

ALTER TABLE conversa ADD COLUMN status_atendimento VARCHAR(12) NOT NULL DEFAULT 'FINALIZADO';
ALTER TABLE conversa ADD CONSTRAINT ck_conversa_status_atendimento
    CHECK (status_atendimento IN ('AGUARDANDO', 'EM_ANDAMENTO', 'EM_ESPERA', 'FINALIZADO'));
CREATE INDEX idx_conversa_status ON conversa (cliente_id, status_atendimento);

CREATE TABLE atendimento (
    id                    UUID         PRIMARY KEY,
    conversa_id           UUID         NOT NULL REFERENCES conversa (id),
    cliente_id            UUID         NOT NULL REFERENCES cliente (id),
    origem                VARCHAR(13)  NOT NULL CHECK (origem IN ('CLIENTE_FINAL', 'EMPRESA')),
    status                VARCHAR(12)  NOT NULL CHECK (status IN ('AGUARDANDO', 'EM_ANDAMENTO', 'EM_ESPERA', 'FINALIZADO')),
    aberto_em             TIMESTAMP WITH TIME ZONE NOT NULL,
    primeira_resposta_em  TIMESTAMP WITH TIME ZONE,
    finalizado_em         TIMESTAMP WITH TIME ZONE,
    mensagens_enviadas    INTEGER      NOT NULL DEFAULT 0,
    mensagens_recebidas   INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_atendimento_conversa ON atendimento (conversa_id, status);
CREATE INDEX idx_atendimento_finalizado ON atendimento (cliente_id, finalizado_em);

-- identificador da mensagem no canal (messageId da Z-API), usado pelos webhooks de entrega e leitura
ALTER TABLE mensagem ADD COLUMN id_externo VARCHAR(100);
CREATE UNIQUE INDEX uk_mensagem_id_externo ON mensagem (id_externo);
