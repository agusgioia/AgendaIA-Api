package com.Agenda.IA.DTO;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class IntentResult {

    private String intent;

    private String title;

    private LocalDate date;

    private LocalTime time;

}