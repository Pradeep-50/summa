package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.OperatorDTO;
import com.nexbus.frontend.service.OperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class OperatorController {
    private final OperatorService operatorService;

    @Autowired
    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping("/api/operators")
    public String showOperators(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:Operator/index1";
        }
        model.addAttribute("pageTitle", "Operators");
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        try {
            model.addAttribute("operators", operatorService.getAllOperators());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch operators: " + e.getMessage());
        }
        return "Operator/Operators";
    }

    @PostMapping("/api/operators")
    public String addOperator(@ModelAttribute OperatorDTO operatorDTO, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Operator/index1";
        }
        try {
            operatorService.createOperator(operatorDTO);
            return "redirect:/api/operators";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add operator: " + e.getMessage());
            return "Operator/Operators";
        }
    }

    @PostMapping("/api/operators/{id}/delete")
    public String deleteOperator(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Operators/index1";
        }
        try {
            operatorService.deleteOperator(id);
            return "redirect:/api/operators";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete operator: " + e.getMessage());
            return "Operators/Operators";
        }
    }
}