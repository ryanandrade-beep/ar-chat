-- Acesso do cliente ao painel por e-mail e senha (cadastro pela tela "Criar conta") e recuperação de senha.
-- Clientes cadastrados pelo administrador continuam entrando pelo CPF/CNPJ até criarem a conta.
CREATE TABLE conta_acesso (
    cliente_id          UUID          PRIMARY KEY REFERENCES cliente (id),
    email               VARCHAR(160)  NOT NULL UNIQUE,
    telefone            VARCHAR(13)   NOT NULL,
    senha_hash          VARCHAR(100)  NOT NULL,
    versao_termos       VARCHAR(20)   NOT NULL,
    termos_aceitos_em   TIMESTAMP WITH TIME ZONE NOT NULL,
    codigo_hash         VARCHAR(100),
    codigo_expira_em    TIMESTAMP WITH TIME ZONE,
    tentativas_codigo   INTEGER       NOT NULL DEFAULT 0
);
