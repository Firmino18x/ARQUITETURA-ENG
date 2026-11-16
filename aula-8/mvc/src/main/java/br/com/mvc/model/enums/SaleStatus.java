package br.com.mvc.model.enums;

public enum SaleStatus {
    ABERTA("Aberta"),
    /** Fechada localmente (estoque baixado), aguardando o ID da fatura do hub. */
    FECHADA("Fechada – aguardando faturamento"),
    FATURADA("Faturada"),
    /** Fechada, mas a chamada ao hub falhou: está sem fatura. */
    FALHA_FATURAMENTO("Falha no faturamento");

    private final String label;

    SaleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
