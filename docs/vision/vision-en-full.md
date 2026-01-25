# I. Klade: General Description


**Version:** 2026.01.25_ver.02

## 1. Introduction

Klade is an evolutionary simulation game in which digital organisms evolve according to biological genetic laws.

Klade is a new type of evolutionary simulation where players manage their species, allowing natural selection to produce remarkable results. Unlike traditional simulations, where evolution is typically constrained in one way or another, Klade creates a different environment. In this environment, complexity emerges naturally from simple rules. Artificial organisms (specimens) consist of a minimal set of elements, each performing one clearly defined function, but their combinations give rise to an incredible diversity of behaviors and forms. The development of such a system has no theoretical limit.

The project is conceived as a platform for exploring emergent properties—phenomena that cannot be predicted by studying components in isolation. When species coevolve, each pushes the other toward new levels of complexity, creating an endless spiral of development. This is not just a simulation—it is a laboratory for observing how order is born from chaos and how simple rules give rise to infinite complexity. Exactly as it happens in living nature.

Klade allows you to experience this process through beautiful, mesmerizing visualization, where creatures forged by evolution fight, compete, and develop before the viewer's eyes. At the same time, the simulation runs continuously on the server, and a player can connect at any moment to see the current results of thousands of selection cycles.


# II. Klade: MVP Scope


**Current development status (January 2026):** The project has completed the architecture validation phase—it has been demonstrated that three independent software modules (management, visualization, and simulation) work together through a portal pattern with asynchronous simulation.

The next goal is to implement the MVP. "MVP" stands for Minimum Viable Product—a version with basic functionality sufficient to demonstrate the concept.

In order to demonstrate the concept in action, it is necessary to implement the key ideas of the modeling philosophy and equip the digital organism with a minimal set of components sufficient to launch a theoretically endless cycle of self-development. This set includes means of perception (the "sensory organ" component), analysis and decision-making (neural network components), as well as environmental interaction (movement and attack components). Such a minimal set creates conditions for the manifestation of emergent properties and the operation of evolutionary mechanisms in full force.

## 2. Model

### 2.1. Modeling Philosophy

At the core of Klade lies the idea that complexity can emerge from simplicity rather than being constructed from the start. Traditional evolutionary simulations often suffer from a limited search space: organisms have parameters—for example, speed, size, and defense—and after these are optimized, evolution stops. In Klade, the search space is fundamentally infinite thanks to two key mechanisms.

The first mechanism is **coevolution**. When one species adapts to fight another, the second species is forced to respond with its own adaptations. This creates a continuous "arms race," where each step forward by one species stimulates the development of the other. As a result, both species constantly grow in complexity, and this process has no natural endpoint, since improvement in one always creates pressure on the other.

The second mechanism is **the possibility of infinite structural complexity of the digital organism**. Specimens consist of elements that, in turn, can be combined into arbitrary structures. Mathematically, this means an infinite configuration space where evolution can search indefinitely without reaching a limit. In other models, the parameter set is often limited and finite—here, there is no limit to the growth of complexity.

A distinctive feature of the model that sets it apart from other evolutionary simulators is the **physical neural network**. In Klade, the organism's structure itself *is* the neural network. The elements of the specimen that form its physical body simultaneously perform the functions of neurons and connections between them. Each element is both a building block of the body and a computational element of the network. There are also elements whose direct function is to be a neural network element—similar to nerve cells in living nature. This radically distinguishes Klade from simulations where the "brain" and "body" are separate entities.

Special attention is given to **clarity**. Every aspect of the simulation should be understandable to the observer. The current state of an element must be immediately visible: whether it is active or not, contracted or relaxed, whether it has detected a target or not. No hidden parameters, no abstract "health bars"—only a transparent, readable system that is interesting to observe.

**Priority on the speed of evolutionary calculations.** Traditional simulations are constrained by the need to display the process in real time. Klade uses the opposite approach: evolutionary computation on the server runs continuously at the maximum speed available to the hardware, without visualization. The user can connect at any moment and view the current results, but this does not stop the calculations. This allows reaching interesting emergent results orders of magnitude faster than with real-time simulation.

### 2.2. Geometric Foundation of Calculations

The physical model of Klade is built on a minimal set of geometric primitives and their interactions. This decision is driven by the pursuit of computational efficiency and mathematical simplicity. All physics reduces to three types of interactions: between two points, between a point and a line segment, and between two line segments. This approach allows calculations to be performed at maximum speed, which is critically important for server-side simulation running in the background.

Digital organisms (specimens) consist of elements: **points** ("nodes") and **line segments** ("segments"). The structure of a specimen and the properties of its elements are determined by its digital genome.

All simulation forces act on the nodes. Each computation tick sums all applied forces and changes the node's position according to the resultant vector.

Segments connect nodes and create the structure of the specimen. Their elastic properties determine how forces are transmitted between nodes and how the specimen's shape responds to external influences.

Such a system creates physically plausible behavior with minimal computational cost. Collisions, deformations, and destruction — all naturally follow from the geometric relationships of the basic primitives.

## 3. Model Components

### 3.1. Specimen

A specimen is the central object of the simulation, representing a living organism in the virtual world. Mathematically, a specimen is modeled as a directed graph, where nodes are vertices and segments are oriented edges. This graph has a dual representation: physical (in the simulation) and visual (for the player), and both representations are identical and synchronized.

Each specimen possesses a genome—a data structure defining its structure and behavior. The genome includes information about the type and number of elements, their interconnections, and the parameters of each element. During reproduction, the genome is passed to offspring with possible mutations, creating the foundation for the evolutionary process.

If any element of a specimen is destroyed, the specimen continues to function. If, as a result of destruction, a specimen splits into two unconnected specimens, one dies off. The one that contains the first node in the genome's node list survives.

### 3.1.1. Simplest Viable Specimen

The minimally viable specimen requires three parts: **node-segment-node**. This simplest configuration creates the foundation for emergent behavior.

**Example: Rhythm Node → Muscle → Friction Node**

This three-element structure produces directed movement through alternating phases:

**I. Contraction Phase:**
1. The rhythm node in the active phase generates a signal
2. The signal activates the muscle segment
3. The contracted muscle transmits the signal to the friction node
4. The friction node activates, "gripping" the surface
5. The friction node travels a shorter distance than the rhythm node, creating asymmetry

**II. Relaxation Phase:**
1. The rhythm node in the inactive phase does not generate a signal
2. The muscle loses activation and tends toward a relaxed state
3. The friction node deactivates and stops "gripping"
4. Both nodes travel equal distances

The unequal distances traveled by the friction node result in rhythmic movement of the entire organism toward the friction node. This is a **primitive example of emergent behavior**, arising from the composition of simple rules rather than from complex parts.

### 3.2. Element

Elements are the building blocks that make up specimens. There are two basic types of elements: nodes and segments. A node is fundamentally a geometric point; a segment is fundamentally a line segment.

All elements share common properties: each can receive and transmit a signal, each has a visual representation corresponding to its current state, and each can be destroyed during interactions.

Each element performs strictly one function, and the performance of this function is immediately readable from its visual state. For all elements, high brightness (V=0.9 in the HSV model) indicates an active function, while low brightness (V=0.3) indicates an inactive one. This unification allows the current state of any element on the arena to be determined instantly.

Functions of elements that do not have a geometric direction are assigned to nodes. Functions of elements with direction are assigned to segments, and their direction is determined by the segment's baseline.

All elements of the same type function identically. No hidden parameters can distinguish one element from another. Any parameter affecting the operation of an element must be visually represented, encoded in the specimen's genome, and easily "readable" by the observer.

For example, the contracted length of a muscle. Different muscles may have different contracted lengths. This is clearly visible, and the model can find evolutionary advantages for muscles of different lengths. The length itself is encoded by a gene.

For each element, an activation threshold is defined (value 0.5), uniform for all types except neurons and neural links. When a signal above the threshold is received, the element activates or deactivates depending on its normal state. If an element's function is activated, it outputs a signal with value 1.0; if not—a signal of 0.0. This binary behavior ensures clarity: the state of an element is always unambiguous.

Most element types can be normally-active or normally-inactive. This means the element "waits" for a signal to switch from its inactive state to active (or vice versa) in order to apply its function (or conversely, to stop applying it). Such a system creates the foundation for all behavioral patterns: from simple reflexes to actions based on complex neural computations.

An important property of specimen elements is **state integrity**. Elements either function or are destroyed. There are no intermediate states of damage. This simplifies understanding of the system and makes the outcome of collisions predictable: if an element is destroyed, it simply stops working, and the observer sees this.

When a segment is destroyed—only the segment itself is destroyed. When a node is destroyed—the node itself and all connected segments are destroyed. A segment cannot exist without one of its nodes.

When an element is destroyed, a visual "imprint" remains in its place—a very dark gray trace showing where the element was located. This enhances the visual narrative of the fight between specimens.

#### 3.2.1. Node

A node is a point that serves as the foundation of a specimen's structure. Geometrically and physically, a node is a material point upon which forces act. Each simulation tick, all forces are summed into a resultant vector that sets the node in motion. Forces arise from interactions with other simulation components: friction, segment elasticity, collision reactions, and others.

In addition to its physical role, each node is a neuron with a threshold activation function (except for nodes of the "Neuron" type). The input signal is the weighted sum of signals from all segments connected to the node at their output end. The output signal is always either 0.0 or 1.0. When the threshold value is exceeded, a normally-active node deactivates, and a normally-inactive node activates. The result is transmitted to all segments attached to the node at their input end.

All nodes are visually represented as circles of identical radius. This ensures consistency and simplifies visual perception. Additionally, nodes (both from different specimens and within the same specimen) repel each other when they approach within a distance equal to the circle's diameter. This creates the effect of a "physical body" and prevents nodes from penetrating each other.

##### 3.2.1.1. Friction Node

When activated, a friction node increases its coefficient of friction. This allows the specimen to "stick" to the surface or resist movement, creating the foundation for the specimen's ability to move (locomotion) or control body position in space.

Genetically, this type of node does not have a separate function gene—its behavior is fixed.

Visually, an active friction node is displayed in bright gray, an inactive one—in dark gray.

##### 3.2.1.2. Rhythm Node

A rhythm node is the specimen's internal timer, generating periodic signals without external stimulus. When activated, the node begins to output a signal according to a square-wave function with a duty cycle of 0.5. The output signal alternates between 0.0 and 1.0 with a constant period, creating a rhythmic foundation for movements.

The function gene determines the wave period, allowing evolution to tune the rhythm frequency for a specific specimen.

Visually, an active rhythm node is displayed with a bright dirty-yellow outline, an inactive one—with a dark dirty-yellow outline. The central part of the node takes the color of the active outline while outputting a signal and the color of the inactive outline when there is no signal. The diameter of the central part is 4/3 of the node's diameter, creating a characteristic "eye" that shows the current rhythm state.

##### 3.2.1.3. Neuron

A neuron is a computational element that processes input signals and transmits the result. Unlike other nodes, a neuron uses the tanh() activation function.

The neuron sums all incoming signals from segments connected to it at their output end, obtaining a weighted sum, and applies the tanh() function to it. The result is transmitted to all segments attached to the neuron at their input end. A neuron does not have "activated" or "deactivated" states—it works continuously, processing incoming data.

The choice of activation function is determined by the appropriate output signal parameter, which is limited to the range [-1.0; 1.0]. The limited range allows for clear visualization of the signal.

Visually, a neuron is displayed with a gradient depending on its current output signal. For the range [-1.0; 0.0], a gradient from bright blue to dark blue is used; for the range [0.0; 1.0]—from dark green to bright green. The value 0.0 corresponds to the intersection point of the gradients—a dark greenish-blue color. This approach allows the observer to assess the intensity of neural activity at a glance.

#### 3.2.2. Segment

A segment is a connection between two nodes, a directed line segment with an input end and an output end. The direction determines signal transmission: from the input end to the output end. Geometrically and physically, a segment is an elastic element that tends to maintain its normal length.

Genetically, a normal length is encoded for each segment. If, in a given tick, the segment's length deviates from normal, it applies a force to the nodes at its ends proportional to the magnitude of the deformation (Hooke's law). The elasticity coefficient is the same for all segment types except neural links.

When a segment is stretched to twice its normal length, it is destroyed. There are no compression limits—a segment can be compressed as much as possible. The normal length of a segment is constrained to a range from 1/2 to 3/2 of the base segment length (100.0 units) for all types except neural links.

Segments also repel nodes that are within a certain distance from their geometric line. If a node is at a distance equal to the node's radius plus half the segment's strip width, a perpendicular force directed away from the segment is applied to the node. This prevents nodes from penetrating through segments and creates a "physical body" effect for the elements.

In addition to their physical role, segments (except neural links) function as neurons with threshold activation. The input signal comes from the node connected to the segment's input end. The output signal is always either 0.0 or 1.0. When the threshold is exceeded, a normally-inactive segment activates; a normally-active segment deactivates. The result is transmitted to the output node.

Segment intersection is not allowed (except for neural links). Upon initial placement of a specimen on the arena, the system checks for the absence of intersections. If an intersection is detected, both segments are destroyed. Neural links can intersect with all elements since they are assumed to be on a "different layer."

The proposed physical model does not allow any segments to intersect during simulation (except neural links). Therefore, there is no need to check for this and destroy intersecting segments during simulation computation.

Visually, segments are represented as strips with a rounded input end (touching the input node at a single point) and a triangular output end (touching the output node at a single point). This design clearly indicates the direction of signal transmission.

##### 3.2.2.1. Muscle

A muscle is the locomotive element of a specimen. When activated, it contracts, applying force to the nodes at its ends. In elasticity calculations, the contracted length is used instead of the normal length, creating a pulling force.

The function gene determines the contracted length in the range from 1/4 to 4/4 of the segment's normal length. This allows evolution to tune the force and amplitude of contraction for a specific muscle.

Visually, a contracted (active) muscle is displayed in bright brick-red, a relaxed (inactive) one—in dark brick-red.

##### 3.2.2.2. Spike

A spike is a specimen's weapon, designed to destroy enemy elements. When activated, a point extends from the main segment, protruding by 1/4 of the segment's normal length and directed along its axis. An active point destroys any elements—both its own and foreign—upon intersection, including neurons and neural links.

After destroying an element, the spike automatically deactivates and enters a recovery period before becoming active again. In the inactive state, the point does not interact with the environment and can freely intersect other elements.

Interactions between spikes and other spikes or probes are specific: when two active points intersect, both spikes deactivate (but are not destroyed); when an active point intersects the main segment of another spike—the intersected spike is destroyed; when an active point intersects an active probe beam—the probe triggers a signal but is not destroyed; when an inactive point intersects an active probe beam—the probe does not trigger a signal.

Genetically, a spike does not have a separate function gene.

Visually, an active spike (main segment or point) is displayed in bright orange, an inactive one—in dark orange. The point is represented as a triangle with a base equal to the segment's strip width, rounded at the base and pointing away from the spike's output node.

##### 3.2.2.3. Probe

A probe is a sensory element that detects objects in a specific direction. When activated, a beam extends from the main segment toward the input node, with a length of 2/3 of the segment's normal length. If the active beam intersects a node or segment—the probe outputs a signal to the output node.

A probe detects any elements, including elements of its own specimen. Upon detection of a target, a signal of 1.0 is output; in the absence of one—0.0. Interactions with other elements: when an active beam intersects an active spike point—the probe triggers a signal; when intersecting with an inactive point—no signal is triggered; when two active beams intersect—both probes output a signal; when two inactive beams intersect, or if only one beam is active—neither probe outputs a signal.

Genetically, a probe does not have a separate function gene.

Visually, an active probe (beam or main segment) is displayed in bright yellow, an inactive one—in dark yellow. The beam is visualized as a strip with 1/3 of the normal segment width, rounded at both ends.

##### 3.2.2.4. Neural Link

A neural link is an element of the neural network that transmits and transforms signals. It multiplies the input signal by its weight value (in the range [-2.0; 2.0]) and transmits the result to the output node. This allows the neural network to adjust the strength and polarity of signals.

The weight range boundaries are selected so that the network can evolutionarily tune itself to preserve the signal when passing through multiple neural nodes (in conjunction with tanh() activation).

The segment's elasticity coefficient is proportional to the absolute value of the weight: at weight 2.0, the coefficient is maximum and equals the normal segment elasticity coefficient; at weight 0.0—there are no elastic forces.

A neural link can intersect with all elements, including other neural links, since they are assumed to be on a different "layer." However, it is destroyed upon intersection with an active spike point. The normal length of a neural link is not constrained and is determined by the gene.

Visually, a neural link is displayed with a gradient, similar to a neuron: from bright blue (output signal -2.0) to bright green (output signal 2.0). The strip thickness is proportional to the absolute value of the weight. At a weight value of 2.0 or -2.0—the neural link's thickness equals the normal segment thickness.

##### 3.2.3. Physical Neural Network

All elements of a specimen collectively form a physical neural network—a unified structure where the physical body and computational network are inseparable. Nodes and segments simultaneously form morphology and perform the functions of neurons and connections. Neurons and neural links, in turn, create analog computational pathways with adjustable weights. This means that evolution simultaneously optimizes both form and "mind"—a process impossible in simulations with separated brain and body.

A critical consequence of this approach: damage to neural elements directly damages the specimen's computational ability. When an active spike point destroys a neuron or neural link, this is equivalent to brain damage—the specimen loses not just a body fragment but specific computational functions. This creates unique evolutionary pressure: organisms must protect not only physical integrity but also the integrity of their computational architecture, forcing evolution to search for both effective behavioral strategies and their reliable placement within the body.

### 3.3. Species

A species is a set of specimens that share a common genetic origin. Genetic material is not transferred between species. This creates conditions for coevolution. Species adapt to each other, adjusting to conditions created by the other species.

A total of three species participate in the simulation. Presumably, it makes sense to have more than two species in the simulation. Two species are more likely to fall into a "local optimum" when searching for evolutionary solutions and stop developing.

Each species has parameters: population size, mutation probabilities, proportions of elite specimens and specimens reproducing through mitosis or meiosis.

### 3.4. Arena

In traditional evolutionary simulations, dozens, hundreds, or even thousands of organisms act simultaneously on a common field. Such an approach imposes serious limitations: it is impossible to visualize in detail the internal workings of each specimen. It is difficult for the observer to track what is happening inside individual organisms, and it is computationally expensive for the simulation to calculate and display their internal processes simultaneously.

The arena in Klade is a two-dimensional space where only two specimens interact. The principle of "one-on-one" confrontation is implemented: only two opposing organisms are present on the arena at any time. This solution allows computational resources to be focused on detailed modeling of internal structure and behavior, while also making the process clear to the observer.

### 3.5. Simulation

Traditional evolutionary simulations are constrained by the need to display everything happening in real time. This requires reducing the simulation frequency to a comfortable level for perception and spends computational power on graphics. Klade uses a fundamentally different approach.

Evolutionary computation occurs on the server continuously, without visual representation, at the maximum available speed. Upon completion of each evolutionary cycle, the system updates the species' genomes and starts the next one. The user can activate visual mode at any moment and observe the current state—battles between specimens of the current generation that have already undergone many cycles of evolutionary development.

This approach allows interesting results to be obtained orders of magnitude faster. While the user watches one minute of visualization, dozens or even hundreds of generations of evolution may pass in the background. At the same time, the user does not lose connection with the process—they see the current state of the species.

Visualization works as follows: the system takes the current genomes of specimens of different species, selects two random specimens, and conducts a visual battle between them. While visualization is in progress, several evolutionary cycles may already pass in the background simulation, updating generations. This creates the effect of a living, continuously evolving world.

The system is built so that battle calculation on the server and battle calculation for visualization are executed according to the same rules (executed by the same program code). This ensures the authenticity of visualization—one can be confident that battles between the same specimens proceed identically both on the server and when viewed.

## 4. Evolutionary Mechanism

### 4.1. Forming the Next Generation

Evolution in Klade is based on the principles of natural selection with several key features. Each species forms a separate evolutionary branch—genes are not mixed between species. This allows species to develop in different directions and creates conditions for coevolution.

To determine fitness, fights are conducted. Each specimen of the new generation fights a certain number of times against random opponents from other species. The number of fights is chosen based on server performance: more fights—more accurate fitness assessment, but higher system load.

The basic rule of battle is the fight for the center of the arena. The winner is the one closer to the center at the end of the fight and who dealt more damage to the opponent while preserving more of their own elements.

### 4.2. Fitness Function

The fitness function value is determined by three criteria. The first is the specimen's position on the arena at the end of the fight, calculated by the average coordinate of all nodes. The closer to the center, the higher the score. If the specimen died during the fight, the last known average coordinate is used.

The second criterion is the ratio of destroyed enemy elements to the total number of their elements at the start of the fight. The higher the ratio, the better.

The third criterion is the ratio of destroyed own elements to the total number of elements at the start of the fight. The smaller this ratio, the better (higher fitness).

The combination of these criteria creates a multifactorial assessment that accounts for both aggressiveness (ability to destroy the opponent), defensive capabilities (preservation of own elements), and tactical positioning (fighting for the center).

### 4.3. Selection and Reproduction Mechanisms

Two mechanisms are used to form the next generation: elitism and tournament selection. Elite specimens (with the best fitness values for the species) are guaranteed to pass to the next generation without changes, preserving the best characteristics of the species.

The tournament mechanism for selecting parents works as follows: two specimens are randomly selected from the current generation, and the most fit is chosen from each pair. These winners become parents of the next generation.

Reproduction occurs in two ways: mitosis and meiosis. In mitosis, the offspring receives a nearly identical genome from the parent with minor mutations. In meiosis, the genome is formed from the genes of two parents, creating new combinations of traits.

## 5. Player Capabilities

At the MVP stage, the player has access to a limited but coherent experience. The main capability is observing fights between specimens of one of three predefined species. The system automatically selects two random specimens and displays their battle in real time.

While visualization is in progress, the simulation continues to run in the background, conducting evolutionary cycles. Upon completion of visualization, the system again selects two specimens from the updated generation, and the cycle repeats. The player observes a continuous stream of evolution, seeing how specimens become more complex and efficient with each new generation.

This minimal experience allows understanding the main idea of the project: evolution in action, where complexity arises from simple rules rather than being constructed manually.


# III. Klade: Development After MVP


## 6. Predicted MVP Problems and Their Solutions

### 6.1. The Local Optimum Problem

The most serious problem of any evolutionary system is developmental stagnation due to reaching a local optimum. Specimens find a strategy that works well enough, and evolution stops improving it. This problem appears to be relevant for the MVP, where the model is relatively simple.

Ways to solve the problem:

**New element types:** adding new elements expands the search space, opening new evolutionary directions.

**Diversity of arenas and competition types:** different conditions create different selection pressures, preventing specialization for only one type of battle.

**Multiplayer:** many players, each with their own species, create constant diversity and unpredictability in the evolutionary process. Each individual player's species is its own direction of selection for other species.

### 6.2. Computational Limitations

Server resources are limited, and this imposes natural limits on the speed of evolution. Solutions include code optimization, transferring calculations to GPU, and leveraging players' computational resources through a separate client.

## 7. Extensions and Improvements After MVP

### 7.1. New Element Types

After the MVP release, a significant expansion of the element set is planned, which will open new evolutionary possibilities. One can come up with practically infinite variations of new functions for elements. For example, from what already comes to mind:

- **New node types**:
    - Nodes with a shield-protection from spikes;
    - Different types of detector nodes, for example, detecting objects within a given radius (1/5 of the standard segment length) without direction binding;
    - Neurons with different activation functions (visualization requires separate development);
    - Neurons with bias, allowing adjustment of the activation threshold;
    - ...

- **New segment types**
    - "Shooting" segments;
    - Segments with a reactive jet, providing short-term force impulse, helping with movement;
    - Bone — a rigid element with increased elasticity, not transmitting signals, visually yellowish-white with medium brightness;
    - Distance detectors outputting an analog signal in the range [0.0; 1.0] depending on the distance to the nearest intersection of the probe with an object;
    - Color detectors registering color parameters (RGB or HSB) analogously to a probe, but for optical characteristics;
    - ...

### 7.2. New Arena and Competition Types

Diversity of competition conditions stimulates evolution in different directions. As with elements, one can come up with practically infinite variations of conditions and fitness functions for arenas. For example, one can imagine:

- **Races** — speed competition to reach a goal, where the first to reach the finish wins. This stimulates the development of speed characteristics and efficient locomotion patterns;
- **Sumo** — the task is to push the opponent out of the arena boundary;
- **Group battles** — for example, two versus two or team fights. This creates conditions for the evolution of cooperative behavior and tactical schemes;
- **Arena with a "death zone"** — a dangerous zone periodically passing through the arena, destroying everything in its path. Survival requires timely evasion;
- **Arenas with specific conditions** — obstacles, variable wind, changing gravity, non-uniform surface. Each condition creates unique selection pressure;
- **Arenas where element functions work non-standardly**, for example, "slippery" ones — where the friction node does not work
- ...

### 7.3. Player Capabilities

After the MVP release, player capabilities are planned to expand. The player becomes the owner and manager of a species but without the ability to directly construct individual specimens.

The player's species participate in calculating an overall ranking among all species of other players. This creates a competitive element and stimulates the improvement of species management strategies.

A player can own several species simultaneously, allowing experimentation with different evolutionary strategies. For each species, the following settings are available:

- **Species population**: more specimens — more opportunities to find successful solutions, but higher server load and slower generation of each subsequent generation.
- **Mutation parameters**: the ability to increase or decrease the overall probability of mutations, as well as fine-tune the probabilities of different mutation types.
- **Reproduction proportions**: determining the ratio of elite specimens, those reproducing through mitosis and meiosis, and the proportion of specimens eliminated after fitness calculation.
- **Number of fights**: determining the number of battles specimens must conduct before fitness assessment.

The ability to set these settings automatically should be provided. Since settings affect computation volumes, the task of fair distribution of server time among players will need to be solved.

Additionally, the ability to work with specific specimens within configurable parameters will appear: one can designate a specimen as "elite" (it is never eliminated) or start a new species from a specific specimen, effectively creating a branch of the evolutionary tree.

For analytics, species evolution graphs and interactive phylogenetic trees will be available, showing relationships between specimens and the history of species development.

A visual genome exploration mode will be added, where the user explores genome sections, and the interface "highlights" the corresponding parts of the specimen.

The ability to conduct a visual mode battle between different specimens of different species, either from one's own or other players', both from current and archived generations.

Some specimen structure editors could be provided. At the same time, it must be understood that the ability of players to determine the structure of a specific specimen themselves will violate the principles embedded in the simulation concept. Therefore, such editors should only be used for some research tasks and answering "What if?" questions that the player poses to themselves. The resulting specimens and their genomes should not participate in the main evolutionary stream. Although it will be possible to organize a separate evolutionary stream for "genetically modified specimens."

For players wishing to accelerate the evolution of their species, a separate client is planned. This application will utilize the player's computer's computational power for evolutionary calculations, synchronizing with the main server. This approach allows scaling computational resources proportionally to the number of active players.

### 7.4. Visual and Audio Improvements

To increase spectacle, plans include adding element destruction animations, visual effects (particle effects) such as sparks when active spikes collide, as well as sound effects for key events.

### 7.5. Computational Improvements

**GPU computing**: transferring calculations to GPU is planned for significant performance improvement. This will allow conducting more evolutionary cycles per unit of time, visualizing more complex scenes, and significantly accelerating the evolutionary process for faster emergence of interesting evolutionary solutions.

**Distributed computing**: a separate application using the player's computer's computational power for evolutionary calculations, synchronizing with the main server. This creates a distributed computing model where each player contributes to the overall evolution. A player can prioritize calculations only for their own species.


# IV. Klade: Technical Implementation Details and Community Support


## 8. Project Technology Stack

### 8.1. Technology Choice Rationale

The Klade project is built on the Spring Boot + Vaadin + libGDX stack. This choice is driven by several key considerations, each of which has significant importance for the successful implementation of the project.

**Java ecosystem orientation** The developer is not a professional programmer and, although passionate about the project, has very limited time to work on it due to family and work commitments. At the same time, they are most familiar with the Java language (at a "junior" level). Therefore, it is desirable to minimize time spent learning new technologies and spend it on the actual project implementation. The Java ecosystem provides a complete set of tools to solve current and future project tasks: server-side (backend) development, frontend and web interface creation, data visualization, as well as cross-platform base simulation logic development that should run both on the server and in the browser, and in the future—in a client application for different operating systems.

**Spring Boot** is the main server framework for Java. It provides reliable server infrastructure with support for asynchronous operations necessary for background evolutionary simulation. Built-in Spring Data JPA mechanisms simplify database work, and a wide range of ready-made modules allows quick implementation of required functionality. Spring Boot deploys as a single executable JAR, which greatly simplifies deployment on the server.

**Vaadin** allows creating web interfaces entirely in Java, without the need to write HTML, CSS, or JavaScript. This is critically important for the project where the developer has no experience with frontend technologies. Vaadin provides a rich set of components and excellent integration with Spring, making interface development efficient and predictable.

**libGDX** is a powerful Java game framework, ideal for 2D simulation visualization. It provides high rendering performance, cross-platform capability (including HTML5 through GWT), and is actively developed. libGDX integration with Gradle simplifies project building and dependency management.

**Windows-based server** is a choice driven by practical necessity and the developer's limited resources. The developer has the most experience working with Windows servers, which speeds up deployment and simplifies operation. There is already a rented Windows Server used for another project that requires integration with a CAD system (which works exclusively in the Windows ecosystem). It is also impractical to pay for two servers at once when there is an opportunity to deploy both projects on the same server infrastructure.

Nevertheless, it appears that the right decision would be to migrate the project to a Linux server with Docker containerization. This is a more common solution for such projects, which will simplify scaling, reduce operational costs, and potentially help attract more participants familiar with this type of server. The transition can be made when enough funds are collected in the project fund for long-term rental of a Linux server.

### 8.2. System Architecture

The project has a three-component architecture divided into independent modules.

**Management layer (main/)** is a Spring Boot + Vaadin application providing a lobby, account settings, and species configuration. This is the entry point for the player, where they interact with the interface and manage their species.

**Simulation client (stage/)** is a separate libGDX module compiled to HTML5 and provided as static resources. It is responsible for visualizing battles and displaying the current state of specimens.

**Shared components (simulation/)** is the place for code shared between server and client. This will contain data models and game logic.

Integration between layers is implemented through a portal pattern: Vaadin views use an iFrame component to embed the libGDX HTML5 client. HUD elements (buttons, indicators) are placed over the iFrame with pointer-events management. This architecture allows each module to work independently while maintaining a cohesive user experience.

### 8.3. Infrastructure and Deployment

The server side is hosted on a rented server with the Windows Server operating system. To ensure a secure connection, the HTTPS protocol is used, with configuration done through the IIS reverse proxy server.

The continuous integration and delivery (CI/CD) process is fully automated using GitHub Actions. The deployment workflow is as follows:

Upon each change to the master branch of any of the three project software modules:

1. The simulation library (simulation/) is published to GitHub Packages.
2. The HTML5 client (stage/) is built and necessary artifacts are generated.
3. The main project (main/) updates dependencies: downloads the latest library version from GitHub Packages and client artifacts.
4. The target updated JAR file is automatically deployed to the server using a self-hosted runner.

## 9. How to Help the Project

Klade is an open-source project developed by enthusiasts. We are looking for people willing to contribute to creating a unique evolutionary simulation.

**Developers** can help with code: architecture review, security audit, implementing new features. The project is written in Java, uses Spring Boot and libGDX—knowledge of any of these technologies will be useful.

**Designers** can help with visual style: logo, interface design, animations. Beauty and clarity are key values of the project.

**Researchers** can help with evolutionary algorithms and models. Fitness function optimization, new selection mechanisms, analysis of emergent properties—all these are areas for research.

**The community** can help with spreading the idea, participating in testing, providing feedback, and contributing ideas. Every voice is important for creating a project that is fascinating to people.

The project is developed on GitHub: https://github.com/SlyCright/Klade. You can also find documentation for contributors and the current development status there.

If you want to support the project financially, we have an account on Boosty: https://boosty.to/klade (RUS). Donations will help cover server costs and allow more time to be dedicated to development.

---

*This vision document serves as the foundation for all other Klade project documents. It describes the philosophy, architecture, and key decisions shaping the project. The document will be updated as the project develops and feedback from the community is received.*
