package com.example.devlogqna;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class DevlogQnaApplicationTests {

    public static class PasswordHashGenerator {
        public static void main(String[] args) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            //String hash = encoder.encode("admin");
            //String hash = encoder.encode("test");
            String hash = encoder.encode("potato");
            System.out.println("Generated hash: " + hash);
        }
    }
}
