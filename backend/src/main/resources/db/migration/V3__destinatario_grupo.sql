-- Destinatários podem ser contatos (telefone) ou grupos do WhatsApp (ID do grupo, ex.: 120363019502650977-group)

ALTER TABLE destinatario ADD COLUMN tipo VARCHAR(10) NOT NULL DEFAULT 'CONTATO';
ALTER TABLE destinatario ADD CONSTRAINT ck_destinatario_tipo CHECK (tipo IN ('CONTATO', 'GRUPO'));
ALTER TABLE destinatario ALTER COLUMN telefone TYPE VARCHAR(40);
