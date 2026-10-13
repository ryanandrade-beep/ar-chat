-- Campanhas: disparo de uma mesma mensagem para vários destinatários

-- LGPD: só recebe campanha quem autorizou receber ofertas
ALTER TABLE destinatario ADD COLUMN aceita_ofertas BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE campanha (
    id                   UUID          PRIMARY KEY,
    cliente_id           UUID          NOT NULL REFERENCES cliente (id),
    nome                 VARCHAR(120)  NOT NULL,
    conteudo             TEXT          NOT NULL,
    prioridade           VARCHAR(10)   NOT NULL CHECK (prioridade IN ('NORMAL', 'URGENTE')),
    total_destinatarios  INTEGER       NOT NULL,
    custo_total          NUMERIC(12, 2) NOT NULL,
    criada_em            TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_campanha_cliente ON campanha (cliente_id, criada_em);

ALTER TABLE mensagem ADD COLUMN campanha_id UUID REFERENCES campanha (id);
CREATE INDEX idx_mensagem_campanha ON mensagem (campanha_id);
