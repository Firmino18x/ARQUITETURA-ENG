package br.com.mvc.controller;

import br.com.mvc.model.enums.Role;
import jakarta.servlet.http.HttpSession;

/** Lê e grava o perfil escolhido na sessão (substitui a autenticação neste projeto). */
public final class SessionRole {

    public static final String KEY = "role";

    private SessionRole() {
    }

    public static Role get(HttpSession session) {
        Object role = session.getAttribute(KEY);
        return role instanceof Role r ? r : Role.USUARIO;
    }

    public static void set(HttpSession session, Role role) {
        session.setAttribute(KEY, role == null ? Role.USUARIO : role);
    }
}
