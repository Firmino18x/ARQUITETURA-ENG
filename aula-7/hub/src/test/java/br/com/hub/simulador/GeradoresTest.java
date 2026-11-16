package br.com.hub.simulador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class GeradoresTest {

    @Test
    void chaveNfeTem44DigitosEDvValido() {
        String chave = ChaveAcessoNfe.gerar("12345678000195", 1, LocalDate.of(2026, 11, 16));

        assertTrue(chave.matches("\\d{44}"));
        assertTrue(chave.startsWith("352611"));
        assertEquals(ChaveAcessoNfe.digitoVerificador(chave.substring(0, 43)), chave.charAt(43) - '0');
    }

    @Test
    void codigoDeBarrasTem44DigitosComValorEDv() {
        String codigo = CodigoBarrasBoleto.gerar(new BigDecimal("259.00"), LocalDate.of(2026, 11, 19));

        assertTrue(codigo.matches("\\d{44}"));
        assertTrue(codigo.startsWith("0019"));
        assertEquals("0000025900", codigo.substring(9, 19));
        String semDv = codigo.substring(0, 4) + codigo.substring(5);
        assertEquals(CodigoBarrasBoleto.digitoVerificador(semDv), codigo.charAt(4) - '0');
    }
}
