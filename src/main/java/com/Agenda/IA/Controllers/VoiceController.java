package com.Agenda.IA.Controllers;

import com.Agenda.IA.DTO.IntentResult;
import com.Agenda.IA.DTO.VoiceRequest;
import com.Agenda.IA.DTO.VoiceResponse;
import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Services.AgendaService;
import com.Agenda.IA.Services.InterpreterService;
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
    public VoiceResponse voice(@RequestBody VoiceRequest req, @RequestParam String email) {
        IntentResult intent = interpreterService.interpret(req.getText());
        String respuesta;
        switch (intent.getIntent()) {
            case "create_event" -> {
                agendaService.createEvent(
                        intent.getTitle(),
                        intent.getDate(),
                        intent.getTime(),
                        email
                );
                respuesta = "Evento creado: " + intent.getTitle() + " para el " + intent.getDate();
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
            default -> respuesta = "No entendí lo que querías hacer";
        }

        return new VoiceResponse(respuesta);
    }
}