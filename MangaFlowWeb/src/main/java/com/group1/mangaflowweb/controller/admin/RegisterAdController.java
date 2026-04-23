package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.request.RegisterRequest;
import com.group1.mangaflowweb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterAdController {

    private final UserService userService;

    public RegisterAdController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("hideNav", true);
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("hideNav", true);

        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Kiểm tra tên đăng nhập đã tồn tại
        if (userService.existsByUsername(registerRequest.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại!");
            return "register";
        }

        // Kiểm tra email đã tồn tại
        if (userService.existsByEmail(registerRequest.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email đã được đăng ký!");
            return "register";
        }

        userService.registerUser(registerRequest);

        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}
