package br.com.archat.aplicacao.mensagem;

import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.time.Clock;
import java.util.Collection;

/**
 * Aplica os avisos de entrega e leitura que o canal manda (webhook de status). Avisos repetidos,
 * fora de ordem ou de mensagens que não são nossas são ignorados.
 */
public class AtualizarStatusPeloCanalUseCase {

    public enum AvisoDoCanal {
        ENTREGUE,
        LIDA
    }

    private final MensagemRepositorio mensagens;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public AtualizarStatusPeloCanalUseCase(MensagemRepositorio mensagens, UnidadeDeTrabalho unidadeDeTrabalho,
                                           Clock relogio) {
        this.mensagens = mensagens;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public int aplicar(Collection<String> idsExternos, AvisoDoCanal aviso) {
        return unidadeDeTrabalho.executar(() -> (int) idsExternos.stream()
                .map(mensagens::buscarPorIdExterno)
                .flatMap(java.util.Optional::stream)
                .filter(m -> aplicar(m, aviso))
                .count());
    }

    private boolean aplicar(Mensagem mensagem, AvisoDoCanal aviso) {
        boolean mudou = false;
        if (mensagem.status() == StatusMensagem.ENVIADA) {
            mensagem.confirmarEntrega(relogio.instant());
            mudou = true;
        }
        if (aviso == AvisoDoCanal.LIDA && mensagem.status() == StatusMensagem.ENTREGUE) {
            mensagem.confirmarLeitura(relogio.instant());
            mudou = true;
        }
        if (mudou) {
            mensagens.salvar(mensagem);
        }
        return mudou;
    }
}
