package site.klade.webapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import site.klade.simulation.SimulationDto;
import site.klade.webapp.service.AsyncSimulationService;

@Route("stage")
@AnonymousAllowed
@PageTitle("Klade Stage")
@SuppressWarnings("unused")
public class StageView extends Div {

    private static final String STAGE_URL = "http://localhost:8082";

    private final Paragraph ticksDisplay;

    private final Paragraph stateChangesDisplay;

    private final Paragraph statusDisplay;

    public StageView(AsyncSimulationService simulationService) {
        // Fullscreen layout
        getStyle().set("position", "relative")
                .set("width", "100vw")
                .set("height", "100vh")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow", "hidden");
        // libGDX iframe (full screen)
        IFrame iframe = new IFrame(STAGE_URL);
        iframe.getElement().setAttribute("frameborder", "0");
        iframe.getStyle().set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("border", "none")
                .set("z-index", "1");
        // Minimal HUD Panel (top-left overlay)
        VerticalLayout hudPanel = new VerticalLayout();
        hudPanel.getStyle().set("position", "absolute")
                .set("top", "20px")
                .set("left", "20px")
                .set("z-index", "1000")
                .set("background", "rgba(0, 0, 0, 0.3333)")
                .set("padding", "6px")
                .set("border-radius", "6px")
                .set("color", "white")
                .set("min-width", "200px")
                .set("margin", "0");
        H3 title = new H3("Simulation");
        title.getStyle().set("margin", "0 0 8px 0").set("font-size", "1.2rem");
        ticksDisplay = new Paragraph("Ticks: 0");
        ticksDisplay.getStyle().set("margin", "0px 0");
        stateChangesDisplay = new Paragraph("Changes: 0");
        stateChangesDisplay.getStyle().set("margin", "0px 0");
        statusDisplay = new Paragraph("Status: INACTIVE");
        statusDisplay.getStyle().set("margin", "0px 0");
        Button startButton = new Button("Start", event -> {
            simulationService.startSimulation();
            UI.getCurrent().getPage().executeJs("console.log('Server simulation started');");
            startPolling(simulationService);
        });
        startButton.getStyle().set("margin-top", "8px")
                .set("font-size", "0.9rem");
        hudPanel.add(title, ticksDisplay, stateChangesDisplay, statusDisplay, startButton);
        // Container for both components
        Div container = new Div(iframe, hudPanel);
        container.getStyle().set("position", "relative")
                .set("width", "100%")
                .set("height", "100%");
        add(container);
    }

    @SuppressWarnings("BusyWait")
    private void startPolling(AsyncSimulationService service) {
        UI ui = UI.getCurrent();
        Thread pollingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    SimulationDto dto = service.getSimulationDto();
                    ui.access(() -> {
                        ticksDisplay.setText("Total ticks: " +
                                dto.getTotalTicks());
                        stateChangesDisplay.setText("Rhyme node status changes: " +
                                dto.getRhymeNodeStateChanges());
                        statusDisplay.setText("Rhyme node current status: " +
                                (dto.getIsRhymeNodeCurrentlyActive() ? "ACTIVE" : "INACTIVE"));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    break;
                }
            }
            ui.access(() -> {
                ticksDisplay.setText("Ticks: 0");
                stateChangesDisplay.setText("Changes: 0");
                statusDisplay.setText("Status: INACTIVE");
            });
        }, "simulation-polling-thread");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}