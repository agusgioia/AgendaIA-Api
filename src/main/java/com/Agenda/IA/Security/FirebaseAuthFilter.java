package com.Agenda.IA.Security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of("/health", "/users");
    private static final Object LOCK = new Object();
    private static volatile boolean firebaseInitialized = false;

    private void initFirebase() {
        if (firebaseInitialized) return;
        synchronized (LOCK) {
            if (firebaseInitialized) return;
            try {
                if (FirebaseApp.getApps().isEmpty()) {
                    String serviceAccount = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
                    if (serviceAccount == null || serviceAccount.isEmpty()) {
                        throw new RuntimeException("Falta la variable de entorno FIREBASE_SERVICE_ACCOUNT_JSON");
                    }
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(
                                    new ByteArrayInputStream(serviceAccount.getBytes(StandardCharsets.UTF_8))
                            ))
                            .setProjectId("ecommerce-84e80") // Debe coincidir con el aud del token
                            .build();
                    FirebaseApp.initializeApp(options);
                }
                firebaseInitialized = true;
                System.out.println("Firebase inicializado correctamente en el filtro.");
            } catch (Exception e) {
                System.err.println("Error al inicializar Firebase en el filtro: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("No se pudo inicializar Firebase", e);
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Asegurar que Firebase está listo
        initFirebase();

        // Manejo de OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token requerido");
            return;
        }

        String token = authHeader.substring(7);
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            request.setAttribute("firebaseUid", decoded.getUid());
            request.setAttribute("firebaseEmail", decoded.getEmail());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("ERROR al verificar token: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o expirado: " + e.getMessage());
        }
    }
}