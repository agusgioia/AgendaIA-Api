package com.Agenda.IA.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.Agenda.IA.Services.AgendaService;
import com.Agenda.IA.Models.Event;

import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin
public class AgendaController {

    @Autowired
    private AgendaService agendaService;


    @GetMapping("/{id}")
    public List<Event> getUsersEvents(@PathVariable("id")Long id){
        return agendaService.getUsersEvent(id);
    }

    @PostMapping
    public Event saveEvent(@RequestBody Event event,@RequestParam String email){
        return agendaService.createEvent(event.getTitle(),event.getDate(),event.getTime(),email);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> editEvent(@RequestBody Event event, @PathVariable Long id) {
        return agendaService.editEvent(event, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable("id")Long id){
        agendaService.deleteEvent(id);
        return "Evento borrado correctamente";
    }
}
