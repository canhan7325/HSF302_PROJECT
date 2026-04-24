package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.admin.RevenueDataPointDTO;
import com.group1.mangaflowweb.service.AdminDashboardService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.TransactionsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final TransactionsService transactionService;
    private final ComicService comicService;

    public AdminController(AdminDashboardService dashboardService,
            TransactionsService transactionService,
            ComicService comicService) {
        this.dashboardService = dashboardService;
        this.transactionService = transactionService;
        this.comicService = comicService;
    }

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "year") String period, Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("topComics", dashboardService.getComicsSortedByViewCount());
        model.addAttribute("revenueWeek", dashboardService.getRevenueByPeriod("week"));
        model.addAttribute("revenueMonth", dashboardService.getRevenueByPeriod("month"));
        model.addAttribute("revenueYear", dashboardService.getRevenueByPeriod("year"));
        model.addAttribute("revenueAll", dashboardService.getRevenueByPeriod("all"));
        model.addAttribute("activePeriod", period);
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
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
    public String revenuePage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subscription,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model) {
        var allTransactions = transactionService.getAllTransactions();
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = status == null ? "" : status.trim();
        String normalizedSubscription = subscription == null ? "" : subscription.trim().toLowerCase(Locale.ROOT);

        var filteredTransactions = allTransactions.stream()
                .filter(t -> t.getPrice() != null && t.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0)
                .filter(t -> {
                    if (normalizedSearch.isBlank()) {
                        return true;
                    }
                    String transactionId = t.getTransactionId() == null ? "" : String.valueOf(t.getTransactionId());
                    String username = t.getUser() != null && t.getUser().getUsername() != null
                            ? t.getUser().getUsername().toLowerCase(Locale.ROOT)
                            : "";
                    String email = t.getUser() != null && t.getUser().getEmail() != null
                            ? t.getUser().getEmail().toLowerCase(Locale.ROOT)
                            : "";
                    return transactionId.contains(normalizedSearch)
                            || username.contains(normalizedSearch)
                            || email.contains(normalizedSearch);
                })
                .filter(t -> normalizedStatus.isBlank()
                        || (t.getStatus() != null && t.getStatus().name().equalsIgnoreCase(normalizedStatus)))
                .filter(t -> normalizedSubscription.isBlank()
                        || (t.getSubscription() != null
                                && t.getSubscription().getName() != null
                                && t.getSubscription().getName().toLowerCase(Locale.ROOT)
                                        .contains(normalizedSubscription)))
                .filter(t -> {
                    if (from == null || t.getCreatedAt() == null) {
                        return true;
                    }
                    return !t.getCreatedAt().toLocalDate().isBefore(from);
                })
                .filter(t -> {
                    if (to == null || t.getCreatedAt() == null) {
                        return true;
                    }
                    return !t.getCreatedAt().toLocalDate().isAfter(to);
                })
                .sorted(Comparator.comparing(
                        transaction -> Objects.requireNonNullElse(transaction.getCreatedAt(),
                                java.time.LocalDateTime.MIN),
                        Comparator.reverseOrder()))
                .toList();

        Set<String> subscriptionNames = allTransactions.stream()
                .map(t -> t.getSubscription() != null ? t.getSubscription().getName() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(java.util.TreeSet::new));

        model.addAttribute("totalRevenue", transactionService.getTotalRevenue());
        model.addAttribute("totalTransactions", transactionService.getTotalTransactionCount());
        model.addAttribute("activeTransactions", transactionService.getActiveTransactions());
        model.addAttribute("revenueBySubscription", transactionService.getRevenueBySubscription());
        model.addAttribute("transactions", filteredTransactions);
        model.addAttribute("subscriptionNames", subscriptionNames);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("subscription", subscription);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/revenue";
    }

    @GetMapping("/revenue/transactions/{id}")
    public String revenueTransactionDetail(@PathVariable Integer id, Model model) {
        var transaction = transactionService.getAllTransactions().stream()
                .filter(t -> t.getTransactionId() != null && t.getTransactionId().equals(id))
                .findFirst()
                .orElse(null);
        if (transaction == null) {
            return "redirect:/admin/revenue";
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/revenue-transaction-detail";
    }

    private LocalDate parseDate(String input) {
        try {
            if (input == null || input.isBlank()) {
                return null;
            }
            return LocalDate.parse(input);
        } catch (Exception e) {
            return null;
        }
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

        model.addAttribute("comics", comics);
        model.addAttribute("searchQuery", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("filterBy", filterBy);
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/view-tracking";
    }
}
