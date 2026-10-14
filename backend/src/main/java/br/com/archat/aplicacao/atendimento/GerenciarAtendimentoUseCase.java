package br.com.archat.aplicacao.atendimento;

import br.com.archat.aplicacao.porta.AtendimentoRepositorio;
import br.com.archat.aplicacao.porta.ConversaRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.atendimento.Atendimento;
import br.com.archat.dominio.atendimento.StatusAtendimento;
import br.com.archat.dominio.conversa.Conversa;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.time.Clock;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** Ações da empresa sobre o atendimento: colocar em espera, retomar, finalizar e marcar como lido. */
public class GerenciarAtendimentoUseCase {

    private final ConversaRepositorio conversas;
    private final AtendimentoRepositorio atendimentos;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public GerenciarAtendimentoUseCase(ConversaRepositorio conversas, AtendimentoRepositorio atendimentos,
                                       UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.conversas = conversas;
        this.atendimentos = atendimentos;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public Conversa colocarEmEspera(UUID clienteId, UUID conversaId) {
        return alterar(clienteId, conversaId, Atendimento::colocarEmEspera);
    }

    public Conversa retomar(UUID clienteId, UUID conversaId) {
        return alterar(clienteId, conversaId, Atendimento::retomar);
    }

    public Conversa finalizar(UUID clienteId, UUID conversaId) {
        return alterar(clienteId, conversaId, atendimento -> atendimento.finalizar(relogio.instant()));
    }

    public Conversa marcarComoLida(UUID clienteId, UUID conversaId) {
        return unidadeDeTrabalho.executar(() -> {
            Conversa conversa = daEmpresa(clienteId, conversaId);
            conversa.marcarComoLida();
            conversas.salvar(conversa);
            return conversa;
        });
    }

    public Map<StatusAtendimento, Long> resumo(UUID clienteId) {
        Map<StatusAtendimento, Long> resumo = new EnumMap<>(StatusAtendimento.class);
        for (StatusAtendimento status : StatusAtendimento.values()) {
            resumo.put(status, 0L);
        }
        conversas.listarPorCliente(clienteId).forEach(c -> resumo.merge(c.statusAtendimento(), 1L, Long::sum));
        return resumo;
    }

    private Conversa alterar(UUID clienteId, UUID conversaId, Consumer<Atendimento> acao) {
        return unidadeDeTrabalho.executar(() -> {
            Conversa conversa = daEmpresa(clienteId, conversaId);
            Atendimento atendimento = atendimentos.buscarAbertoDaConversa(conversaId)
                    .orElseThrow(() -> new RegraDeNegocioException("Esta conversa não tem atendimento aberto"));
            acao.accept(atendimento);
            atendimentos.salvar(atendimento);
            conversa.atualizarStatusAtendimento(atendimento.status());
            conversas.salvar(conversa);
            return conversa;
        });
    }

    private Conversa daEmpresa(UUID clienteId, UUID conversaId) {
        return conversas.buscarPorId(conversaId)
                .filter(c -> c.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa"));
    }
}
