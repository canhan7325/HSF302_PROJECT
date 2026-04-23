package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.user.UserAdminDTO;
import com.group1.mangaflowweb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class UserAdController {

    private final UserService userService;

    public UserAdController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String userList(@PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search,
            Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("page", userService.searchUsers(search, pageable));
        } else {
            model.addAttribute("page", userService.getUsersPage(pageable));
        }
        model.addAttribute("search", search);
        model.addAttribute("view", "list");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String userNewForm(Model model) {
        model.addAttribute("user", new UserAdminDTO());
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users";
    }

    @PostMapping("/users/new")
    public String userCreate(@Valid UserAdminDTO user, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Manual validation for password (required for create)
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            result.rejectValue("password", "NotBlank", "Mật khẩu không được để trống");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        try {
            userService.createUser(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("user", user);
            model.addAttribute("duplicateError", "Username or email already exists.");
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", user);
            model.addAttribute("duplicateError", e.getMessage());
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String userEditForm(@PathVariable Integer id, Model model) {
        UserAdminDTO existing = userService.getUserById(id);
        model.addAttribute("user", existing);
        model.addAttribute("userInfo", existing);
        model.addAttribute("editMode", true);
        model.addAttribute("userId", id);
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users";
    }

    @PostMapping("/users/{id}/edit")
    public String userUpdate(@PathVariable Integer id,
            @Valid UserAdminDTO user, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("editMode", true);
            model.addAttribute("userId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        try {
            userService.updateUser(id, user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("user", user);
            model.addAttribute("duplicateError", "Username or email already exists.");
            model.addAttribute("editMode", true);
            model.addAttribute("userId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", user);
            model.addAttribute("duplicateError", e.getMessage());
            model.addAttribute("editMode", true);
            model.addAttribute("userId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String userDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        userService.softDeleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User disabled.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/restore")
    public String userRestore(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        userService.restoreUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User restored.");
        return "redirect:/admin/users";
    }
}
