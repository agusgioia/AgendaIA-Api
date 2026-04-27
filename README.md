# 📅 Agenda IA

Aplicación web de agenda personal con asistente de voz impulsado por inteligencia artificial. Permite crear y gestionar eventos hablando en lenguaje natural en español.

---

## 🧠 ¿Qué hace?

- Interpretás comandos de voz como _"agendar reunión mañana a las 10"_ y el sistema crea el evento automáticamente.
- Podés preguntar _"¿qué tengo para hoy?"_ o _"¿qué eventos tengo esta semana?"_ y el asistente te responde en voz alta.
- Los eventos se muestran en un calendario interactivo con vistas diaria, semanal y mensual.
- La autenticación es gestionada por Firebase.

---

## 🛠 Stack

### Frontend

| Tecnología        | Uso                                    |
| ----------------- | -------------------------------------- |
| React 19 + Vite 8 | Framework y bundler                    |
| TailwindCSS 3     | Estilos                                |
| PrimeReact 10     | Componentes UI (Dialog, Button, Card…) |
| FullCalendar 6    | Calendario interactivo                 |
| Firebase 12       | Autenticación (email/password)         |
| Axios             | Llamadas HTTP a la API                 |
| React Router 7    | Navegación y rutas protegidas          |

### Backend

| Tecnología           | Uso                              |
| -------------------- | -------------------------------- |
| Spring Boot 4        | Framework principal              |
| Spring Data JPA      | Acceso a base de datos           |
| PostgreSQL           | Base de datos                    |
| Lombok               | Reducción de boilerplate         |
| Anthropic Claude API | Interpretación de intents con IA |

---

## 🏗 Arquitectura

```
Usuario habla
    ↓
Web Speech API (browser)
    ↓
POST /voice?email=...   { text: "agendar reunión mañana a las 10" }
    ↓
InterpreterService
  ├── Reglas simples (regex + keywords)   →  IntentResult
  └── AIService (Claude API) si no matchea →  IntentResult
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
├── src/                          # Frontend React
│   ├── Api/api.jsx               # Llamadas HTTP con Axios
│   ├── App.jsx
│   ├── Components/
│   │   ├── Eventlist.jsx         # Calendario con FullCalendar
│   │   ├── Header.jsx
│   │   ├── Layout.jsx
│   │   ├── Navbar.jsx
│   │   └── VoiceAssistant.jsx    # Micrófono + TTS
│   ├── Context/
│   │   ├── AuthContext.jsx
│   │   └── AuthProvider.jsx      # Firebase onAuthStateChanged
│   ├── Hooks/useAuth.jsx
│   ├── Pages/
│   │   ├── Agenda.jsx
│   │   ├── Dashboard.jsx
│   │   └── auth/
│   │       ├── Login.jsx
│   │       └── Register.jsx
│   └── Router/Router.jsx         # Rutas públicas y privadas
│
├── src/main/java/com/Agenda/IA/  # Backend Spring Boot
│   ├── Controllers/
│   │   ├── AgendaController.java  # GET/POST/DELETE /events
│   │   ├── UserController.java    # GET/POST /users
│   │   ├── VoiceController.java   # POST /voice
│   │   └── HealthController.java  # GET /health
│   ├── Services/
│   │   ├── AgendaService.java
│   │   ├── InterpreterService.java
│   │   └── AIService.java         # Integración Claude
│   ├── Models/
│   │   ├── Event.java
│   │   └── User.java
│   ├── Repositories/
│   │   ├── EventRepository.java
│   │   └── UserRepository.java
│   └── DTO/
│       ├── IntentResult.java
│       ├── VoiceRequest.java
│       └── VoiceResponse.java
│
├── Dockerfile                    # Build multi-stage del backend
├── firebase.js                   # Configuración Firebase
└── index.html                    # Landing page estática
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
API_KEY=sk-ant-...   # Anthropic API Key
```

---

## 🚀 Correr el proyecto localmente

### Backend

**Requisitos:** Java 21, Maven, PostgreSQL

```bash
# Clonar y compilar
mvn clean package -DskipTests

# Ejecutar con variables de entorno
DB_URL=... DB_USER=... DB_PASSWORD=... API_KEY=... java -jar target/*.jar
```

**O con Docker:**

```bash
docker build -t agendaia-backend .
docker run -p 8080:8080 \
  -e DB_URL=... \
  -e DB_USER=... \
  -e DB_PASSWORD=... \
  -e API_KEY=... \
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

| Método   | Ruta                    | Descripción               |
| -------- | ----------------------- | ------------------------- |
| `GET`    | `/health`               | Health check              |
| `GET`    | `/users?email={email}`  | Obtener usuario por email |
| `GET`    | `/users/{id}`           | Obtener usuario por ID    |
| `POST`   | `/users`                | Crear usuario             |
| `GET`    | `/events/{userId}`      | Eventos del usuario       |
| `POST`   | `/events?email={email}` | Crear evento              |
| `DELETE` | `/events/{id}`          | Eliminar evento           |
| `POST`   | `/voice?email={email}`  | Procesar comando de voz   |

---

## 🤖 Cómo funciona el asistente de IA

El `InterpreterService` primero intenta resolver el intent con reglas simples (keywords y regex). Si el mensaje es ambiguo o complejo, delega al `AIService`, que llama a **Claude claude-sonnet-4-20250514** con un prompt estructurado y espera una respuesta JSON:

```json
{
  "intent": "create_event",
  "title": "reunión con el cliente",
  "date": "2025-05-15",
  "time": "10:00"
}
```

Los intents soportados son `create_event`, `read_today` y `read_week`.
