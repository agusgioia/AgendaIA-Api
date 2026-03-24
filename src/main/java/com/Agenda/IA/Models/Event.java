package com.Agenda.IA.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Events")
@Data
public class Event {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private LocalDate date;
    private LocalTime time;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
