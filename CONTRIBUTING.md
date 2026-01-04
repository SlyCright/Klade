# Contributing to Klade

**Version:** 2026.01.04_ver.04

## Greetings
Thank you for your interest!

## What We Need Most
- **Senior architects**: Code review, security audits, performance guidance
- **Graphics developers**: libGDX rendering pipeline expertise
- **Community builders**: Documentation, issue triage, outreach

## Development Constraints
- **Java-only server-side**: No handwritten HTML/CSS/JS
- **Monolithic Spring Boot**: No microservices
- **Windows Server deployment**: Paths and scripts must be Windows-compatible
- **No Docker**: Bare metal JAR execution
- **Gradle**: Use `./gradlew` wrapper exclusively

## Repository Structure & Contributing
Klade uses a **multi-repository architecture**. Please contribute to the appropriate repository:
- **[SlyCright/Klade](https://github.com/SlyCright/Klade)** (this repo): Vaadin UI, server logic, WebSocket endpoints, authentication, database
- **[SlyCright/klade-stage](https://github.com/SlyCright/klade-stage)**: libGDX rendering, particle effects, arena graphics, client-side simulation
- **Future: SlyCright/klade-simulation**: Shared ECS components, physics engine, genetic algorithms

### Where to Open Issues
- **UI bugs, login problems, database issues**: Open in **main** repository
- **Rendering glitches, graphics performance, GWT compilation**: Open in **stage** repository
- **Uncertain?** Open in main repository and I'll transfer it

### Working with Multiple Repositories
1. **Clone each repository separately** into distinct folders
2. **Open each as independent IntelliJ IDEA projects** (do NOT open as modules)
3. **Use separate Gradle tool windows** for each project
4. **Run on different ports** simultaneously (main: 8080, stage: 8082)
When making changes that affect both repositories (e.g., API contracts), open pull requests in both repos and reference each other.

## Getting Started
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Ensure no secrets are committed
4. Use meaningful commit messages
5. Open a Pull Request against `main`

## Build Commands
- **Development**: `./gradlew bootRun`
- **Production**: `./gradlew clean build -Pproduction`
- **Tests**: `./gradlew test`

### Development Workflow
1. Modify stage client code
2. Run `./gradlew copyStageToMain` in stage project (builds and copies to main)
3. Run `./gradlew bootRun` in main project

## Contact
Open an issue for architectural discussions before major changes.