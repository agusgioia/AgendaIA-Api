package com.Agenda.IA.Services;

import com.Agenda.IA.DTO.IntentResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class InterpreterService {


    private final AIService aiService;

    public IntentResult interpret(String text) {
        text = text.toLowerCase();

        IntentResult r = new IntentResult();

        // 1. INTENT
        if (text.contains("agendar") || text.contains("agenda")) {
            r.setIntent("create_event");
        } else if (text.contains("hoy")) {
            r.setIntent("read_today");
        } else if (text.contains("semana")) {
            r.setIntent("read_week");
        } else {
            return aiService.interpret(text);
        }

        // 2. ENTIDADES
        if (text.contains("mañana")) {
            r.setDate(LocalDate.now().plusDays(1));
        } else if (text.contains("hoy")) {
            r.setDate(LocalDate.now());
        }

        Pattern p = Pattern.compile("(\\d{1,2})(:\\d{2})?");
        Matcher m = p.matcher(text);

        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int min = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 0;
            r.setTime(LocalTime.of(hour, min));
        }

        // 3. VALIDACIÓN CLAVE
        if ("create_event".equals(r.getIntent())) {
            if (r.getDate() == null || r.getTime() == null) {
                return aiService.interpret(text);
            }

            String clean = text
                    .replaceAll("agendar|agenda|mañana|hoy|a las \\d{1,2}(:\\d{2})?", "")
                    .trim();

            r.setTitle(clean);
        }

        return r;
    }
}
