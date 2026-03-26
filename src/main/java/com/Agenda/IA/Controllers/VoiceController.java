package com.Agenda.IA.Controllers;

import com.Agenda.IA.DTO.IntentResult;
import com.Agenda.IA.DTO.VoiceRequest;
import com.Agenda.IA.DTO.VoiceResponse;
import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Services.AgendaService;
import com.Agenda.IA.Services.InterpreterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
            
                String dia = LocalDate.now()
                        .getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            
                respuesta = events.isEmpty()
                        ? "No tenés eventos hoy (" + dia + ")"
                        : "Hoy es " + dia + " y tenés " + events.size() + " evento(s): " +
                        events.stream()
                                .map(e -> {
                                    String hora = e.getTime() != null
                                            ? e.getTime().toString().substring(0,5)
                                            : "sin hora";
                                    return e.getTitle() + " a las " + hora;
                                })
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("");
            }
            case "read_week" -> {
                List<Event> events = agendaService.getWeekEvents(email);
            
                if (events.isEmpty()) {
                    respuesta = "No tenés eventos esta semana";
                    break;
                }
            
                Map<LocalDate, List<Event>> grouped = events.stream()
                        .collect(Collectors.groupingBy(Event::getDate));
            
                StringBuilder sb = new StringBuilder("Esta semana tenés: ");
            
                grouped.forEach((date, evs) -> {
                    String dia = date.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            
                    sb.append("El ").append(dia).append(" tenés: ");
            
                    String eventos = evs.stream()
                            .map(e -> e.getTitle())
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
            
                    sb.append(eventos).append(". ");
                });
            
                respuesta = sb.toString();
            }
            default -> respuesta = "No entendí lo que querías hacer";
        }

        return new VoiceResponse(respuesta);
    }
}
