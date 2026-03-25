package com.Agenda.IA.Repositories;

import com.Agenda.IA.Models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event,Long> {
    List<Event> findByDate(LocalDate date);
    List<Event> findByUser_Id(Long id);
}
