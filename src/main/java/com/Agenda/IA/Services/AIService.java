package com.Agenda.IA.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.Agenda.IA.DTO.IntentResult;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    private static final String PROMPT = """
            Sos un asistente de agenda personal. El usuario habla en español.
            Analizá el siguiente mensaje y extraé la información relevante.
            
            Intenciones posibles:
            - create_event: el usuario quiere agendar algo
            - read_today: el usuario quiere saber sus eventos de hoy
            - read_week: el usuario quiere saber sus eventos de la semana
            
            Fecha de hoy: %s
            Mensaje del usuario: "%s"
            
            Respondé ÚNICAMENTE con un JSON con esta estructura, sin texto adicional:
            {
              "intent": "create_event",
              "title": "título del evento",
              "date": "2025-03-24",
              "time": "10:30"
            }
            
            Si el intent es read_today o read_week, title/date/time pueden ser null.
            La fecha debe estar en formato yyyy-MM-dd y la hora en HH:mm.
            """;

    public IntentResult interpret(String text) {
        String prompt = PROMPT.formatted(LocalDate.now(), text);

        Map<String, Object> body = Map.of(
                "model", "claude-sonnet-4-20250514",
                "max_tokens", 256,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map response = restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        String json = extractText(response);
        return parseResult(json);
    }

    private String extractText(Map response) {
        List<Map> content = (List<Map>) response.get("content");
        return (String) content.get(0).get("text");
    }

    private IntentResult parseResult(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(json, IntentResult.class);
        } catch (Exception e) {
            // fallback si el parseo falla
            IntentResult r = new IntentResult();
            r.setIntent("read_today");
            return r;
        }
    }
}