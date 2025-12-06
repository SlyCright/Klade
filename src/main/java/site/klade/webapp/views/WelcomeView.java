package site.klade.webapp.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
@PageTitle("Klade - Evolutionary Simulation Game")
@SuppressWarnings("unused")
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        // Configure layout
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);

        // Title
        H1 title = new H1("The Klade!");
        title.getStyle().set("font-size", "4rem")
                         .set("margin", "0");

        // Subtitle
        Paragraph subtitle = new Paragraph("Evolutionary Simulation Game");
        subtitle.getStyle().set("font-size", "1.5rem")
                          .set("margin-top", "0");

        // Description
        Paragraph description = new Paragraph(
            "An open-source multiplayer simulation where players create species " +
            "and watch evolutionary competition unfold. Currently in early development."
        );
        description.getStyle().set("max-width", "600px")
                             .set("text-align", "center");

        // Add components
        add(title, subtitle, description);
    }
}