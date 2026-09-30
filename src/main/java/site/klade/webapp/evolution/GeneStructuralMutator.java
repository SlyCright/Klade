package site.klade.webapp.evolution;

import site.klade.simulation.gene.Gene;

import java.util.List;
import java.util.Random;

/**
 * Handles structural mutations of genes (addition, deletion, replacement, swap, duplication, shift).
 * <p>
 * Structural mutations change the composition and ordering of genes in the genome.
 * <p>
 * Every operation follows the drift/replace split of the rank-linear operator
 * ({@code mutation_factor = rank × R_max}):
 * <ul>
 *   <li><b>REPLACE</b> (probability {@code mutation_factor}) — global change: the target position is
 *       drawn uniformly from the whole domain;</li>
 *   <li><b>DRIFT</b> (probability {@code 1 - mutation_factor}) — local change: the target position is
 *       drawn from the drift window around the rank-linearly selected index
 *       (see {@link #driftIndex}).</li>
 * </ul>
 * At {@code mutation_factor = 1.0} the REPLACE branch always wins, so drift and replace coincide;
 * the smaller the factor, the narrower the drift window, up to the exact selected index.
 */
public class GeneStructuralMutator {

    private final Random random = new Random();
    private final RandomIndexSelector indexSelector = new RandomIndexSelector();
    private final double deletionBias;

    /**
     * @param deletionBias extra probability of forcing a {@code DELETION} on top of the flat
     *                     mutation-type distribution (see {@code evolution.mutation.deletion-bias})
     */
    public GeneStructuralMutator(double deletionBias) {
        this.deletionBias = deletionBias;
    }

    /**
     * Types of structural mutations that can be applied to genes.
     */
    private enum StructuralMutation {
        DELETION,           // remove a gene from the list
        ADDITION,           // add a new gene to the list
        REPLACEMENT,        // replace a gene with a brand new one
        SWAPPING,           // swap positions of two genes
        DUPLICATION,        // duplicate an existing gene
        SHIFT               // shift a gene by N positions (rotate within list)
        // TODO: Future candidates for consideration:
        // SPLIT - split one gene into two genes
        // ADDITION_TO_THE_END - add a new gene to the end of the gene list
        // MERGE - merge two adjacent genes into one
        // TRANSPOSITION - move a gene from one position to another
        // INVERSION - reverse the order of a segment of genes
        // BLOCK_DELETION - delete multiple contiguous genes at once
        // BLOCK_ADDITION - add multiple new genes as a block
        // BLOCK_DUPLICATION - duplicate a block of genes
        // BLOCK_MOVE - move a block of contiguous genes to another position
    }

    /**
     * Performs structural mutations on genes.
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    public void mutate(List<Gene> genes, double mutationFactor) {
        int maxMutations = Math.max(1, (int) Math.round(genes.size() * mutationFactor));
        StructuralMutation[] mutations = StructuralMutation.values();

        for (int i = 0; i < maxMutations; i++) {
            if (random.nextDouble() < mutationFactor) {
                // Choose mutation operation with DELETION having higher probability
                StructuralMutation mutationType = mutations[random.nextInt(mutations.length)];
                if (random.nextDouble() < deletionBias) mutationType = StructuralMutation.DELETION;
                // Apply the selected mutation
                applyStructuralMutation(genes, mutationType, mutationFactor);
            }
        }
    }

    /**
     * Applies a specific structural mutation to genes.
     *
     * @param genes          the genes to mutate
     * @param mutationType   the type of structural mutation to apply
     * @param mutationFactor the mutation intensity
     */
    private void applyStructuralMutation(List<Gene> genes, StructuralMutation mutationType, double mutationFactor) {
        switch (mutationType) {
            case DELETION -> deleteGene(genes, mutationFactor);
            case ADDITION -> addGene(genes, mutationFactor);
            case REPLACEMENT -> replaceWithNew(genes, mutationFactor);
            case SWAPPING -> swapGenes(genes, mutationFactor);
            case DUPLICATION -> duplicateGene(genes, mutationFactor);
            case SHIFT -> shiftGene(genes, mutationFactor);
        }
    }

    /**
     * Deletes one gene.
     *
     * <p>Drift: the selected gene or one of its closest neighbours — the victim is drawn from the
     * drift window around the selected index (see {@link #driftIndex}).
     * Replace: delete any random gene (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void deleteGene(List<Gene> genes, double mutationFactor) {
        if (genes.isEmpty()) return;

        boolean isReplace = random.nextDouble() < mutationFactor;
        int index;
        if (isReplace) {
            // Replace: any random gene (plain distribution)
            index = indexSelector.pickUniformIndex(genes.size());
        } else {
            // Drift: the selected gene, or a closest neighbour inside the drift window.
            // Window collapses to the selected index at mutationFactor = 0.0 (the exact gene is
            // deleted) and widens towards the whole genome as mutationFactor grows to 1.0.
            int selectedIndex = indexSelector.pickLinearWeightedIndex(genes.size());
            index = driftIndex(selectedIndex, genes.size(), mutationFactor);
        }
        genes.remove(index);
    }

    /**
     * Adds one gene.
     *
     * <p>Drift: add a new gene into a gap inside the drift window around the selected position
     * (local change) — the insertion domain is the set of gaps {@code 0 .. genes.size()}.
     * Replace: add into any random gap (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void addGene(List<Gene> genes, double mutationFactor) {
        if (genes.isEmpty()) {
            // TODO: ADDITION needs a brand-new gene (random parameters, ID generated between the
            //  neighbouring IDs and respecting tombstones) — still an open design question,
            //  so ADDITION is a no-op while the gene list is empty.
            return;
        }

        boolean isReplace = random.nextDouble() < mutationFactor;
        int insertIndex;
        if (isReplace) {
            // Replace: any random gap (plain distribution)
            insertIndex = random.nextInt(genes.size() + 1);
        } else {
            // Drift: a gap inside the drift window around the selected gene
            int selectedIndex = indexSelector.pickLinearWeightedIndex(genes.size());
            insertIndex = driftIndex(selectedIndex, genes.size() + 1, mutationFactor);
        }
        // TODO: Create a new gene with random parameters
//        Gene newGene = new Gene();
        int sourceIndex = Math.min(insertIndex, genes.size() - 1);
        genes.add(insertIndex, genes.get(sourceIndex));
    }

    /**
     * Replaces a gene with a brand new one.
     *
     * <p>Drift: the selected gene or one of its closest neighbours (local change).
     * Replace: any random gene (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void replaceWithNew(List<Gene> genes, double mutationFactor) {
        if (genes.isEmpty()) return;

        boolean isReplace = random.nextDouble() < mutationFactor;
        int index;
        if (isReplace) {
            // Replace: any random gene (plain distribution)
            index = indexSelector.pickUniformIndex(genes.size());
        } else {
            // Drift: the selected gene, or a closest neighbour inside the drift window
            int selectedIndex = indexSelector.pickLinearWeightedIndex(genes.size());
            index = driftIndex(selectedIndex, genes.size(), mutationFactor);
        }
        // TODO: Replace with a new gene with random parameters here
//        Gene newGene = new Gene();
        genes.set(index, genes.get(index));
    }

    /**
     * Swaps positions of two genes.
     *
     * <p>Drift: swap the selected gene with a gene from its drift window (local change) — no partner
     * is forced, so a collapsed window makes the swap a no-op, which is the intended
     * "small factor = small change" behaviour.
     * Replace: swap with any random gene (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void swapGenes(List<Gene> genes, double mutationFactor) {
        if (genes.size() < 2) return;

        int index1 = indexSelector.pickLinearWeightedIndex(genes.size());
        int index2;
        boolean isReplace = random.nextDouble() < mutationFactor;
        if (isReplace) {
            // Replace: any random gene (plain distribution)
            index2 = indexSelector.pickUniformIndex(genes.size());
        } else {
            // Drift: swap with a gene inside the drift window around the selected one
            index2 = driftIndex(index1, genes.size(), mutationFactor);
        }
        if (index1 == index2) return;
        // Swap
        Gene temp = genes.get(index1);
        genes.set(index1, genes.get(index2));
        genes.set(index2, temp);
    }

    /**
     * Duplicates an existing gene.
     *
     * <p>Drift: duplicate the selected gene into a gap inside its drift window (local change).
     * Replace: duplicate into any random gap (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void duplicateGene(List<Gene> genes, double mutationFactor) {
        if (genes.isEmpty()) return;

        int sourceIndex = indexSelector.pickLinearWeightedIndex(genes.size());
        int insertIndex;
        boolean isReplace = random.nextDouble() < mutationFactor;
        if (isReplace) {
            // Replace: any random gap (plain distribution)
            insertIndex = random.nextInt(genes.size() + 1);
        } else {
            // Drift: a gap inside the drift window around the source gene
            insertIndex = driftIndex(sourceIndex, genes.size() + 1, mutationFactor);
        }
        // Duplicate
        Gene duplicated = new Gene(genes.get(sourceIndex));
        genes.add(insertIndex, duplicated);
    }

    /**
     * Shifts a gene by N positions.
     *
     * <p>Drift: shift to a position inside the drift window around the selected gene (local change).
     * Replace: shift to any random position (global change).</p>
     *
     * @param genes          the genes to mutate
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     */
    private void shiftGene(List<Gene> genes, double mutationFactor) {
        if (genes.size() < 2) return;

        int fromIndex = indexSelector.pickLinearWeightedIndex(genes.size());
        int toIndex;
        boolean isReplace = random.nextDouble() < mutationFactor;
        if (isReplace) {
            // Replace: any random position (plain distribution)
            toIndex = indexSelector.pickUniformIndex(genes.size());
        } else {
            // Drift: a position inside the drift window around the selected gene
            toIndex = driftIndex(fromIndex, genes.size(), mutationFactor);
        }
        if (fromIndex == toIndex) return;
        // Shift (remove and insert at new position)
        Gene gene = genes.remove(fromIndex);
        genes.add(toIndex, gene);
    }

    /**
     * Picks an index inside the drift window around the selected (rank-linear) index.
     *
     * <p>Drift must be a <b>local</b> change while replace is a global one. Following the operator
     * rule {@code drift_amplitude = mutation_factor × base_range}, where the index domain
     * {@code 0 .. domainSize-1} has {@code base_range = domainSize - 1}, the window is built as:</p>
     *
     * <pre>
     * drift_amplitude = mutation_factor × (domainSize - 1)   // number of "neighbour" steps
     * candidateCount  = round(drift_amplitude) + 1           // + the selected index itself
     * </pre>
     *
     * <p>The window contains <b>exactly</b> {@code candidateCount} positions: it is centred on the
     * anchor and, when it would stick out of the domain, it is <b>shifted</b> back inside instead of
     * being clipped, so the count never shrinks at the edges. Therefore:</p>
     *
     * <ul>
     *   <li>{@code mutationFactor = 0.0} → 1 candidate: exactly the selected index
     *       ("tiny factor — tiny change");</li>
     *   <li>{@code mutationFactor = 1.0} → {@code domainSize} candidates, i.e. the whole domain for
     *       <b>every</b> anchor, so drift and replace describe the same distribution;</li>
     *   <li>example — {@code domainSize = 10}, {@code mutationFactor = 0.9}:
     *       {@code drift_amplitude = 0.9 × 9 = 8.1 → round → 8}, so {@code candidateCount = 9};
     *       for the last index (9) the window is {@code [1..9]}, for the first (0) it is
     *       {@code [0..8]}.</li>
     * </ul>
     *
     * @param anchor         the index selected with the rank-linear (linear weighted) distribution
     * @param domainSize     number of valid positions in the target domain — genes for in-place
     *                       operations, or gaps ({@code genes.size() + 1}) for insertions
     * @param mutationFactor the mutation intensity (0.0 = no mutation, 1.0 = maximum mutation)
     * @return an index from the drift window, always inside {@code [0, domainSize - 1]}
     */
    private int driftIndex(int anchor, int domainSize, double mutationFactor) {
        int candidateCount = (int) Math.round(mutationFactor * (domainSize - 1)) + 1;
        candidateCount = Math.max(1, Math.min(domainSize, candidateCount));
        // Centre the window on the anchor, then shift (not clip) it back into the domain, so the
        // candidate count stays exact even for anchors near the edges.
        int lowerBound = anchor - candidateCount / 2;
        if (lowerBound < 0) lowerBound = 0;
        if (lowerBound + candidateCount > domainSize) lowerBound = domainSize - candidateCount;
        return lowerBound + random.nextInt(candidateCount);
    }
}
