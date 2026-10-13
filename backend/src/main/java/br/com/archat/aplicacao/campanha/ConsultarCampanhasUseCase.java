package br.com.archat.aplicacao.campanha;

import br.com.archat.aplicacao.porta.CampanhaRepositorio;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.dominio.campanha.Campanha;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Campanhas do cliente com o andamento das mensagens (quantas enfileiradas, entregues, com falha...). */
public class ConsultarCampanhasUseCase {

    private final CampanhaRepositorio campanhas;
    private final MensagemRepositorio mensagens;

    public ConsultarCampanhasUseCase(CampanhaRepositorio campanhas, MensagemRepositorio mensagens) {
        this.campanhas = campanhas;
        this.mensagens = mensagens;
    }

    public List<AndamentoCampanha> listar(UUID clienteId) {
        return campanhas.listarPorCliente(clienteId).stream().map(this::comAndamento).toList();
    }

    public AndamentoCampanha buscar(UUID clienteId, UUID campanhaId) {
        return campanhas.buscarPorId(campanhaId)
                .filter(c -> c.pertenceA(clienteId))
                .map(this::comAndamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Campanha"));
    }

    private AndamentoCampanha comAndamento(Campanha campanha) {
        return new AndamentoCampanha(campanha, mensagens.contarPorStatusDaCampanha(campanha.id()));
    }

    public record AndamentoCampanha(Campanha campanha, Map<StatusMensagem, Long> mensagensPorStatus) {
    }
}
