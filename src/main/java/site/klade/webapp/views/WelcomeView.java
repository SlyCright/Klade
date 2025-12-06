package site.klade.webapp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
@SuppressWarnings("unused")
@PageTitle("Klade - Evolutionary Simulation Game")
public class WelcomeView extends VerticalLayout {

    private static final String BOOSTY_URL = "https://boosty.to/klade";

    private static final String GITHUB_URL = "https://github.com/SlyCright/Klade";

    private static final String VERSION =
            WelcomeView.class.getPackage().getImplementationVersion() != null ?
                    WelcomeView.class.getPackage().getImplementationVersion() :
                    "0.0.1-SNAPSHOT";

    public WelcomeView() {
        configureLayout();
        addHeader();
        addDescription();
        addCallToAction();
        addFooter();
    }

    private void configureLayout() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);
    }

    private void addHeader() {
        H1 title = new H1("Klade");
        title.getStyle().set("font-size", "4rem")
                .set("margin", "0");
        Paragraph subtitle = new Paragraph("Evolutionary Simulation Game");
        subtitle.getStyle().set("font-size", "1.0rem")
                .set("margin-top", "0")
                .set("color", "var(--lumo-secondary-text-color)");
        add(title, subtitle);
    }

    private void addDescription() {
        Paragraph description = new Paragraph(
                "An open-source multiplayer simulation where players create species and" +
                        " watch evolutionary competition unfold. Currently in early development."
        );
        description.getStyle().set("max-width", "600px")
                .set("text-align", "center")
                .set("margin-top", "1.5rem");
        add(description);
    }

    private void addCallToAction() {
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setSpacing(true);
        actionLayout.getStyle().set("flex-wrap", "wrap");
        actionLayout.getStyle().set("margin-top", "2rem");
        Button boostyButton = createActionButton(
                "Support on Boosty",
                BOOSTY_URL,
                VaadinIcon.HEART
        );
        Button githubButton = createActionButton(
                "View on GitHub",
                GITHUB_URL,
                VaadinIcon.CURLY_BRACKETS
        );
        actionLayout.add(boostyButton, githubButton);
        add(actionLayout);
    }

    private Button createActionButton(String text, String url, VaadinIcon icon) {
        Button button = new Button(text, event ->
                getUI().ifPresent(ui -> ui.getPage().open(url, "_blank")));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        button.setIcon(new Icon(icon));
        return button;
    }

    private void addFooter() {
        Div footer = new Div();
        footer.getStyle().set("margin-top", "3rem")
                .set("font-size", "0.875rem")
                .set("color", "var(--lumo-secondary-text-color)");
        // Dynamic version info
        Paragraph versionInfo = new Paragraph("Version: " + VERSION +
                " | Status: LibGDX Integration In Progress");
        Paragraph techStack = new Paragraph("Built with" +
                " Spring Boot 3.5.8," +
                " Vaadin 24.9.6," +
                " Java 17 &" +
                " PostgreSQL");
        techStack.getStyle().set("margin-top", "0.5rem")
                .set("font-size", "0.75rem");
        // Security-focused contributor callout
        Paragraph securityNote = new Paragraph("🔒 Security reviews welcome - see SECURITY.md");
        securityNote.getStyle().set("margin-top", "1rem")
                .set("font-size", "0.75rem");
        footer.add(versionInfo, techStack, securityNote);
        add(footer);
    }
}