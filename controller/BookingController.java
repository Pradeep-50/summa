package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.BookingDTO;
import com.nexbus.frontend.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/api/bookings")
    public String showBookings(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/Admin/Login";
        }
        model.addAttribute("pageTitle", "Bookings");
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("authToken", authToken);
        try {
            model.addAttribute("bookings", bookingService.getAllBookings());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch bookings: " + e.getMessage());
        }
        return "Admin/Bookings";
    }

    @PostMapping("/api/bookings")
    public String addBooking(@ModelAttribute BookingDTO bookingDTO, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            bookingService.createBooking(bookingDTO);
            return "redirect:/api/Bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add booking: " + e.getMessage());
            return "Admin/Bookings";
        }
    }

    @PostMapping("/api/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("authToken") == null) {
            return "redirect:/Admin/Login";
        }
        try {
            bookingService.cancelBooking(id);
            return "redirect:/api/bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel booking: " + e.getMessage());
            return "Bookings";
        }
    }
}