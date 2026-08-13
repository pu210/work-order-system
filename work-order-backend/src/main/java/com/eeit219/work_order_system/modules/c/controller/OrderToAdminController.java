package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/pages/admin")
public class OrderToAdminController {
    @GetMapping("/orders")
    public String getMethodName(@RequestParam String param) {
        
        return new String();
    }
    
}
