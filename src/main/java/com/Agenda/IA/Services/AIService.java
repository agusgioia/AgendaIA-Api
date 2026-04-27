package com.Agenda.IA.Services;

import com.Agenda.IA.DTO.IntentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    private static final String PROMPT = """
            Sos un asistente de agenda personal. El usuario habla en español.
            Analizá el siguiente mensaje y extraé la información relevante.
            
            Intenciones posibles:
            - create_event: el usuario quiere agendar algo
            - read_today: el usuario quiere saber sus eventos de hoy
            - read_week: el usuario quiere saber sus eventos de la semana
            
            CONTEXTO PREVIO (Información que ya conocemos del evento):
            %s
            
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

    public IntentResult interpret(String text, String email, PendingIntentService pendingService) {
        // Buscamos si hay algo pendiente para este usuario
        IntentResult anterior = pendingService.get(email);
        String contextoString = (anterior != null) ? anterior.toString() : "No hay contexto previo.";

        String prompt = PROMPT.formatted(contextoString, LocalDate.now(), text);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            Map response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String json = extractText(response);
            IntentResult actual = parseResult(json);

            // Si el evento está incompleto, lo guardamos/actualizamos en el servicio de pendientes
            if (actual != null && "create_event".equals(actual.getIntent())) {
                if (actual.getDate() == null || actual.getTime() == null || actual.getTitle() == null) {
                    pendingService.save(email, actual);
                } else {
                    // Si ya está completo, limpiamos el pendiente
                    pendingService.clear(email);
                }
            } else {
                // Si la intención es leer o cualquier otra cosa, limpiamos contexto
                pendingService.clear(email);
            }

            return actual;

        } catch (Exception e) {
            System.err.println("=== ERROR EN GROQ API ===");
            e.printStackTrace();
            return fallbackIntent();
        }
    }

    private String extractText(Map response) {
        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private IntentResult parseResult(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(json, IntentResult.class);
        } catch (Exception e) {
            System.err.println("Error parseando JSON de Groq: " + json);
            return fallbackIntent();
        }
    }

    private IntentResult fallbackIntent() {
        IntentResult r = new IntentResult();
        r.setIntent("read_today");
        return r;
    }
}