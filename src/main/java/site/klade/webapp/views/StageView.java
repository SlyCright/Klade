package site.klade.webapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("stage")
@AnonymousAllowed
@SuppressWarnings("unused")
@PageTitle("Klade Stage")
public class StageView extends Div {

    private static final String STAGE_URL = "http://localhost:8082";

    public StageView() {
        // Fullscreen container
        getStyle().set("position", "relative")
                .set("width", "100vw")
                .set("height", "100vh")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow", "hidden");
        // libGDX iframe
        IFrame iframe = new IFrame(STAGE_URL);
        iframe.getElement().setAttribute("frameborder", "0");
        iframe.getStyle().set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("border", "none");
        // HUD overlay button
        Button hudButton = new Button("HUD check", event ->
                UI.getCurrent().getPage().executeJs(
                        "console.log('HUD button clicked!');" +
                                "alert('This is a Vaadin button overlaying libGDX!');"
                ));
        hudButton.getStyle().set("position", "absolute")
                .set("top", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("pointer-events", "auto"); // Enable clicks
        // Container for both components
        Div container = new Div(iframe, hudButton);
        container.getStyle().set("position", "relative")
                .set("width", "100%")
                .set("height", "100%");
        add(container);
    }
}