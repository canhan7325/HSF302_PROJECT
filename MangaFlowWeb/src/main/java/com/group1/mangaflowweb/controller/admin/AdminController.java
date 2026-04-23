package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.admin.RevenueDataPointDTO;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.service.AdminDashboardService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.TransactionsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final TransactionsService transactionService;
    private final ComicService comicService;

    public AdminController(AdminDashboardService dashboardService,
                           TransactionsService transactionService,
                           ComicService comicService) {
        this.dashboardService    = dashboardService;
        this.transactionService = transactionService;
        this.comicService        = comicService;
    }

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "year") String period, Model model) {
        model.addAttribute("stats",     dashboardService.getDashboardStats());
        model.addAttribute("topComics", dashboardService.getComicsSortedByViewCount());
        model.addAttribute("revenueWeek",  dashboardService.getRevenueByPeriod("week"));
        model.addAttribute("revenueMonth", dashboardService.getRevenueByPeriod("month"));
        model.addAttribute("revenueYear",  dashboardService.getRevenueByPeriod("year"));
        model.addAttribute("revenueAll",   dashboardService.getRevenueByPeriod("all"));
        model.addAttribute("activePeriod", period);
        model.addAttribute("username",  SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/revenue")
    @ResponseBody
    public List<RevenueDataPointDTO> getRevenue(
            @RequestParam(defaultValue = "month") String period) {
        return dashboardService.getRevenueByPeriod(period);
    }

    // ── Revenue page ──────────────────────────────────────────────────────────

    @GetMapping("/revenue")
    public String revenuePage(Model model) {
        model.addAttribute("totalRevenue",         transactionService.getTotalRevenue());
        model.addAttribute("totalTransactions",    transactionService.getTotalTransactionCount());
        model.addAttribute("activeTransactions",   transactionService.getActiveTransactions());
        model.addAttribute("revenueBySubscription",transactionService.getRevenueBySubscription());
        model.addAttribute("transactions",         transactionService.getAllTransactions());
        model.addAttribute("username",             SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/revenue";
    }

    // ── View tracking page ────────────────────────────────────────────────────

    @GetMapping("/view-tracking")
    public String viewTrackingPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String filterBy,
            Model model) {

        var comics = (search != null && !search.isBlank())
                ? comicService.searchComicsByName(search)
                : comicService.getComicsWithFilter(sortBy, sortOrder, filterBy);

        model.addAttribute("comics",      comics);
        model.addAttribute("searchQuery", search);
        model.addAttribute("sortBy",      sortBy);
        model.addAttribute("sortOrder",   sortOrder);
        model.addAttribute("filterBy",    filterBy);
        model.addAttribute("username",    SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/view-tracking";
    }
}

