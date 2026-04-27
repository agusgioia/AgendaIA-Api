# 📅 Agenda IA

Aplicación web de agenda personal con asistente de voz impulsado por inteligencia artificial. Permite crear y gestionar eventos hablando en lenguaje natural en español.

---

## 🧠 ¿Qué hace?

- Interpretás comandos de voz como _"agendar reunión mañana a las 10"_ y el sistema crea el evento automáticamente.
- Podés preguntar _"¿qué tengo para hoy?"_ o _"¿qué eventos tengo esta semana?"_ y el asistente te responde en voz alta.
- Los eventos se muestran en un calendario interactivo con vistas diaria, semanal y mensual.
- Podés crear, editar y eliminar eventos también desde el calendario directamente.
- Soporta recordatorios push vía OneSignal (10 min, 30 min, 1 hora o 1 día antes).
- La autenticación es gestionada por Firebase (email/password).

---

## 🛠 Stack

### Frontend

| Tecnología        | Uso                                   |
| ----------------- | ------------------------------------- |
| React 19 + Vite 8 | Framework y bundler                   |
| TailwindCSS 3     | Estilos                               |
| PrimeReact 10     | Componentes UI (Dialog, Button, etc.) |
| FullCalendar 6    | Calendario interactivo                |
| Firebase 12       | Autenticación (email/password)        |
| Axios             | Llamadas HTTP a la API                |
| React Router 7    | Navegación y rutas protegidas         |
| OneSignal         | Push notifications                    |

### Backend

| Tecnología      | Uso                              |
| --------------- | -------------------------------- |
| Spring Boot 4   | Framework principal              |
| Spring Data JPA | Acceso a base de datos           |
| PostgreSQL      | Base de datos                    |
| Lombok          | Reducción de boilerplate         |
| Firebase Admin  | Verificación de tokens JWT       |
| Groq API        | Interpretación de intents con IA |
| OneSignal API   | Envío de notificaciones push     |

---

## 🏗 Arquitectura

```
Usuario habla / escribe
    ↓
Web Speech API (browser) / input de texto
    ↓
POST /voice?email=...   { text: "agendar reunión mañana a las 10" }
    ↓
InterpreterService
  ├── Reglas simples (regex + keywords)   →  IntentResult
  └── AIService (Groq API) si no matchea  →  IntentResult
    ↓
VoiceController  →  switch(intent)
  ├── create_event  →  AgendaService.createEvent()
  ├── read_today    →  AgendaService.getTodayEvents()
  └── read_week     →  AgendaService.getWeekEvents()
    ↓
VoiceResponse { response: "Evento creado: ..." }
    ↓
SpeechSynthesis API (browser) lee la respuesta
```

---

## 📁 Estructura del proyecto

```
├── src/                              # Frontend React
│   ├── Api/api.jsx                   # Llamadas HTTP con Axios + Firebase token
│   ├── App.jsx
│   ├── Components/
│   │   ├── Eventlist.jsx             # Calendario con FullCalendar (CRUD)
│   │   ├── Header.jsx
│   │   ├── Layout.jsx
│   │   ├── Navbar.jsx
│   │   ├── PushNotificationButton.jsx # Activar recordatorios push
│   │   └── VoiceAssistant.jsx        # Micrófono + TTS + input de texto
│   ├── Context/
│   │   ├── AuthContext.jsx
│   │   └── AuthProvider.jsx          # Firebase onAuthStateChanged
│   ├── Hooks/
│   │   ├── useAuth.jsx
│   │   └── usePushNotifications.js   # Lógica de OneSignal
│   ├── Pages/
│   │   ├── Agenda.jsx
│   │   ├── Dashboard.jsx
│   │   └── auth/
│   │       ├── Login.jsx
│   │       └── Register.jsx
│   └── Router/Router.jsx             # Rutas públicas y privadas
│
├── src/main/java/com/Agenda/IA/      # Backend Spring Boot
│   ├── Controllers/
│   │   ├── AgendaController.java     # GET/POST/PUT/DELETE /events
│   │   ├── UserController.java       # GET/POST /users
│   │   ├── VoiceController.java      # POST /voice
│   │   └── HealthController.java     # GET /health
│   ├── Services/
│   │   ├── AgendaService.java
│   │   ├── InterpreterService.java
│   │   ├── AIService.java            # Integración Groq
│   │   ├── PendingIntentService.java # Contexto de conversación en memoria
│   │   └── ReminderScheduler.java    # Cron de recordatorios push
│   ├── Models/
│   │   ├── Event.java
│   │   └── User.java
│   ├── Repositories/
│   │   ├── EventRepository.java
│   │   └── UserRepository.java
│   ├── Security/
│   │   └── FirebaseAuthFilter.java   # Validación de token JWT
│   └── Config/
│       ├── FirebaseConfig.java
│       ├── SecurityConfig.java
│       └── GlobalExceptionHandler.java
│
├── Dockerfile                        # Build multi-stage del backend
├── firebase.js                       # Configuración Firebase (frontend)
└── vercel.json                       # Rewrite rules para SPA
```

---

## ⚙️ Variables de entorno

### Frontend (`.env`)

```env
VITE_API_URL=http://localhost:8080
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_PROJECT_ID=...
VITE_FIREBASE_STORAGE_BUCKET=...
VITE_FIREBASE_MESSAGING_SENDER_ID=...
VITE_FIREBASE_APP_ID=...
```

### Backend

```env
DB_URL=jdbc:postgresql://localhost:5432/agendaia
DB_USER=postgres
DB_PASSWORD=...
GROQ_API_KEY=gsk_...              # Groq API Key
FIREBASE_SERVICE_ACCOUNT_JSON=... # JSON del service account de Firebase (todo en una línea)
```

---

## 🚀 Correr el proyecto localmente

### Backend

**Requisitos:** Java 21, Maven, PostgreSQL

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
DB_URL=... DB_USER=... DB_PASSWORD=... GROQ_API_KEY=... \
FIREBASE_SERVICE_ACCOUNT_JSON='...' \
java -jar target/*.jar
```

**O con Docker:**

```bash
docker build -t agendaia-backend .
docker run -p 8080:8080 \
  -e DB_URL=... \
  -e DB_USER=... \
  -e DB_PASSWORD=... \
  -e GROQ_API_KEY=... \
  -e FIREBASE_SERVICE_ACCOUNT_JSON='...' \
  -e ONE_SIGNAL_ID=... \
  -e ONE_SIGNAL_KEY=... \
  agendaia-backend
```

### Frontend

**Requisitos:** Node.js 20+

```bash
npm install
npm run dev
```

Abre en `http://localhost:5173`

---

## 🔌 Endpoints de la API

| Método   | Ruta                    | Auth     | Descripción               |
| -------- | ----------------------- | -------- | ------------------------- |
| `GET`    | `/health`               | No       | Health check              |
| `GET`    | `/users?email={email}`  | No       | Obtener usuario por email |
| `GET`    | `/users/{id}`           | No       | Obtener usuario por ID    |
| `POST`   | `/users`                | No       | Crear usuario             |
| `GET`    | `/events/{userId}`      | Firebase | Eventos del usuario       |
| `POST`   | `/events?email={email}` | Firebase | Crear evento              |
| `PUT`    | `/events/{id}`          | Firebase | Editar evento             |
| `DELETE` | `/events/{id}`          | Firebase | Eliminar evento           |
| `POST`   | `/voice?email={email}`  | Firebase | Procesar comando de voz   |

> Los endpoints marcados con Firebase requieren el header `Authorization: Bearer <firebase-id-token>`.

---

## 🤖 Cómo funciona el asistente de IA

El `InterpreterService` primero intenta resolver el intent con reglas simples (keywords y regex). Si el mensaje es ambiguo o complejo, delega al `AIService`, que llama a la **Groq API** con el modelo `llama-3.3-70b-versatile` y espera una respuesta JSON:

```json
{
  "intent": "create_event",
  "title": "reunión con el cliente",
  "date": "2025-05-15",
  "time": "10:00"
}
```

El `PendingIntentService` guarda en memoria el contexto de conversaciones incompletas (por ejemplo, si el usuario dice el título pero no la hora). Cuando el evento se completa, el contexto se limpia.

Los intents soportados son `create_event`, `read_today` y `read_week`.

---
