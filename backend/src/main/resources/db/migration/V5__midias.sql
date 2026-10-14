-- Mensagens e campanhas com mídia (imagem, documento, áudio ou vídeo).
-- O arquivo não é guardado: guarda-se o link público de onde o canal o baixa. Com mídia, o conteúdo é a legenda.
ALTER TABLE mensagem ADD COLUMN tipo_midia   VARCHAR(10)   CHECK (tipo_midia IN ('IMAGEM', 'DOCUMENTO', 'AUDIO', 'VIDEO'));
ALTER TABLE mensagem ADD COLUMN url_midia    VARCHAR(2048);
ALTER TABLE mensagem ADD COLUMN nome_arquivo VARCHAR(255);
ALTER TABLE mensagem ADD CONSTRAINT ck_mensagem_midia CHECK ((tipo_midia IS NULL) = (url_midia IS NULL));

ALTER TABLE campanha ADD COLUMN tipo_midia   VARCHAR(10)   CHECK (tipo_midia IN ('IMAGEM', 'DOCUMENTO', 'AUDIO', 'VIDEO'));
ALTER TABLE campanha ADD COLUMN url_midia    VARCHAR(2048);
ALTER TABLE campanha ADD COLUMN nome_arquivo VARCHAR(255);
ALTER TABLE campanha ADD CONSTRAINT ck_campanha_midia CHECK ((tipo_midia IS NULL) = (url_midia IS NULL));
