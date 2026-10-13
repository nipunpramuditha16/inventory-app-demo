package com.abcretail.inventory.controller.web;

import com.abcretail.inventory.exception.DuplicateSkuException;
import com.abcretail.inventory.model.Product;
import com.abcretail.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Server-rendered web page (Thymeleaf) for browsing and managing the inventory. */
@Controller
public class HomeController {

    private final InventoryService service;

    public HomeController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        populate(model);
        if (!model.containsAttribute("newProduct")) {
            model.addAttribute("newProduct", new Product());
        }
        return "index";
    }

    @PostMapping("/products")
    public String add(@Valid @ModelAttribute("newProduct") Product product,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirect) {
        if (!bindingResult.hasErrors()) {
            try {
                service.create(product);
                redirect.addFlashAttribute("message", "Product added: " + product.getName());
                return "redirect:/";
            } catch (DuplicateSkuException ex) {
                bindingResult.rejectValue("sku", "duplicate", ex.getMessage());
            }
        }
        populate(model);
        return "index";
    }

    @PostMapping("/products/{id}/stock")
    public String adjust(@PathVariable Long id, @RequestParam int delta, RedirectAttributes redirect) {
        try {
            service.adjustStock(id, delta);
            redirect.addFlashAttribute("message", "Stock updated");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("message", "Product deleted");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }

    private void populate(Model model) {
        model.addAttribute("products", service.findAll());
        model.addAttribute("summary", service.summarize());
    }
}
