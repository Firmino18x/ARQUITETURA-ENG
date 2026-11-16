package br.com.mvc.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.mvc.model.enums.Role;
import jakarta.servlet.http.HttpSession;

/** Disponibiliza o perfil atual e a lista de perfis para todas as telas. */
@ControllerAdvice
public class RoleModelAdvice {

    @ModelAttribute
    public void addRole(Model model, HttpSession session) {
        model.addAttribute("currentRole", SessionRole.get(session));
        model.addAttribute("roles", Role.values());
    }
}
