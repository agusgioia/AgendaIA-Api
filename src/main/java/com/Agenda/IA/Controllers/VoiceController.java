package com.Agenda.IA.Controllers;

import com.Agenda.IA.DTO.IntentResult;
import com.Agenda.IA.DTO.VoiceRequest;
import com.Agenda.IA.DTO.VoiceResponse;
import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Services.AIService;
import com.Agenda.IA.Services.AgendaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/voice")
@CrossOrigin
public class VoiceController {

    @Autowired
    private AIService aiService;

    @Autowired
    private AgendaService agendaService;

    @PostMapping
    public VoiceResponse voice(@RequestBody VoiceRequest req,@RequestParam String email){

        IntentResult intent = aiService.interpret(req.getText());
        String respuesta="";
        if(intent.getIntent().equals("create_event")){
            agendaService.createEvent(
                    intent.getTitle(),
                    intent.getDate(),
                    intent.getTime(),
                    email
            );
            respuesta="evento creado";
        }
        if(intent.getIntent().equals("read_today")){
            List<Event> events = agendaService.getTodayEvents(intent.getDate());
            respuesta="tenés "+events.size()+" eventos hoy";

        }
        return new VoiceResponse(respuesta);
    }
}