package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.PassengerDTO;
import com.nexbus.frontend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class PassengerController {
    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping("/api/passengers")
    public String showPassengers(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/Admin/Login";
        }
        model.addAttribute("pageTitle", "Passengers");
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        try {
            model.addAttribute("passengers", passengerService.getAllPassengers());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch passengers: " + e.getMessage());
        }
        return "Admin/Passengers";
    }

    @PostMapping("/api/passengers")
    public String addPassenger(@ModelAttribute PassengerDTO passengerDTO, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            passengerService.createPassenger(passengerDTO);
            return "redirect:/api/passengers";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add passenger: " + e.getMessage());
            return "Admin/Passengers";
        }
    }

    @PostMapping("/api/passengers/{id}/delete")
    public String deletePassenger(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            passengerService.deletePassenger(id);
            return "redirect:/api/passengers";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete passenger: " + e.getMessage());
            return "Admin/Passengers";
        }
    }
}