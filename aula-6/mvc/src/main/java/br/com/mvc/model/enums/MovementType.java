package br.com.mvc.model.enums;

public enum MovementType {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
