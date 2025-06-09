package com.nexbus.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController {
    @GetMapping("/api/audit-logs")
    public String showAudit(Model model) {
        model.addAttribute("pageTitle", "Audit Logs");
        // Mock data or fetch from backend if available
        model.addAttribute("auditLogs", "Sample audit logs");
        return "Admin/Audit";
    }
}