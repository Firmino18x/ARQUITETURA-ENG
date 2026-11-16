package br.com.mvc.controller;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.mvc.model.entity.Sale;
import br.com.mvc.model.enums.ProductStatus;
import br.com.mvc.model.service.ProductService;
import br.com.mvc.model.service.SaleCheckoutService;
import br.com.mvc.model.service.SaleService;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final SaleCheckoutService saleCheckoutService;
    private final ProductService productService;
    private final String hubUrl;

    public SaleController(SaleService saleService, SaleCheckoutService saleCheckoutService,
            ProductService productService, @Value("${hub.url}") String hubUrl) {
        this.saleService = saleService;
        this.saleCheckoutService = saleCheckoutService;
        this.productService = productService;
        this.hubUrl = hubUrl;
    }

    @GetMapping
    public String read(Model model) {
        model.addAttribute("sales", saleService.read());
        model.addAttribute("sale", Sale.builder().build());
        return "sales";
    }

    @PostMapping
    public String create(@ModelAttribute Sale sale) {
        Sale created = saleService.create(sale);
        return "redirect:/sales/" + created.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable BigInteger id, Model model) {
        model.addAttribute("sale", saleService.findById(id));
        model.addAttribute("products", productService.read().stream()
                .filter(p -> p.getStatus() == ProductStatus.DISPONIVEL)
                .toList());
        model.addAttribute("hubUrl", hubUrl);
        return "sale";
    }

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable BigInteger id,
            @RequestParam(required = false) BigInteger productId,
            @RequestParam(required = false) Integer quantity) {
        saleService.addItem(id, productId, quantity);
        return "redirect:/sales/" + id;
    }

    @PostMapping("/{id}/items/{itemId}/remove")
    public String removeItem(@PathVariable BigInteger id, @PathVariable BigInteger itemId) {
        saleService.removeItem(id, itemId);
        return "redirect:/sales/" + id;
    }

    /** Fecha o pedido: baixa o estoque, envia ao hub e guarda o ID da fatura. */
    @PostMapping("/{id}/close")
    public String close(@PathVariable BigInteger id) {
        saleCheckoutService.close(id);
        return "redirect:/sales/" + id;
    }

    @PostMapping("/{id}/retry-invoice")
    public String retryInvoice(@PathVariable BigInteger id) {
        saleCheckoutService.retryInvoice(id);
        return "redirect:/sales/" + id;
    }
}
