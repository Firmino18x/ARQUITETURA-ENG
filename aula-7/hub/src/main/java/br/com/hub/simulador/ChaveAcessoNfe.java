package br.com.hub.simulador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera uma chave de acesso de NF-e com 44 dígitos no layout oficial:
 * cUF(2) + AAMM(4) + CNPJ(14) + modelo(2) + série(3) + número(9) + tpEmis(1) + código(8) + DV(1).
 */
public final class ChaveAcessoNfe {

    private static final String UF_SP = "35";
    private static final String MODELO_NFE = "55";
    private static final String SERIE = "001";
    private static final String EMISSAO_NORMAL = "1";

    private ChaveAcessoNfe() {
    }

    public static String gerar(String cnpjEmitente, long numeroNota, LocalDate emissao) {
        String aamm = emissao.format(DateTimeFormatter.ofPattern("yyMM"));
        String codigo = "%08d".formatted(ThreadLocalRandom.current().nextInt(100_000_000));
        String semDv = UF_SP + aamm + cnpjEmitente + MODELO_NFE + SERIE
                + "%09d".formatted(numeroNota) + EMISSAO_NORMAL + codigo;
        return semDv + digitoVerificador(semDv);
    }

    /** Módulo 11 com pesos de 2 a 9, da direita para a esquerda. */
    static int digitoVerificador(String numeros) {
        int soma = 0;
        int peso = 2;
        for (int i = numeros.length() - 1; i >= 0; i--) {
            soma += (numeros.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
