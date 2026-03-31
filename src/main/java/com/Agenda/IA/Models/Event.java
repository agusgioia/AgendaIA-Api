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

    @Column
    private String title;

    @Column
    private LocalDate date;

    @Column
    private LocalTime time;

    @Column
    private Integer reminderMinutes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
