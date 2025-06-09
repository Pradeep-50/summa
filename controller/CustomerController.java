package com.nexbus.frontend.controller;

import com.nexbus.frontend.dto.CustomerDTO;
import com.nexbus.frontend.dto.BookingPassengerDTO;
import com.nexbus.frontend.dto.PaymentDTO;
import com.nexbus.frontend.dto.SearchDTO;
import com.nexbus.frontend.dto.TicketDTO;
import com.nexbus.frontend.service.BusService;
import com.nexbus.frontend.service.CustomerService;
import com.nexbus.frontend.service.PaymentService;
import com.nexbus.frontend.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final TicketService ticketService;
    private final BusService busService;
    private final PaymentService paymentService;

    public CustomerController(CustomerService customerService, TicketService ticketService, 
                            BusService busService, PaymentService paymentService) {
        this.customerService = customerService;
        this.ticketService = ticketService;
        this.busService = busService;
        this.paymentService = paymentService;
    }

    @GetMapping({"/", "/index"})
    public String showIndex(Model model, HttpSession session) {
        model.addAttribute("customerName", session.getAttribute("customerName"));
        return "customer/index";
    }

    @GetMapping("/login")
    public String showLogin(Model model, HttpSession session) {
        model.addAttribute("error", session.getAttribute("error"));
        session.removeAttribute("error");
        return "customer/index"; // Uses index.html with OTP popup
    }

    @PostMapping("/otp/request")
    @ResponseBody
    public String requestOTP(@RequestParam("phoneNumber") String phoneNumber, HttpSession session) {
        try {
            // Simulate OTP generation (replace with actual service call)
            String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
            session.setAttribute("otp", otp);
            session.setAttribute("phoneNumber", phoneNumber);
            return "{\"status\":\"success\",\"otp\":\"" + otp + "\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"Failed to send OTP\"}";
        }
    }

    @PostMapping("/otp/verify")
    @ResponseBody
    public String verifyOTP(@RequestParam("otp") String inputOTP, HttpSession session) {
        String storedOTP = (String) session.getAttribute("otp");
        String phoneNumber = (String) session.getAttribute("phoneNumber");
        if (storedOTP != null && storedOTP.equals(inputOTP)) {
            // Simulate customer authentication
            session.setAttribute("authToken", "dummy-token-" + phoneNumber);
            session.setAttribute("customerName", "Customer"); // Replace with actual name from DB
            session.setAttribute("customerRole", "ROLE_CUSTOMER");
            return "{\"status\":\"success\"}";
        }
        return "{\"status\":\"error\",\"message\":\"Invalid OTP\"}";
    }

    @GetMapping("/search")
    public String showSearch(Model model, HttpSession session) {
        return authenticateAndRender("customer/availabilty_nd_booking", model, session);
    }

    @PostMapping("/search")
    public String handleSearch(@ModelAttribute SearchDTO searchDTO, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            List<Object> buses = busService.searchBuses(searchDTO.getFrom(), searchDTO.getTo(), searchDTO.getDate());
            model.addAttribute("buses", buses);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return "customer/availabilty_nd_booking";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to search buses: " + e.getMessage());
            return "customer/index";
        }
    }

    @GetMapping("/passenger")
    public String showPassengerForm(Model model, HttpSession session) {
        return authenticateAndRender("customer/passenger", model, session);
    }

    @PostMapping("/passenger")
    public String submitPassengerDetails(@ModelAttribute BookingPassengerDTO passengerDTO, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            ticketService.savePassengerDetails(authToken, passengerDTO);
            return "redirect:/customer/payment";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save passenger details: " + e.getMessage());
            return "customer/passenger";
        }
    }

    @GetMapping("/payment")
    public String showPayment(Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            TicketDTO ticket = ticketService.getPendingTicket(authToken);
            model.addAttribute("ticket", ticket);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return "customer/payment";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payment details: " + e.getMessage());
            return "customer/payment";
        }
    }

    @PostMapping("/payment")
    public String processPayment(@ModelAttribute PaymentDTO paymentDTO, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            paymentService.processPayment(authToken, paymentDTO);
            return "redirect:/customer/my-trips";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process payment: " + e.getMessage());
            return "customer/payment";
        }
    }

    @GetMapping("/my-profile")
    public String showProfile(Model model, HttpSession session) {
        String viewName = "customer/myProfile";
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            CustomerDTO customer = customerService.getCustomerProfile(authToken);
            model.addAttribute("customer", customer);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return viewName;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return viewName;
        }
    }

    @PostMapping("/my-profile")
    public String updateProfile(@ModelAttribute CustomerDTO customerDTO, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            customerService.updateCustomerProfile(authToken, customerDTO);
            session.setAttribute("customerName", customerDTO.getFirstName() + " " + customerDTO.getLastName());
            return "redirect:/customer/my-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
            return "customer/myProfile";
        }
    }

    @GetMapping("/my-trips")
    public String showTrips(Model model, HttpSession session) {
        String viewName = "customer/myTrip";
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            List<TicketDTO> tickets = ticketService.getCustomerTickets(authToken);
            model.addAttribute("tickets", tickets);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return viewName;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load trips: " + e.getMessage());
            return viewName;
        }
    }

    @GetMapping("/cancel-ticket")
    public String showCancelTicket(Model model, HttpSession session) {
        String viewName = "customer/cancelTicket";
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            List<TicketDTO> tickets = ticketService.getCustomerTickets(authToken);
            model.addAttribute("tickets", tickets);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return viewName;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load tickets for cancellation: " + e.getMessage());
            return viewName;
        }
    }

    @PostMapping("/cancel-ticket")
    public String cancelTicket(@RequestParam("ticketId") String ticketId, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            ticketService.cancelTicket(authToken, ticketId);
            return "redirect:/customer/my-trips";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel ticket: " + e.getMessage());
            return "customer/cancelTicket";
        }
    }

    @GetMapping("/show-ticket")
    public String showTicket(Model model, HttpSession session, @RequestParam("ticketId") String ticketId) {
        String viewName = "customer/showTicket";
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        try {
            TicketDTO ticket = ticketService.getTicketDetails(authToken, ticketId);
            model.addAttribute("ticket", ticket);
            model.addAttribute("customerName", session.getAttribute("customerName"));
            model.addAttribute("customerRole", session.getAttribute("customerRole"));
            model.addAttribute("authToken", authToken);
            return viewName;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load ticket details: " + e.getMessage());
            return viewName;
        }
    }

    private String authenticateAndRender(String viewName, Model model, HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        if (authToken == null) {
            return "redirect:/customer/index";
        }
        model.addAttribute("customerName", session.getAttribute("customerName"));
        model.addAttribute("customerRole", session.getAttribute("customerRole"));
        model.addAttribute("authToken", authToken);
        return viewName;
    }
}