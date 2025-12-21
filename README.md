# Klade Evolutionary Simulation Game

**Version:** 2025.21.21_ver.03  

## Description
An open-source multiplayer evolutionary simulation where players create species and watch their 2D specimens compete in dynamic arenas with fluid animations and particle effects.

## Vision
A hobby project driven by a vision I couldn't find anywhere else. I'm genuinely curious what creatures become at high evolution levels. Open to community contributions. The core game will always remain open-source.

## Project Structure
The Klade application follows a **three-project portal architecture**:
- **main** (this repository): Spring Boot + Vaadin management interface
- **[stage](https://github.com/SlyCright/klade-stage)**: libGDX HTML5 simulation client (separate repository)
- **simulation**: Shared simulation logic (future separate repository)
Each project is an independent project with its own Git repository and Gradle build. The Vaadin 
  UI embeds the stage client via iframe for seamless integration.

### How the Components Connect
- **Management UI and backend** runs on `localhost:8080` (Vaadin)
- **Simulation Client** runs on `localhost:8082` (libGDX GWT)
- **Integration**: Iframe in Vaadin loads HTML5 client; HUD overlay uses absolute positioning
- **Communication**: WebSocket STOMP for real-time data exchange 
See the [klade-stage](https://github.com/SlyCright/klade-stage) repository for libGDX client setup and development.

## Current State
The iframe integration proof-of-concept is complete. The Vaadin management UI can successfully launch and overlay controls on the libGDX simulation client. The project is ready for implementation of core simulation logic and graphics pipeline development.

## Tech Stack
- **Backend**: Spring Boot 3.5.8 + Java 17
- **UI**: Vaadin 24.9.6 (Java-only)
- **Simulation**: libGDX (GWT/HTML5 client)
- **Database**: PostgreSQL
- **Real-time**: WebSocket STOMP

## Current State
The project is in early development. See `CONTRIBUTING.md` to get involved.

## Setup for Development
1. Ensure Java 17+ and PostgreSQL are installed
2. Create `application-local.yaml` and copy `application-local.yaml.example` into it.
3. Configure your local database and other credentials in `application-local.yaml`
4. Run `./gradlew bootRun`  
5. Open http://localhost:8080

## Building for Production
Run `./gradlew clean build -Pproduction`

## Community & Support
- **Funding**: [Boosty](https://boosty.to/klade)
- **License**: Apache 2.0 (or your chosen license)
- **Deployment**: Windows Server bare-metal (no Docker)