package br.com.archat.dominio.cliente;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

/** CPF ou CNPJ normalizado (somente dígitos) e com os dígitos verificadores conferidos. */
public record Documento(String numero, TipoDocumento tipo) {

    private static final int[] PESOS_CPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Documento {
        if (tipo == null) {
            throw new RegraDeNegocioException("Tipo de documento é obrigatório");
        }
        numero = numero == null ? "" : numero.replaceAll("\\D", "");
        if (!valido(numero, tipo)) {
            throw new RegraDeNegocioException(tipo + " inválido");
        }
    }

    private static boolean valido(String numero, TipoDocumento tipo) {
        if (numero.length() != tipo.quantidadeDigitos() || numero.chars().distinct().count() == 1) {
            return false;
        }
        int[] pesos = tipo == TipoDocumento.CPF ? PESOS_CPF : PESOS_CNPJ;
        int tamanhoBase = numero.length() - 2;
        int primeiro = digitoVerificador(numero, tamanhoBase, pesos, pesos.length - tamanhoBase);
        int segundo = digitoVerificador(numero, tamanhoBase + 1, pesos, pesos.length - tamanhoBase - 1);
        return primeiro == digito(numero, tamanhoBase) && segundo == digito(numero, tamanhoBase + 1);
    }

    private static int digitoVerificador(String numero, int quantidade, int[] pesos, int inicioPesos) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digito(numero, i) * pesos[inicioPesos + i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digito(String numero, int posicao) {
        return numero.charAt(posicao) - '0';
    }
}
