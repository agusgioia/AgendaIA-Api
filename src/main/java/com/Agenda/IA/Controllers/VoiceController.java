package com.Agenda.IA.Controllers;

import com.Agenda.IA.DTO.IntentResult;
import com.Agenda.IA.DTO.VoiceRequest;
import com.Agenda.IA.DTO.VoiceResponse;
import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Services.AgendaService;
import com.Agenda.IA.Services.InterpreterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/voice")
@CrossOrigin
public class VoiceController {

    @Autowired
    private InterpreterService interpreterService;

    @Autowired
    private AgendaService agendaService;

    @PostMapping
    public ResponseEntity<VoiceResponse> voice(@RequestBody VoiceRequest req, @RequestParam String email) {
        try {
            // Pasamos el email para mantener el contexto en el proceso de interpretación
            IntentResult intent = interpreterService.interpret(req.getText(), email);

            if (intent == null || intent.getIntent() == null) {
                return ResponseEntity.ok(new VoiceResponse("Tuve un problema interpretando tu solicitud, ¿podés repetirlo?"));
            }

            String respuesta;
            switch (intent.getIntent()) {
                case "create_event" -> {
                    if (intent.getTime() == null) {
                        return ResponseEntity.ok(new VoiceResponse("Entendí que querés agendar '" + intent.getTitle() + "' el " + intent.getDate() + ", pero me faltó la hora. ¿A qué hora lo agendo?"));
                    }

                    agendaService.createEvent(
                            intent.getTitle(),
                            intent.getDate(),
                            intent.getTime(),
                            email
                    );
                    respuesta = "Evento creado: " + intent.getTitle() + " para el " + intent.getDate() + " a las " + intent.getTime();
                }
                case "read_today" -> {
                    List<Event> events = agendaService.getTodayEvents(email);
                    respuesta = events.isEmpty()
                            ? "No tenés eventos hoy"
                            : "Tenés " + events.size() + " evento(s) hoy: " +
                            events.stream().map(Event::getTitle).reduce((a, b) -> a + ", " + b).orElse("");
                }
                case "read_week" -> {
                    List<Event> events = agendaService.getWeekEvents(email);
                    respuesta = events.isEmpty()
                            ? "No tenés eventos esta semana"
                            : "Tenés " + events.size() + " evento(s) esta semana: " +
                            events.stream().map(Event::getTitle).reduce((a, b) -> a + ", " + b).orElse("");
                }
                default -> respuesta = "No entendí lo que querías hacer, asegurate de decirme la fecha y la hora del evento para poder agendarlo.";
            }

            return ResponseEntity.ok(new VoiceResponse(respuesta));

        } catch (Exception e) {
            System.err.println("=== ERROR EN VOICE CONTROLLER ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body(new VoiceResponse("Hubo un error interno en el servidor: " + e.getMessage()));
        }
    }
}