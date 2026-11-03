# 🤖 VoltFind - Chatbot Microservice

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deployment-Render-46e3b7.svg)](https://render.com)
[![Status](https://img.shields.io/badge/Port-8093-purple.svg)]()

The **Chatbot Service** is a dedicated conversational intelligence microservice in the **VoltFind** ecosystem. It acts as the intelligent orchestration bridge between frontend clients / API Gateway and downstream AI services (such as **Gemini Service** on port `8092`), enabling real-time EV station inquiries, route recommendations, and contextual conversational support.

---

## 🏗️ Architecture & Flow

```
                      ┌────────────────────────┐
                      │    VoltFind Frontend   │
                      │  (Mobile App / Web)    │
                      └───────────┬────────────┘
                                  │ HTTPS (port 8080 API Gateway)
                                  ▼
                      ┌────────────────────────┐
                      │    API Gateway         │
                      │ (Injects Internal Key) │
                      └───────────┬────────────┘
                                  │ HTTP + X-Internal-Secret
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Chatbot Service (Port 8093)                  │
│                                                                 │
│  [HealthController] ──> GET /health                             │
│  [ChatController]   ──> POST /api/chat                          │
│         │                                                       │
│         ▼                                                       │
│  [Secret Verification Filter] (Constant-time MessageDigest)     │
│         │                                                       │
│         ▼                                                       │
│  [ChatServiceImpl] ── Assemble History + Prompt + EV Location  │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  │ HTTP POST /gemini/chat
                                  ▼
                      ┌────────────────────────┐
                      │     Gemini Service     │
                      │      (Port 8092)       │
                      │ Google Gemini API LLM  │
                      └────────────────────────┘
```

---

## 🔒 Security & Authentication

Direct public access to AI pipelines is restricted to prevent token abuse:
- All `/api/chat` calls **MUST** provide the `X-Internal-Secret` HTTP header matching `APP_INTERNAL_SECRET`.
- Verification uses `java.security.MessageDigest.isEqual(...)` for constant-time comparison to eliminate side-channel timing attacks.
- Public health probes (`GET /health`) are unauthenticated to allow container uptime monitoring and keep-alive cron pings.

---

## 🚀 API Specification

### 1. Health & Keep-Alive Check
Used by orchestrators (Render, Kubernetes, Cron-Job.org) for zero-downtime monitoring.

- **Endpoints:** `GET /`, `GET /health`, `GET /api/health`
- **Auth:** None (Public)
- **Response `200 OK`:**
```json
{
  "status": "UP",
  "service": "chatbot-service",
  "port": 8093,
  "timestamp": "2026-11-04T03:30:00.123"
}
```

---

### 2. Conversational Chat Completion
Routes user queries with real-time geolocation context and multi-turn conversational history.

- **Endpoint:** `POST /api/chat`
- **Headers:**
  - `Content-Type: application/json`
  - `X-Internal-Secret: <your_configured_internal_secret>`
- **Request Payload (`ChatRequestDto`):**
```json
{
  "message": "Find fast-charging stations within 5 km of my current location.",
  "locationDto": {
    "latitude": 28.6139,
    "longitude": 77.2090,
    "accuracy": 15.0,
    "address": "Connaught Place, New Delhi"
  },
  "history": [
    {
      "role": "user",
      "parts": "Hello! I am low on battery."
    },
    {
      "role": "model",
      "parts": "Hello! Please share your current location and connector type."
    }
  ],
  "systemInstruction": "You are the VoltFind AI Assistant. Always prioritize nearby compatible charging stations with real-time availability."
}
```

- **Response `200 OK` (`ChatResponseDto`):**
```json
{
  "aiReply": "Found 3 fast-charging stations near Connaught Place: 1. Tata Power EZ Charge (50kW, 1.2 km away, 2 slots free)..."
}
```

- **Error Responses:**
  - `401 Unauthorized`: Missing or invalid `X-Internal-Secret` header.
  - `400 Bad Request`: Validation failure (blank message or missing systemInstruction).
  - `500 Internal Server Error`: Downstream Gemini service unreachable.

---

## ⚙️ Configuration & Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `server.port` | `8093` | Embedded Tomcat HTTP port |
| `APP_INTERNAL_SECRET` | *Configured HMAC secret* | Internal microservice authentication secret |
| `GEMINI_SERVICE_URL` | `https://gemini-service-jecn.onrender.com` | Base URL of downstream Gemini LLM Service |

Copy `.env.example` to `.env` or set environment variables in your hosting provider:
```bash
GEMINI_SERVICE_URL=http://localhost:8092
APP_INTERNAL_SECRET=your_internal_shared_secret_here
```

---

## 🛠️ Local Development & Build

### Prerequisites
- **JDK 21** or later
- **Maven 3.9+** (or use bundled `./mvnw`)

### Build & Run
```bash
# 1. Clone repository
git clone https://github.com/Aditya11376/Chatbot-service.git
cd Chatbot-service

# 2. Compile and test
./mvnw clean package -DskipTests

# 3. Run application
./mvnw spring-boot:run
```

The service will bind to `http://localhost:8093`. Test the health probe:
```bash
curl http://localhost:8093/health
```

---

## 🐳 Docker & Container Deployment

### Build Docker Image
```bash
docker build -t voltfind-chatbot:latest .
```

### Run Container
```bash
docker run -d \
  -p 8093:8093 \
  -e APP_INTERNAL_SECRET="your_secret" \
  -e GEMINI_SERVICE_URL="https://gemini-service-jecn.onrender.com" \
  --name voltfind-chatbot \
  voltfind-chatbot:latest
```

---

## 🌐 Cloud Deployment (Render)

This repository includes a native `render.yaml` blueprint:
1. Link this repository in **Render Dashboard**.
2. Select **Web Service (Docker)**.
3. Configure `APP_INTERNAL_SECRET` in Render Environment Variables.
4. Set Health Check path to `/health`.

---

## 📄 License
Part of the **VoltFind Microservices Platform**. Internal proprietary software.
