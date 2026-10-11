package com.careerlens.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class CareerLensApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(CareerLensApplication.class, args);
    }

    private static void loadDotEnv() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) {
            envPath = Paths.get("../.env");
        }
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (key.contains("PASSWORD")) {
                            value = value.replaceAll("\\s+", "");
                        }
                        if (!value.isEmpty()) {
                            System.setProperty(key, value);
                            if (key.equals("EMAIL_USERNAME") || key.equals("MAIL_USERNAME")) {
                                System.setProperty("spring.mail.username", value);
                            }
                            if (key.equals("EMAIL_PASSWORD") || key.equals("MAIL_PASSWORD")) {
                                System.setProperty("spring.mail.password", value);
                            }
                            if (key.equals("EMAIL_HOST") || key.equals("MAIL_HOST")) {
                                System.setProperty("spring.mail.host", value);
                            }
                            if (key.equals("EMAIL_PORT") || key.equals("MAIL_PORT")) {
                                System.setProperty("spring.mail.port", value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading .env file: " + e.getMessage());
            }
        }
    }
}
