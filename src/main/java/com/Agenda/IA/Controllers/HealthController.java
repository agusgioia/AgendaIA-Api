package com.Agenda.IA.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({"/", "/gethealth", "/health"})
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("Ok");
    }
}
