package br.com.mvc.model.enums;

/**
 * Perfil do usuário logado. Como o projeto ainda não tem autenticação,
 * o perfil é escolhido na tela inicial e fica guardado na sessão.
 */
public enum Role {
    USUARIO("Usuário"),
    ADM("Administrador");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
