package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.repository.TransactionsRepository;
import com.group1.mangaflowweb.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionsRepository transactionsRepository;
    private final UserContextService userContextService;

    @GetMapping
    public String listTransactions(Model model) {
        var currentUser = userContextService.getCurrentUser();

        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        // Fetch transactions for the authenticated user
        var transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(currentUser.get().getUserId());

        model.addAttribute("transactions", transactions);
        return "clients/transactions";
    }
}


