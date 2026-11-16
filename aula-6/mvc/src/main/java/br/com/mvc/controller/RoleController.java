package br.com.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.mvc.model.enums.Role;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/role")
public class RoleController {

    @PostMapping
    public String change(@RequestParam Role role,
            @RequestParam(defaultValue = "/") String returnTo,
            HttpSession session) {
        SessionRole.set(session, role);
        boolean localPath = returnTo.startsWith("/") && !returnTo.startsWith("//");
        return "redirect:" + (localPath ? returnTo : "/");
    }
}
