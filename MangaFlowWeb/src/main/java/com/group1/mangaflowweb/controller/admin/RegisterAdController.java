package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterAdController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterAdController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("hideNav", true);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            model.addAttribute("hideNav", true);
            return "register";
        }

        Users newUser = Users.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("READER")
                .enabled(true)
                .build();

        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}
