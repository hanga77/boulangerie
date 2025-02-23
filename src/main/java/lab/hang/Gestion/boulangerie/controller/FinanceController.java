package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.TransactionDTO;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/finances")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public String getFinances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<TransactionDTO> transactions = financeService.getAllTransactions(PageRequest.of(page, size));
        double revenusTotaux = financeService.calculerRevenusTotaux();
        double depensesTotales = financeService.calculerDepensesTotales();
        double profit = financeService.calculerProfit();

        model.addAttribute("transactions", transactions.getContent());
        model.addAttribute("revenusTotaux", revenusTotaux);
        model.addAttribute("depensesTotales", depensesTotales);
        model.addAttribute("profit", profit);
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", transactions.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);

        return "finances/finance";
    }

    @GetMapping("/transactions")
    public String getTransactions(Model model) {
        List<TransactionDTO> transactions = financeService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "finances/transactions";
    }

    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable Long id, Model model) {
        TransactionDTO transaction = financeService.getTransactionById(id);
        model.addAttribute("transaction", transaction);
        return "finances/transaction-details";
    }
}