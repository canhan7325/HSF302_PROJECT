package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.SubcriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private SubcriptionsService subscriptionsService;

    @GetMapping
    public String getPricingPage(Model model) {
        model.addAttribute("subscriptions", subscriptionsService.getAllActiveSubscriptions());
        return "clients/buysubscriptions";
    }
}

