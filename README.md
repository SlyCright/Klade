# Klade Evolutionary Simulation Game

**Version:** 2025.11.16_ver.01  

## Description
An open-source multiplayer evolutionary simulation where players create species and watch their 2D specimens compete in dynamic arenas with fluid animations and particle effects.

## Vision
A hobby project driven by a vision I couldn't find anywhere else. I'm genuinely curious what creatures become at high evolution levels. Open to community contributions. The core game will always remain open-source.

## Tech Stack
- **Backend**: Spring Boot 3.5.7 + Java 18
- **UI**: Vaadin 24.9.5 (Java-only)
- **Simulation**: libGDX (GWT/HTML5 client)
- **Database**: PostgreSQL
- **Real-time**: WebSocket STOMP

## Current State
The project is in early development. See `CONTRIBUTING.md` to get involved.

## Setup for Development
1. Ensure Java 17+ and PostgreSQL are installed
2. Create `application-local.yaml` and copy `application-local.yaml.example` into it.
3. Configure your local database and other credentials in `application-local.yaml`
4. Run `./mvnw spring-boot:run`

## Community & Support
- **Funding**: [Boosty](https://boosty.to/klade)
- **License**: Apache 2.0 (or your chosen license)
- **Deployment**: Windows Server bare-metal (no Docker)