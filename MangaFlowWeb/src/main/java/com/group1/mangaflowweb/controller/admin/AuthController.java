package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SessionRegistry sessionRegistry;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, SessionRegistry sessionRegistry) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.sessionRegistry = sessionRegistry;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String expired, Model model) {
        if ("true".equals(expired)) {
            model.addAttribute("error", "Tài khoản của bạn đã được đăng nhập từ một thiết bị khác. Vui lòng đăng nhập lại.");
        }
        model.addAttribute("hideNav", true);
        return "clients/home/login";
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

            // Register session in SessionRegistry for single session control
            SecurityContextHolder.getContext().setAuthentication(authentication);
            sessionRegistry.registerNewSession(session.getId(), authentication.getPrincipal());

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
            return "clients/home/login";
        }
    }
}


