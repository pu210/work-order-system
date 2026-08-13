package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.dto.RegisterRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.RegisterResponseDTO;
import com.eeit219.work_order_system.modules.a.service.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

}
