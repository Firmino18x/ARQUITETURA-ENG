package br.com.mvc.controller;

import java.math.BigInteger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import br.com.mvc.model.entity.Product;
import br.com.mvc.model.service.ProductService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public String read(Model model){
        model.addAttribute("products", productService.read());
        model.addAttribute("product", Product.builder().build());
        return "products";
    }

    @PostMapping
    public String create(@ModelAttribute Product product){
        productService.create(product);
        return "redirect:/products";
    }

    @PostMapping("/{id}/discontinue")
    public String discontinue(@PathVariable BigInteger id, HttpSession session) {
        productService.discontinue(id, SessionRole.get(session));
        return "redirect:/products";
    }
}
