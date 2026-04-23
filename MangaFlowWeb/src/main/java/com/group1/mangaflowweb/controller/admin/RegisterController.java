package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.user.UserDTO;
import com.group1.mangaflowweb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("hideNav", true);
        model.addAttribute("registerRequest", new UserDTO());
        return "clients/home/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute UserDTO registerRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("hideNav", true);

        if (bindingResult.hasErrors()) {
            return "clients/home/register";
        }

        try {
            userService.registerReader(registerRequest);
        } catch (IllegalArgumentException ex) {
            if ("USERNAME_EXISTS".equals(ex.getMessage())) {
                bindingResult.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại!");
                return "clients/home/register";
            }
            if ("EMAIL_EXISTS".equals(ex.getMessage())) {
                bindingResult.rejectValue("email", "error.email", "Email đã được đăng ký!");
                return "clients/home/register";
            }
            bindingResult.reject("error.register", "Không thể đăng ký tài khoản, vui lòng thử lại.");
            return "clients/home/register";
        }

        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}
