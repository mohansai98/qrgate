package com.qrgate.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@lombok.RequiredArgsConstructor
public class IndexController {

    private final com.qrgate.core.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam String fullName) {
        if (userRepository.existsByUsername(username)) {
            return "redirect:/register?error=username_exists";
        }
        com.qrgate.core.model.User user = new com.qrgate.core.model.User(username, passwordEncoder.encode(password), email, fullName);
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}
