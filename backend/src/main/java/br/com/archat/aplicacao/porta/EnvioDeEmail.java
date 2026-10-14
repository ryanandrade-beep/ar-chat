package br.com.archat.aplicacao.porta;

/** Envia e-mails transacionais (ex.: o código de recuperação de senha). */
public interface EnvioDeEmail {

    void enviar(String para, String assunto, String texto);
}
