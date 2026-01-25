# Klade: The Evolution Simulator

**Version:** 2026.01.25_ver.02

## What is Klade?

Klade is an evolutionary simulation game where digital organisms evolve according to biological genetic laws. But it's not just another simulation—it's a laboratory for observing how complexity emerges from simple rules, exactly as it happens in nature.

**The core idea:** Give organisms simple building blocks, let them interact, and watch as evolution produces remarkable, unpredictable results.

## How It Works

### Infinite Evolution

Unlike traditional simulations with limited parameters, Klade creates a fundamentally infinite search space through two key mechanisms:

- **Coevolution**: When species adapt to fight each other, they push one another toward greater complexity forever. Each improvement creates pressure for the other to respond.
- **Unlimited structure**: Organisms can grow arbitrarily complex—there's no theoretical ceiling on what evolution can discover.

### Physical Neural Network

Here's what makes Klade unique: the organism's physical structure *is* its neural network. Every element that builds the body simultaneously performs computational functions. There's no separation between "brain" and "body"—they evolve together.

### Clear Visualization

Every element's state is immediately visible. Bright colors mean active; dark means inactive. No hidden parameters or abstract health bars. What you see is exactly what's happening.

### Continuous Evolution

The simulation runs on the server at maximum speed, constantly evolving generations. You can connect at any moment to see current results—no waiting for real-time rendering to catch up.

## Building Blocks

Organisms consist of just two element types:

**Nodes** (geometric points):
- Friction nodes: grip the surface to enable movement
- Rhythm nodes: internal timers generating periodic signals
- Neurons: computational elements with tanh() activation

**Segments** (directed connections):
- Muscles: contract to produce movement
- Spikes: weapons that destroy enemy elements
- Probes: sensors detecting objects in a specific direction
- Neural links: transmit and transform signals with adjustable weights

The simplest viable organism requires just three elements—a rhythm node, a muscle, and a friction node. Yet this minimal structure already produces emergent, directed movement.

## The Arena

Each battle features one-on-one combat between two organisms. This focused approach allows detailed visualization of internal structure and behavior while creating intense selection pressure.

## Why This Matters

Klade demonstrates emergence—properties that cannot be predicted by studying components in isolation. When species coevolve, each pushes the other toward new levels of complexity in an endless spiral of development.

This isn't programmed behavior. It's evolution in action.

## Current Status

The project completed architecture validation in January 2026 and is now building toward MVP (Minimum Viable Product). The MVP will demonstrate core concepts with three species, basic element types, and visual battle simulation.

**What's planned after MVP:**
- New element types (shield nodes, distance detectors, shooting segments)
- Diverse arena types (races, sumo battles, team fights)
- Player-managed species with configurable evolution parameters
- Distributed computing client for faster evolution
- GPU-accelerated calculations

## Technology

Built with Spring Boot, Vaadin, and libGDX. The three-module architecture separates management, visualization, and simulation logic while maintaining seamless integration.

## Get Involved

Klade is open source and community-driven. Whether you're a developer, designer, researcher, or simply curious about emergence, your contribution matters.

**We need:**
- Developers for architecture review, security, and new features
- Designers for visual style, interface, and animations
- Researchers for evolutionary algorithm optimization
- Community members for testing, feedback, and spreading the word

**Resources:**
- GitHub: https://github.com/SlyCright/Klade
- Support: https://boosty.to/klade (RUS)

---

*This is an overview of Klade's vision. For detailed technical documentation, behavioral specifications, and development plans, see the full vision document at [docs/vision/vision-en-full.md](docs/vision/vision-en-full.md).*
