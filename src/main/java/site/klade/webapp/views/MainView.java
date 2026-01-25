package site.klade.webapp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
@PageTitle("Klade - Evolutionary Simulation Game")
@StyleSheet("styles.css") // TODO: should be implemented in the "VaadinAppConfig", not here
public class MainView extends Div {

    private static final String GITHUB_URL = "https://github.com/SlyCright/Klade";

    private static final String BOOSTY_URL = "https://boosty.to/klade";

    private static final String VISION_DOC_URL = "https://github.com/SlyCright/Klade/blob/master/docs/vision-en-full.md";

    public MainView() {
        add(createHeroSection());
        add(createShortVisionSection());
        add(createFeaturesSection());
        add(createStageSection());
        add(createTrustSection());
        add(createActionButtonsSection());
        add(createFooter());
    }

    private Div createHeroSection() {
        Div hero = new Div();
        hero.addClassName("hero-section");
        H1 title = new H1("KLADE");
        H3 subtitle1 = new H3(
                "Evolution simulation game where digital organisms evolve by fighting each " +
                        "other");
        H3 subtitle2 = new H3("(Currently in early development)");
        Div explanationBox = createExplanationBox();
        Button ctaButton = createPrimaryButton();
        Paragraph ctaCaption = new Paragraph("Technical prototype: current visual simulation, " +
                "future game arena");
        ctaCaption.addClassName("cta-caption");
        hero.add(title, subtitle1, subtitle2, explanationBox, ctaButton, ctaCaption);
        return hero;
    }

    private Div createExplanationBox() {
        Div box = new Div();
        box.addClassName("explanation-box");
        H4 header = new H4("How Is This Different From a Normal Game?");
        Paragraph p1 = new Paragraph("In most games, creatures are designed by artists. In " +
                "Klade, they're designed by evolution.");
        Paragraph p2 = new Paragraph("They consist of simple parts — a muscle, a timer, a sensor. The computer breeds thousands of variants, keeps the winners, and repeats the process. After a hundred generations, you get creatures that act in ways no human imagined.");
        Paragraph p3 = new Paragraph("While you're away, the server runs at full speed. Check back tomorrow — your species might have evolved a new strategy.");
        box.add(header, p1, p2, p3);
        return box;
    }

    private Details createShortVisionSection() {
        Details details = new Details();
        details.addClassName("short-vision-section");
        details.setSummaryText("Vision document (5-minute read)");
        details.setOpened(false);
        // TODO: parse document from resource file
        String visionShort = """
                
                ### Klade: The Evolution Simulator
                
                **Version:** 2026.01.25_ver.02
                
                #### What is Klade?
                
                Klade is an evolutionary simulation game where digital organisms evolve according to biological genetic laws. But it's not just another simulation—it's a laboratory for observing how complexity emerges from simple rules, exactly as it happens in nature.
                
                **The core idea:** Give organisms simple building blocks, let them interact, and watch as evolution produces remarkable, unpredictable results.
                
                ###### Infinite Evolution
                
                Unlike traditional simulations with limited parameters, Klade creates a fundamentally infinite search space through two key mechanisms:
                
                - **Coevolution**: When species adapt to fight each other, they push one another toward greater complexity forever. Each improvement creates pressure for the other to respond.
                - **Unlimited structure**: Organisms can grow arbitrarily complex—there's no theoretical ceiling on what evolution can discover.
                
                ###### Physical Neural Network
                
                Here's what makes Klade unique: the organism's physical structure *is* its neural network. Every element that builds the body simultaneously performs computational functions. There's no separation between "brain" and "body"—they evolve together.
                
                ###### Clear Visualization
                
                Every element's state is immediately visible. Bright colors mean active; dark means inactive. No hidden parameters or abstract health bars. What you see is exactly what's happening.
                
                ###### Continuous Evolution
                
                The simulation runs on the server at maximum speed, constantly evolving generations. You can connect at any moment to see current results—no waiting for real-time rendering to catch up.
                
                ##### Building Blocks
                
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
                
                ##### The Arena
                
                Each battle features one-on-one combat between two organisms. This focused approach allows detailed visualization of internal structure and behavior while creating intense selection pressure.
                
                ##### Why This Matters
                
                Klade demonstrates emergence—properties that cannot be predicted by studying components in isolation. When species coevolve, each pushes the other toward new levels of complexity in an endless spiral of development.
                
                This isn't programmed behavior. It's evolution in action.
                
                ##### Current Status
                
                The project completed architecture validation in January 2026 and is now building toward MVP (Minimum Viable Product). The MVP will demonstrate core concepts with three species, basic element types, and visual battle simulation.
                
                **What's planned after MVP:**
                - New element types (shield nodes, distance detectors, shooting segments)
                - Diverse arena types (races, sumo battles, team fights)
                - Player-managed species with configurable evolution parameters
                - Distributed computing client for faster evolution
                - GPU-accelerated calculations
                
                ##### Technology
                
                Built with Spring Boot, Vaadin, and libGDX. The three-module architecture separates management, visualization, and simulation logic while maintaining seamless integration.
                
                ##### Get Involved
                
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
                
                *This is an overview of Klade's vision. For detailed technical documentation, behavioral specifications, and development plans, see the full vision document:
                https://github.com/SlyCright/Klade/blob/master/docs/vision-en-full.md*
                
                """;
        Markdown markdown = new Markdown(visionShort);
        details.add(markdown);
        return details;
    }

    private HorizontalLayout createFeaturesSection() {
        HorizontalLayout features = new HorizontalLayout();
        features.addClassName("features-container");
        features.add(createFeatureCard(
                "♾️",
                "Endless Evolution (Theoretically)",
                "No artificial complexity limits. Coevolution creates an infinite search space—creatures can grow arbitrarily complex without hitting a ceiling.",
                "feature-coevolution"));
        features.add(createFeatureCard(
                "🧠",
                "Brain & Body Are One",
                "Destroy a limb, destroy thought. Every element computes and fights.",
                "feature-brain"));
        features.add(createFeatureCard("⚡",
                "Zero-Downtime Evolution",
                "Your species evolve even when you're offline. The world never stops.",
                "feature-uptime"));
        return features;
    }

    private Div createFeatureCard(String emoji, String title, String description, String className) {
        Div card = new Div();
        card.addClassName("feature-card-" + className);
        Paragraph emojiIcon = new Paragraph(emoji);
        emojiIcon.addClassName("feature-emoji");
        H5 cardTitle = new H5(title);
        Paragraph cardDesc = new Paragraph(description);
        card.add(emojiIcon, cardTitle, cardDesc);
        return card;
    }

    private Div createStageSection() {
        Div section = new Div();
        section.addClassName("stage-section");
        H3 heading = new H3("Live: Rhythm Node Engine Test");
        Div iframeContainer = new Div();
        iframeContainer.addClassName("iframe-container");
        IFrame iframe = new IFrame("/stage/index.html");
        iframe.getElement().setAttribute("frameborder", "0");
        Paragraph caption = new Paragraph();
        caption.addClassName("stage-caption");
        caption.getElement().setProperty("innerHTML", "<strong>What you're seeing:</strong> Server-side async simulation ↔ libGDX client rendering through independent modules.");
        iframeContainer.add(iframe);
        section.add(heading, iframeContainer, caption);
        return section;
    }

    private HorizontalLayout createTrustSection() {
        HorizontalLayout trust = new HorizontalLayout();
        trust.addClassName("trust-section");
        trust.add(createTrustBadge(VaadinIcon.CHECK, "Architecture Validated<br>Jan 2026"));
        trust.add(createTrustBadge(VaadinIcon.COG, "CI/CD Operational<br>GitHub Actions"));
        trust.add(createTrustBadge(VaadinIcon.PUZZLE_PIECE, "Three-Project Independence<br>main, stage, simulation"));
        return trust;
    }

    private Div createTrustBadge(VaadinIcon icon, String labelHtml) {
        Div badge = new Div();
        badge.addClassName("trust-badge");
        Icon badgeIcon = new Icon(icon);
        badgeIcon.addClassName("badge-icon");
        Paragraph label = new Paragraph();
        label.addClassName("trust-label");
        label.getElement().setProperty("innerHTML", labelHtml);
        badge.add(badgeIcon, label);
        return badge;
    }

    private HorizontalLayout createActionButtonsSection() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions-container");
        actions.add(createLinkButton(
                "View Architecture on GitHub",
                GITHUB_URL, VaadinIcon.CODE, false));
        actions.add(createLinkButton(
                "Support on Boosty (RUS)",
                BOOSTY_URL, VaadinIcon.HEART, true));
        actions.add(createLinkButton(
                "Read Full Vision Document",
                VISION_DOC_URL, VaadinIcon.EXTERNAL_LINK, false));
        return actions;
    }

    private Button createLinkButton(String text, String url, VaadinIcon icon, boolean primary) {
        Button button = new Button(text, e -> getUI().ifPresent(ui -> ui.getPage().open(url, "_blank")));
        button.setIcon(new Icon(icon));
        button.addClassName("action-button");
        if (primary) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
        return button;
    }

    private Button createPrimaryButton() {
        Button button = new Button(
                "See Tech Prototype →",
                e -> getUI().ifPresent(ui -> ui.navigate("stage")));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClassName("primary-button");
        return button;
    }

    private Paragraph createFooter() {
        Paragraph footer = new Paragraph("Version: 0.0.1-SNAPSHOT | Status: Architecture Validated | License: Apache 2.0");
        footer.addClassName("footer");
        return footer;
    }
}