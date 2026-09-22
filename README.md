# Klade Evolutionary Simulation Game

**Version:** 2026.01.25_ver.05  

## Description
An open-source multiplayer evolutionary simulation where players create species and watch their 2D specimens compete in dynamic arenas with fluid animations and particle effects.

## Live Site: [klade.site](https://klade.site/)

## Vision
A hobby project driven by a vision I couldn't find anywhere else. I'm genuinely curious what creatures become at high evolution levels. Open to community contributions. The core game will always remain open-source.

For more details:
- [VISION.md](VISION.md) — **Short:** The high-level overview of Klade's evolutionary simulation game and its goals.
- [docs/vision/vision-en-full.md](docs/vision/vision-en-full.md) — **Long:** A detailed overview of Klade's evolutionary simulation game, architecture, and community involvement. It covers the core concepts, planned features, technology stack, and project's vision for the future.

## Project Structure
The Klade application follows a **three-project portal architecture**:
- **main** (this repository): Spring Boot + Vaadin management interface
- **[stage](https://github.com/SlyCright/klade-stage)**: libGDX HTML5 simulation client (separate repository)
- **simulation**: Shared simulation logic (future separate repository)
Each project is an independent project with its own Git repository and Gradle build. The Vaadin 
  UI embeds the stage client via iframe for seamless integration.

### How the Components Connect
- **Management UI and backend** runs on `localhost:8080` (Vaadin)
- **Simulation Client** is served as static resources at `/stage/` (libGDX GWT)
- **Integration**: Iframe in Vaadin loads from the same origin; HUD overlay uses absolute positioning
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
2. Create the PostgreSQL database:
   ```sql
   CREATE DATABASE klade;
   ```
3. Create `src/main/resources/application-local.yaml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/klade
       username: your_postgres_username
       password: your_postgres_password
   ```
4. Run `./gradlew bootRun`  
5. Open http://localhost:8080

### Windows Development Notes

**IntelliJ IDEA - "Command line is too long" Error**

If you encounter a "Command line is too long" error when running the application from IntelliJ IDEA on Windows:

1. Go to **Run → Edit Configurations**
2. Select the `KladeWebApplication` configuration
3. Click **Modify Options** (or "More Options" in older versions)
4. Select **Shorten command line**
5. Choose **JAR manifest** or **classpath file** from the dropdown
6. Click **Apply** and **OK**
7. Run the application again

This is a Windows-specific limitation where the classpath exceeds the OS command line length limit. The workaround configures IntelliJ to use a JAR manifest or classpath file instead of passing all dependencies as command-line arguments.

## Building for Production
Run `./gradlew clean build -Pproduction`

## Community & Support
- **Funding**: [Boosty](https://boosty.to/klade) (RUS)
- **License**: Apache 2.0 (or your chosen license)
- **Deployment**: Windows Server bare-metal (no Docker)