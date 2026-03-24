package com.Agenda.IA.Services;

import org.springframework.stereotype.Service;
import com.Agenda.IA.DTO.IntentResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {


    private LocalTime extractTime(String text){

        // formato: 10:30
        Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher matcher = pattern.matcher(text);

        if(matcher.find()){
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));

            return LocalTime.of(hour, minute);
        }

        // formato: "a las 10"
        Pattern pattern2 = Pattern.compile("a las (\\d{1,2})");
        Matcher matcher2 = pattern2.matcher(text);

        if(matcher2.find()){
            int hour = Integer.parseInt(matcher2.group(1));
            return LocalTime.of(hour, 0);
        }

        return LocalTime.of(9, 0); // fallback
    }

    private LocalDate extractDate(String text){

        if(text.contains("hoy")){
            return LocalDate.now();
        }

        if(text.contains("mañana")){
            return LocalDate.now().plusDays(1);
        }

        // formato: 17/03 o 17-03
        Pattern pattern = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})");
        Matcher matcher = pattern.matcher(text);

        if(matcher.find()){
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));

            return LocalDate.of(LocalDate.now().getYear(), month, day);
        }

        return LocalDate.now(); // fallback
    }

    public IntentResult interpret(String text){

        IntentResult r = new IntentResult();
        String lower = text.toLowerCase();

        if(lower.contains("agenda")){

            r.setIntent("create_event");
            String cleanTitle = text
                    .replaceAll("(?i)\\bagend[a-záéíóú]*\\b", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            r.setTitle(cleanTitle);

            // ---- FECHA ----
            LocalDate date = extractDate(lower);
            r.setDate(date);

            // ---- HORA ----
            LocalTime time = extractTime(lower);
            r.setTime(time);

        }else{
            r.setIntent("read_today");
        }

        return r;
    }

}