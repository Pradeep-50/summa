package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.SupportDTO;
import com.nexbus.frontend.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class SupportController {
    private final SupportService supportService;

    @Autowired
    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/api/support")
    public String showSupport(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/Admin/Login";
        }
        model.addAttribute("pageTitle", "Support Tickets");
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        try {
            model.addAttribute("tickets", supportService.getAllTickets());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch support tickets: " + e.getMessage());
        }
        return "Admin/Support";
    }

    @PostMapping("/api/support")
    public String addTicket(@ModelAttribute SupportDTO supportDTO, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            supportService.createTicket(supportDTO);
            return "redirect:/api/support";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add support ticket: " + e.getMessage());
            return "Admin/Support";
        }
    }

    @PostMapping("/api/support/{id}/delete")
    public String deleteTicket(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            supportService.deleteTicket(id);
            return "redirect:/api/support";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete support ticket: " + e.getMessage());
            return "Admin/Support";
        }
    }
}