package site.klade.webapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        hudPanel.setSpacing(false);
        hudPanel.setPadding(false);
        hudPanel.getStyle().set("position", "absolute")
                .set("top", "20px")
                .set("left", "20px")
                .set("z-index", "1000")
                .set("background", "rgba(0, 0, 0, 0.6666)")
                .set("padding", "4px")
                .set("border-radius", "4px")
                .set("color", "white")
                .set("margin", "0");
        // Line 1: Buttons
        HorizontalLayout buttonsLine = new HorizontalLayout();
        buttonsLine.setSpacing(false);
        buttonsLine.setPadding(false);
        buttonsLine.getStyle().set("margin", "0").set("gap", "4px");
        Button startButton = new Button("Start simulation", event -> {
            simulationService.startSimulation();
            UI.getCurrent().getPage().executeJs(
                    "console.log('Server simulation started');");
            startPolling(simulationService);
        });
        startButton.getStyle().set("margin", "0").set("font-size", "0.9rem");
        Button stopButton = new Button("Stop simulation", event -> {
            simulationService.stopSimulation();
            UI.getCurrent().getPage().executeJs(
                    "console.log('Server simulation stopped');");
        });
        stopButton.getStyle().set("margin", "0").set("font-size", "0.9rem");
        buttonsLine.add(startButton, stopButton);
        // Line 2: All statuses
        VerticalLayout statusesLine = new VerticalLayout();
        statusesLine.setSpacing(false);
        statusesLine.setPadding(false);
        statusesLine.getStyle().set("margin", "0");
        var space = new Paragraph("____");
        space.getStyle()
                .set("margin", "0").set("font-size", "0.90rem").set("line-height", "1.2");
        var header = new Paragraph("Simulation run on server:");
        header.getStyle()
                .set("margin", "0").set("font-size", "0.90rem").set("line-height", "1.2");
        ticksDisplay = new Paragraph("Total ticks: 0");
        stateChangesDisplay = new Paragraph("Rhyme node status changes: 0");
        statusDisplay = new Paragraph("Rhyme node current status: INACTIVE");
        // Style statuses - much smaller font
        ticksDisplay.getStyle()
                .set("margin", "0").set("font-size", "0.90rem").set("line-height", "1.2");
        stateChangesDisplay.getStyle()
                .set("margin", "0").set("font-size", "0.90rem").set("line-height", "1.2");
        statusDisplay.getStyle()
                .set("margin", "0").set("font-size", "0.90rem").set("line-height", "1.2");
        statusesLine.add(space, header, ticksDisplay, stateChangesDisplay, statusDisplay);
        // Assemble HUD
        hudPanel.add(buttonsLine, statusesLine);
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
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    SimulationDto dto = service.getSimulationDto();
                    ui.access(() -> {
                        ticksDisplay.setText("Total ticks: " + dto.getTotalTicks());
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
                ticksDisplay.setText("Total ticks: 0");
                stateChangesDisplay.setText("Rhyme node status changes: 0");
                statusDisplay.setText("Rhyme node current status: INACTIVE");
            });
        }, "simulation-polling-thread");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}