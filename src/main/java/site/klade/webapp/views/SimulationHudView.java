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

@Route("simulation")
@AnonymousAllowed
@PageTitle("Rhythm Node POC - Server View")
@SuppressWarnings("unused")
public class SimulationHudView extends Div {

    private final Paragraph ticksDisplay;

    private final Paragraph stateChangesDisplay;

    private final Paragraph statusDisplay;

    public SimulationHudView(AsyncSimulationService simulationService) {
        // Fullscreen layout
        getStyle().set("position", "relative")
                .set("width", "100vw")
                .set("height", "100vh")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow", "hidden");
        // libGDX iframe (full screen)
        IFrame iframe = new IFrame("http://localhost:8082");
        iframe.getElement().setAttribute("frameborder", "0");
        iframe.getStyle().set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("border", "none")
                .set("z-index", "1");
        // HUD Panel (top-left overlay)
        VerticalLayout hudPanel = new VerticalLayout();
        hudPanel.getStyle().set("position", "absolute")
                .set("top", "20px")
                .set("left", "20px")
                .set("z-index", "1000")
                .set("background", "rgba(0, 0, 0, 0.7)")
                .set("padding", "15px")
                .set("border-radius", "8px")
                .set("color", "white")
                .set("min-width", "250px");
        H3 title = new H3("Server Simulation HUD");
        title.getStyle().set("margin", "0 0 10px 0");
        ticksDisplay = new Paragraph("Total Ticks: 0");
        stateChangesDisplay = new Paragraph("State Changes: 0");
        statusDisplay = new Paragraph("Node Status: INACTIVE");
        Button startServertButton = new Button("Start Server Simulation", event -> {
            simulationService.startSimulation();
            UI.getCurrent().getPage().executeJs("console.log('Server simulation restart requested');");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.startPolling(simulationService);
        });
        startServertButton.getStyle().set("margin-top", "10px");
        hudPanel.add(title, ticksDisplay, stateChangesDisplay, statusDisplay, startServertButton);
        // Container
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
                        ticksDisplay.setText("Total Ticks: " + dto.getTotalTicks());
                        stateChangesDisplay.setText("State Changes: " + dto.getRhymeNodeStateChanges());
                        statusDisplay.setText("Node Status: " + (dto.getIsRhymeNodeCurrentlyActive() ? "ACTIVE" : "INACTIVE"));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    break;
                }
            }
            ui.access(() -> {
                ticksDisplay.setText("Total Ticks: 0");
                stateChangesDisplay.setText("State Changes: 0");
                statusDisplay.setText("Node Status: INACTIVE");
            });
        }, "simulation-polling-thread");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}