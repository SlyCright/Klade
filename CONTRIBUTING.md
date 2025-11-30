# Contributing to Klade

**Version:** 2025.11.30_ver.02

## Greetings
Thank you for your interest!

## What We Need Most
- **Senior architects**: Code review, security audits, performance guidance
- **Graphics developers**: libGDX rendering pipeline expertise
- **Community builders**: Documentation, issue triage, outreach

## Development Constraints (Negotiable)
- **Java-only server-side**: No handwritten HTML/CSS/JS
- **Monolithic Spring Boot**: No microservices
- **Windows Server deployment**: Paths and scripts must be Windows-compatible
- **No Docker**: Bare metal JAR execution
- **Gradle**: Use `./gradlew` wrapper exclusively

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

## Contact
Open an issue for architectural discussions before major changes.