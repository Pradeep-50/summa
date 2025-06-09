package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.RouteDTO;
import com.nexbus.frontend.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class RouteController {
    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/api/routes")
    public String showRoutes(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/Admin/Login";
        }
        model.addAttribute("pageTitle", "Routes");
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        try {
            model.addAttribute("routes", routeService.getAllRoutes());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch routes: " + e.getMessage());
        }
        return "Operator/OpertorRoutes";
    }

    @PostMapping("/api/routes")
    public String addRoute(@ModelAttribute RouteDTO routeDTO, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/login";
        }
        try {
            routeService.createRoute(routeDTO);
            return "redirect:/api/routes";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add route: " + e.getMessage());
            return "routes";
        }
    }

    @PostMapping("/api/routes/{id}/delete")
    public String deleteRoute(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/login";
        }
        try {
            routeService.deleteRoute(id);
            return "redirect:/api/routes";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete route: " + e.getMessage());
            return "routes";
        }
    }
}