package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.request.admin.UserRequest;
import com.group1.mangaflowweb.dto.response.admin.UserAdminResponse;
import com.group1.mangaflowweb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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
        model.addAttribute("user", new UserRequest());
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users";
    }

    @PostMapping("/users/new")
    public String userCreate(@Valid UserRequest user, BindingResult result,
                             Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors()) {
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        try {
            userService.createUser(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("duplicateError", "Username or email already exists.");
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        } catch (IllegalArgumentException e) {
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
        UserAdminResponse existing = userService.getUserById(id);
        model.addAttribute("userInfo", existing);
        model.addAttribute("editMode", true);
        model.addAttribute("userId", id);
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users";
    }

    @PostMapping("/users/{id}/edit")
    public String userUpdate(@PathVariable Integer id,
                             @Valid UserRequest user, BindingResult result,
                             Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("userId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        }
        try {
            userService.updateUser(id, user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("duplicateError", "Username or email already exists.");
            model.addAttribute("editMode", true);
            model.addAttribute("userId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/users";
        } catch (IllegalArgumentException e) {
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
