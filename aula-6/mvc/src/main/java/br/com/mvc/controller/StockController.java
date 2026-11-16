package br.com.mvc.controller;

import java.math.BigInteger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.mvc.model.enums.MovementType;
import br.com.mvc.model.service.ProductService;
import br.com.mvc.model.service.StockService;

@Controller
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final ProductService productService;

    public StockController(StockService stockService, ProductService productService) {
        this.stockService = stockService;
        this.productService = productService;
    }

    @GetMapping
    public String read(Model model) {
        model.addAttribute("products", productService.read());
        model.addAttribute("movements", stockService.readMovements());
        model.addAttribute("types", MovementType.values());
        return "stock";
    }

    @PostMapping
    public String register(@RequestParam(required = false) BigInteger productId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) Integer quantity) {
        stockService.register(productId, type, quantity);
        return "redirect:/stock";
    }
}
