package com.Agenda.IA.Services;

import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Models.User;
import com.Agenda.IA.Repositories.EventRepository;
import com.Agenda.IA.Repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AgendaService {

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    public Event createEvent(String title, LocalDate date, LocalTime time,String email){

        User user = userRepository.findByEmail(email);

        Event e = new Event();
        e.setTitle(title);
        e.setDate(date);
        e.setTime(time);
        e.setUser(user);

        return eventRepository.save(e);
    }


    public User findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public List<Event> getTodayEvents(LocalDate date){
        return eventRepository.findByDate(date);
    }

    public List<Event> getUsersEvent(Long id){
        return eventRepository.findByUserId(id);
    }

    public void deleteEvent(Long id){
        eventRepository.deleteById(id);
    }
}
