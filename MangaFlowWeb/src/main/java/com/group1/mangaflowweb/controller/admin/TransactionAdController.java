package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionAdController {
    
    private final TransactionService transactionService;
    
    public TransactionAdController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @GetMapping("/{id}")
    public String viewTransactionDetail(@PathVariable Integer id, Model model) {
        Transactions transaction = transactionService.getTransactionById(id);
        
        if (transaction == null) {
            return "redirect:/admin/revenue";
        }
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/transactions/detail";
    }
}

