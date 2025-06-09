package com.nexbus.frontend.controller;

import com.nexbus.frontend.service.ApiClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ApiClientService apiClientService;

    @GetMapping
    public String showDashboard(HttpSession session, Model model) {
        // Authentication check
        String authToken = (String) session.getAttribute("authToken");
        String adminName = (String) session.getAttribute("adminName");
        String adminRole = (String) session.getAttribute("adminRole");

        if (authToken == null || adminName == null || adminRole == null) {
            return "redirect:/Admin/Login";
        }

        model.addAttribute("adminName", adminName);
        model.addAttribute("adminRole", adminRole);
        model.addAttribute("authToken", authToken);
        model.addAttribute("pageTitle", "Dashboard");

        // Dashboard data
        try {
            model.addAttribute("metrics", getDashboardMetrics(authToken));
            model.addAttribute("recentBookings", getRecentBookings(authToken));
            model.addAttribute("systemAlerts", getSystemAlerts(authToken));
            model.addAttribute("recentActivity", getRecentActivity(authToken));
            model.addAttribute("notifications", getNotifications(authToken));
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
        }
        model.addAttribute("sidebarHidden", false);

        return "Admin/dashboard";
    }

    private Map<String, Object> getDashboardMetrics(String authToken) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            // Fetch metrics from backend
            Map response = apiClientService.get("/api/dashboard/metrics", authToken, Map.class);
            metrics.put("totalPassengers", response.getOrDefault("totalPassengers", 0));
            metrics.put("totalBookings", response.getOrDefault("totalBookings", 0));
            metrics.put("totalRoutes", response.getOrDefault("totalRoutes", 0));
            metrics.put("totalRevenue", response.getOrDefault("totalRevenue", 0));
        } catch (Exception e) {
            // Fallback to defaults
            metrics.put("totalPassengers", 0);
            metrics.put("totalBookings", 0);
            metrics.put("totalRoutes", 0);
            metrics.put("totalRevenue", 0);
        }
        return metrics;
    }

    private List<Map<String, Object>> getRecentBookings(String authToken) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        try {
            List<Map<String, Object>> response = apiClientService.get("/bookings/recent?limit=5", authToken, List.class);
            bookings.addAll(response);
        } catch (Exception e) {
            // Empty list on error
        }
        return bookings;
    }

    private List<Map<String, String>> getSystemAlerts(String authToken) {
        List<Map<String, String>> alerts = new ArrayList<>();
        try {
            // Fetch system alerts
            List<Map<String, String>> response = apiClientService.get("/api/alerts?limit=5", authToken, List.class);
            alerts.addAll(response);
        } catch (Exception e) {
            Map<String, String> alert1 = new HashMap<>();
            alert1.put("message", "Bus #B101 maintenance due");
            alert1.put("timestamp", LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            alerts.add(alert1);

            Map<String, String> alert2 = new HashMap<>();
            alert2.put("message", "New operator registered");
            alert2.put("timestamp", LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            alerts.add(alert2);
        }
        return alerts;
    }

    private List<Map<String, String>> getRecentActivity(String authToken) {
        List<Map<String, String>> activities = new ArrayList<>();
        try {
            // Fetch recent activity
            List<Map<String, String>> response = apiClientService.get("/api/audit/recent?limit=5", authToken, List.class);
            activities.addAll(response);
        } catch (Exception e) {
            // Fallback to static activity
            Map<String, String> activity = new HashMap<>();
            activity.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            activity.put("details", "New operator registered");
            activities.add(activity);
        }
        return activities;
    }

    private List<Map<String, String>> getNotifications(String authToken) {
        List<Map<String, String>> notifications = new ArrayList<>();
        try {
            // Fetch notifications
            List<Map<String, String>> response = apiClientService.get("/api/notifications?limit=5", authToken, List.class);
            notifications.addAll(response);
        } catch (Exception e) {
            // Fallback to static notifications
            Map<String, String> notification1 = new HashMap<>();
            notification1.put("message", "System: New operator registered");
            notification1.put("timestamp", LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            notifications.add(notification1);

            Map<String, String> notification2 = new HashMap<>();
            notification2.put("message", "Alert: Bus #B101 maintenance due");
            notification2.put("timestamp", LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            notifications.add(notification2);
        }
        return notifications;
    }
}