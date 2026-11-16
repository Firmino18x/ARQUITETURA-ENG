package br.com.hub.simulador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera um código de barras de boleto (44 dígitos, padrão FEBRABAN):
 * banco(3) + moeda(1) + DV(1) + fator de vencimento(4) + valor(10) + campo livre(25).
 */
public final class CodigoBarrasBoleto {

    private static final String BANCO = "001";
    private static final String MOEDA_REAL = "9";
    /** A partir de 22/02/2025 o fator de vencimento recomeça em 1000. */
    private static final LocalDate BASE_FATOR = LocalDate.of(2025, 2, 22);

    private CodigoBarrasBoleto() {
    }

    public static String gerar(BigDecimal valor, LocalDate vencimento) {
        long fator = 1000 + ChronoUnit.DAYS.between(BASE_FATOR, vencimento);
        long centavos = valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        if (fator < 1000 || fator > 9999 || centavos > 9_999_999_999L) {
            throw new IllegalArgumentException("Valor ou vencimento fora do intervalo do boleto.");
        }
        StringBuilder campoLivre = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            campoLivre.append(ThreadLocalRandom.current().nextInt(10));
        }
        String semDv = BANCO + MOEDA_REAL + "%04d".formatted(fator) + "%010d".formatted(centavos) + campoLivre;
        return semDv.substring(0, 4) + digitoVerificador(semDv) + semDv.substring(4);
    }

    /** Módulo 11 do código de barras: pesos 2 a 9; resultados 0, 10 e 11 viram 1. */
    static int digitoVerificador(String numeros) {
        int soma = 0;
        int peso = 2;
        for (int i = numeros.length() - 1; i >= 0; i--) {
            soma += (numeros.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int dv = 11 - (soma % 11);
        return (dv == 0 || dv == 10 || dv == 11) ? 1 : dv;
    }
}
