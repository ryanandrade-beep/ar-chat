package br.com.archat.aplicacao.atendimento;

import br.com.archat.aplicacao.porta.AtendimentoRepositorio;
import br.com.archat.dominio.atendimento.Atendimento;
import br.com.archat.dominio.conversa.Conversa;

import java.time.Instant;

/**
 * Mantém o atendimento da conversa em dia com as mensagens trocadas. Usado pelos casos de uso de envio
 * e de recebimento, sempre dentro da mesma transação deles. Mensagens de campanha não passam por aqui:
 * marketing não abre atendimento, só a resposta do cliente final abre.
 */
public class RegistroDeAtendimento {

    private final AtendimentoRepositorio atendimentos;

    public RegistroDeAtendimento(AtendimentoRepositorio atendimentos) {
        this.atendimentos = atendimentos;
    }

    public Atendimento aoResponder(Conversa conversa, Instant agora) {
        Atendimento atendimento = atendimentos.buscarAbertoDaConversa(conversa.id())
                .map(aberto -> {
                    aberto.registrarEnvio(agora);
                    return aberto;
                })
                .orElseGet(() -> Atendimento.abertoPelaEmpresa(conversa.id(), conversa.clienteId(), agora));
        return salvar(conversa, atendimento);
    }

    public Atendimento aoReceber(Conversa conversa, Instant agora) {
        Atendimento atendimento = atendimentos.buscarAbertoDaConversa(conversa.id())
                .map(aberto -> {
                    aberto.registrarRecebimento();
                    return aberto;
                })
                .orElseGet(() -> Atendimento.abertoPeloClienteFinal(conversa.id(), conversa.clienteId(), agora));
        return salvar(conversa, atendimento);
    }

    private Atendimento salvar(Conversa conversa, Atendimento atendimento) {
        atendimentos.salvar(atendimento);
        conversa.atualizarStatusAtendimento(atendimento.status());
        return atendimento;
    }
}
