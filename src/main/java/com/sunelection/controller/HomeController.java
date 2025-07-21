package com.sunelection.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home.html";
    }
    
    @GetMapping("/api")
    public String redirectApiToHome() {
        return "redirect:/api/home.html";
    }
}
