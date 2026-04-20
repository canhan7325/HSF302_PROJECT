package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserService;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final ComicService comicService;
    private final UserService userService;
    private final TransactionService transactionService;
    
    public AdminController(ComicService comicService, UserService userService, TransactionService transactionService) {
        this.comicService = comicService;
        this.userService = userService;
        this.transactionService = transactionService;
    }
    
    @GetMapping("")
    public String dashboard(Model model) {
        // Get statistics
        long totalComics = comicService.getTotalComics();
        long totalUsers = userService.getTotalUsers();
        
        // Add to model
        model.addAttribute("totalComics", totalComics);
        model.addAttribute("totalUsers", totalUsers);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/view-tracking")
    public String viewTracking(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(value = "filterBy", required = false) String filterBy,
            Model model) {
        
        List<Comics> comics;
        
        if (search != null && !search.trim().isEmpty()) {
            comics = comicService.searchComicsByName(search);
            model.addAttribute("searchQuery", search);
        } else {
            comics = comicService.getComicsWithFilter(sortBy, sortOrder, filterBy);
        }
        
        // Apply sorting and filtering if searched results need to be sorted
        if (search != null && !search.trim().isEmpty() && (sortBy != null || filterBy != null)) {
            comics = comicService.getComicsWithFilter(sortBy, sortOrder, filterBy);
            // Re-filter by search
            comics = comics.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(search.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("comics", comics);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("filterBy", filterBy);
        
        return "admin/view-tracking";
    }
    
    @GetMapping("/revenue")
    public String revenue(Model model) {
        // Get revenue statistics
        model.addAttribute("totalRevenue", transactionService.getTotalRevenue());
        model.addAttribute("revenueBySubscription", transactionService.getRevenueBySubscription());
        model.addAttribute("transactions", transactionService.getAllTransactions());
        model.addAttribute("activeTransactions", transactionService.getActiveTransactions());
        model.addAttribute("totalTransactions", transactionService.getTotalTransactionCount());
        
        return "admin/revenue";
    }
}


