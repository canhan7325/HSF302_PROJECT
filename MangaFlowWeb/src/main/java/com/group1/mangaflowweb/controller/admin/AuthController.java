package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("hideNav", true);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            Model model) {
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Get role
            String role = "USER";
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                role = authority.getAuthority().replace("ROLE_", "");
                break;
            }

            // Generate JWT
            String token = jwtUtil.generateToken(username, role);

            // Store in Session
            HttpSession session = request.getSession();
            session.setAttribute("JWT_TOKEN", token);

            // Redirect based on role
            if ("ADMIN".equalsIgnoreCase(role)) {
                return "redirect:/admin";
            } else if ("READER".equalsIgnoreCase(role)) {
                return "redirect:/";
            } else {
                return "redirect:/";
            }
        } catch (AuthenticationException e) {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("hideNav", true);
            return "login";
        }
    }
}
