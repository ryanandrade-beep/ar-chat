package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/** Unidade de trabalho sobre a transação do Spring: o bloco inteiro é confirmado ou desfeito. */
@Component
class UnidadeDeTrabalhoSpring implements UnidadeDeTrabalho {

    private final TransactionTemplate transacao;

    UnidadeDeTrabalhoSpring(TransactionTemplate transacao) {
        this.transacao = transacao;
    }

    @Override
    public <T> T executar(Supplier<T> bloco) {
        return transacao.execute(status -> bloco.get());
    }
}
