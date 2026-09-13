package site.klade.webapp.views;

import com.vaadin.flow.component.Component;
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
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import site.klade.webapp.service.GenomeQueryService;
import site.klade.webapp.service.SimulationLifecycleService;
import site.klade.webapp.service.SpeciesStats;

import java.util.List;

@Slf4j
@Route("stage")
@AnonymousAllowed
@PageTitle("Klade Stage")
public class StageView extends Div {

    private static final String STAGE_URL = "/stage/index.html";

    private static final String CLASS_FULL_SIZE = "stage-full-size";

    private static final String CLASS_CONTAINER = "stage-container";

    private static final String CLASS_HUD_PANEL = "stage-hud-panel";

    private static final String CLASS_STATUS_LINE = "stage-status-line";

    private static final String CLASS_BUTTONS_LINE = "stage-buttons-line";

    private static final String CLASS_TEXT_COMMON = "stage-text-common";

    private static final int POLL_INTERVAL_MILLIS = 1000;

    private final SimulationLifecycleService lifecycleService;

    private final GenomeQueryService genomeQueryService;

    private Paragraph statusDisplay;

    public StageView(SimulationLifecycleService lifecycleService, GenomeQueryService genomeQueryService) {
        this.lifecycleService = lifecycleService;
        this.genomeQueryService = genomeQueryService;
        structurePage();
        setupStyle();
        startPolling();
    }

    private void structurePage() {
        // Keep it: Method nesting maps DOM structure for maximum readability and maintainability
        add(
                getContainerWith(
                        getStageAsIFrame(),
                        getHudPanelWith(
                                getStatusesLineWith(
                                        createParagraph("This control panel represents the server and is provided by Vaadin."),
                                        createParagraph("You can control the /simulation."),
                                        createParagraph("It executes the same code that runs in the browser."),
                                        createParagraph("When the server simulation is running, it's easy to see that it runs much faster."),
                                        createParagraph("___"),
                                        createParagraph("Simulation run on server:"),
                                        getStatusDisplayParagraph()
                                ),
                                getButtonsLineWith(
                                        createActionButton("Start simulation", () -> {
                                            lifecycleService.start();
                                            logToConsole("Server simulation started");
                                        }),
                                        // TODO: implement GUI for user so they can set desired
                                        //  amount of the generations to be run
                                        //  instead of the "1", "10", "100" buttons
                                        createActionButton("Run next 1 generations", () -> {
                                            lifecycleService.runCertainGenerations(1);
                                            logToConsole("Simulate 1 next generations");
                                        }),
                                        createActionButton("Run next 10 generations", () -> {
                                            lifecycleService.runCertainGenerations(10);
                                            logToConsole("Simulate 10 next generations");
                                        }),
                                        createActionButton("Run next 100 generations", () -> {
                                            lifecycleService.runCertainGenerations(100);
                                            logToConsole("Simulate 100 next generations");
                                        }),
                                        createActionButton("Stop simulation", () -> {
                                            lifecycleService.stop();
                                            logToConsole("Server simulation stopped");
                                        }),
                                        createActionButton("Reset simulation", () -> {
                                            lifecycleService.reset();
                                            logToConsole("Reset simulation");
                                        })
                                )
                        )
                )
        );
    }

    private @NonNull IFrame getStageAsIFrame() {
        final var iframe = new IFrame(STAGE_URL);
        iframe.getElement().setAttribute("frameborder", "0");
        iframe.addClassName(CLASS_FULL_SIZE);
        iframe.getStyle().set("z-index", "1");
        return iframe;
    }

    private @NonNull VerticalLayout getHudPanelWith(
            @NonNull VerticalLayout statusesLine, @NonNull HorizontalLayout buttonsLine) {
        final var hudPanel = new VerticalLayout(statusesLine, buttonsLine);
        hudPanel.setSpacing(false);
        hudPanel.setPadding(false);
        hudPanel.addClassName(CLASS_HUD_PANEL);
        return hudPanel;
    }

    private @NonNull VerticalLayout getStatusesLineWith(@NonNull Component... components) {
        final var statusesLine = new VerticalLayout(components);
        statusesLine.setSpacing(false);
        statusesLine.setPadding(false);
        statusesLine.addClassName(CLASS_STATUS_LINE);
        return statusesLine;
    }

    private @NonNull HorizontalLayout getButtonsLineWith(@NonNull Button... buttons) {
        HorizontalLayout buttonsLine = new HorizontalLayout(buttons);
        buttonsLine.setSpacing(false);
        buttonsLine.setPadding(false);
        buttonsLine.addClassName(CLASS_BUTTONS_LINE);
        return buttonsLine;
    }

    private @NonNull Div getContainerWith(@NonNull IFrame iFrame, @NonNull VerticalLayout hudPanel) {
        Div container = new Div(iFrame, hudPanel);
        container.addClassName(CLASS_CONTAINER);
        return container;
    }

    private @NonNull Paragraph createParagraph(String text) {
        final var paragraph = new Paragraph(text);
        paragraph.addClassName(CLASS_TEXT_COMMON);
        return paragraph;
    }

    private @NonNull Paragraph getStatusDisplayParagraph() {
        this.statusDisplay = new Paragraph("---");
        statusDisplay.addClassName(CLASS_TEXT_COMMON);
        return statusDisplay;
    }

    private @NonNull Button createActionButton(String label, @NonNull Runnable action) {
        Button button = new Button(label, event -> action.run());
        button.getStyle().set("margin", "0").set("font-size", "0.9rem");
        return button;
    }

    private void logToConsole(String message) {
        UI.getCurrent().getPage().executeJs(String.format("console.log('%s');", message));
    }

    private void setupStyle() {
        addClassName(CLASS_FULL_SIZE);
    }

    /**
     * Uses Vaadin's built-in client polling instead of a hand-rolled thread: poll
     * requests arrive on the UI session thread, so no {@code ui.access()} coordination
     * is needed, and a failed poll cannot permanently kill the update loop.
     */
    private void startPolling() {
        UI ui = UI.getCurrent();
        ui.setPollInterval(POLL_INTERVAL_MILLIS);
        ui.addPollListener(event -> {
            try {
                statusDisplay.setText(buildStatusText(genomeQueryService.getSpeciesStatistics()));
            } catch (Exception e) {
                log.warn("Failed to refresh simulation status", e);
            }
        });
    }

    /**
     * Presentation-only formatting of the persisted per-species statistics.
     * Fitness semantics: LOWER = BETTER (distance to the arena center);
     * {@code null} means "nothing evaluated" and is shown as N/A.
     */
    private String buildStatusText(@NonNull List<SpeciesStats> statsList) {
        if (statsList.isEmpty()) {
            return "No persisted generation yet — start the simulation.";
        }
        int totalSpecimens = 0;
        double bestOverall = Double.MAX_VALUE;
        double totalFitness = 0;
        int fitnessCount = 0;
        StringBuilder speciesLines = new StringBuilder();

        for (SpeciesStats stats : statsList) {
            totalSpecimens += stats.specimenCount();
            if (stats.bestFitness() != null && stats.bestFitness() < bestOverall) {
                bestOverall = stats.bestFitness();
            }
            if (stats.averageFitness() != null) {
                totalFitness += stats.averageFitness();
                fitnessCount++;
            }
            speciesLines.append("\n  Species[").append(stats.speciesIndex())
                    .append("]: specimens=").append(stats.specimenCount())
                    .append(", bestFitness=").append(formatFitness(stats.bestFitness()))
                    .append(", avgFitness=").append(formatFitness(stats.averageFitness()));
        }

        double avgOverall = fitnessCount > 0 ? totalFitness / fitnessCount : Double.MAX_VALUE;

        return "Persisted generation: species=" + statsList.size()
                + ", totalSpecimens=" + totalSpecimens
                + ", bestFitness=" + formatFitness(bestOverall == Double.MAX_VALUE ? null : bestOverall)
                + ", avgFitness=" + formatFitness(avgOverall == Double.MAX_VALUE ? null : avgOverall)
                + speciesLines;
    }

    private String formatFitness(Double fitness) {
        return fitness == null ? "N/A" : String.valueOf(Math.round(fitness * 100) / 100.0);
    }
}