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

    @Value("${google.gemini.api.key}")
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

        // Estructura específica de Gemini
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                // Forzamos la salida a JSON para evitar que devuelva texto explicativo
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        // Endpoint de Gemini 1.5 Flash
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        String json = extractText(response);
        return parseResult(json);
    }

    private String extractText(Map response) {
        try {
            // Navegación en el JSON de Gemini: candidates -> content -> parts -> text
            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "{}"; // Fallback mínimo
        }
    }

    private IntentResult parseResult(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(json, IntentResult.class);
        } catch (Exception e) {
            IntentResult r = new IntentResult();
            r.setIntent("read_today");
            return r;
        }
    }
}