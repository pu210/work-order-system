package com.eeit219.work_order_system;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashGeneratorTest {

    @Test
    void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";

        String newHash = encoder.encode(password);

        System.out.println("Password hash: " + newHash);
        System.out.println(
                "Matches: "
                        + encoder.matches(password, newHash));
    }
}
