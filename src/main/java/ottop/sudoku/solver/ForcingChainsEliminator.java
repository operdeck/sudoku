package ottop.sudoku.solver;

import org.jetbrains.annotations.NotNull;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.ForcingChainsReason;
import ottop.sudoku.puzzle.ISudoku;

import java.text.MessageFormat;
import java.util.*;

public class ForcingChainsEliminator extends Eliminator {

    class ForcingChain {
        Coord startingPoint;
        List<Step>[] chains;
        String[] startingSymbols;

        ForcingChain(Coord c) {
            startingPoint = c;
            Set<Integer> candidates = candidatesPerCell.get(c);
            chains = new List[candidates.size()];
            startingSymbols = new String[candidates.size()];
            int i=0;
            for (Integer candidate: candidates) {
                String symbol = myPuzzle.symbolCodeToSymbol(candidate);
                startingSymbols[i] = symbol;
                chains[i] = new ArrayList<>();
                chains[i].add(new MoveStep(chains[i], null, c, symbol));
                i++;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Forcing Chain starting at " + startingPoint + "\n");
            for (int i=0; i< chains.length; i++) {
                sb.append(getSubChainStrings(String.valueOf(startingPoint), chains[i].get(0), null));
                //sb.append("   chain " + i + ": ").append(String.valueOf(chains[i])).append("\n");
            }
            return sb.toString();
        }

        private String getSubChainStrings(String preamble, Step step, Set<Step> conclusions) {
            StringBuilder sb = new StringBuilder();
            if (step.children == null || step.children.size() == 0) {
                if (conclusions == null || conclusions.contains(step)) {
                    sb.append(preamble).append("->").append(step).append("\n");
                }
            } else {
                for (Step child : step.children) {
                    sb.append(getSubChainStrings(preamble + "->" + step, child, conclusions));
                }
            }
            return sb.toString();
        }

        private boolean nextConclusion(int maxDepth) {
            boolean hasReachedNextConclusion = false;
            while (!hasReachedNextConclusion) {
                Step s = findNextStep();
                if (s == null) return false;
                if (s.level + 1 > maxDepth) {
//                    System.out.println("Bail out as s level is " + s.level + " and max depth = " + maxDepth);
                    return false;
                }
                // TODO if equal then we shoud prefer the one with total smaller size but can only do that later
//                System.out.println("Level next step: " + s.level);
                hasReachedNextConclusion = s.addConclusions();
            }
            return hasReachedNextConclusion;
        }

        private Step findNextStep() {
            Step bestStep = null;
            for (int i=0; i<chains.length; i++) {
                for (Step aStep: chains[i]) {
                    if (!aStep.isResolved) {
                        if (bestStep == null || aStep.level < bestStep.level) {
                            bestStep = aStep;
                        }
                    }
                }
            }
            return bestStep;
        }

        private Set<Step> getSharedConclusions() {
            // find overlap between all chains
            Set<Step> shared = new HashSet<>(chains[0]);
            for (int i=1; i<chains.length; i++) {
                shared.retainAll(chains[i]);
            }
            return shared;
        }


        private List<Coord> getSequenceLeadingUpTo(Step step) {
            List<Coord> sequence = new ArrayList<>();
            if (step.parent != null) {
                sequence.addAll(getSequenceLeadingUpTo(step.parent));
            }
            if (step instanceof EliminationStep) {
                sequence.add(step.coord);
            }
            return sequence;
        }

        // This is the main loop. Advance the steps in all "chains"
        // until we have a set of shared conclusions - so eliminations/moves
        // that occur in all chains.
        private List<ForcingChainsReason> findChains(int maxDepth) {
            Set<Step> shared = null;
            while (shared == null || shared.size() == 0) {
                if (nextConclusion(maxDepth)) {
                    shared = getSharedConclusions();
                } else {
                    break;
                }
            }
            if (shared == null) return null;

            // When we have shared conclusions, turn those into an explanation
            // that targets the elimination of one symbol in one cell (or TODO multiple if
            // they all share the same parent).

            // Assuming: all shared results are Eliminations
            boolean allAreEliminations = shared.stream().allMatch(s -> (s instanceof EliminationStep));
            if (!allAreEliminations) {
                System.out.println("Expected all conclusions to be eliminations but not all of them are: " + shared);
                System.out.println(this);
                return null;
            }

            // Each conclusion will get its own resulting explanation separately, as we
            // cannot assume all conclusions always share the same parent.
            List<ForcingChainsReason> results = new ArrayList<>();
            for (Step sharedConclusion: shared) {
                ForcingChainsReason fcr =
                        new ForcingChainsReason(sharedConclusion.symbol, startingPoint, sharedConclusion.coord);
                int currentChainMaxLevel = -1;
                for (int i=0; i<chains.length; i++) {
                    List<Coord> stepChain = null;
                    for (Step s: chains[i]) {
                        if (s.equals(sharedConclusion)) {
                            if (s.level > currentChainMaxLevel) currentChainMaxLevel = s.level;
                            List<Coord> currentStepChain = getSequenceLeadingUpTo(s.parent);
                            if (stepChain == null || currentStepChain.size() < stepChain.size()) {
                                stepChain = currentStepChain;
                            }
                        }
                    }
                    assert (stepChain != null);
                    fcr.addChain(startingSymbols[i], stepChain);
                }
                fcr.setChainDepth(currentChainMaxLevel);
                results.add(fcr);
            }

            return results;
        }

    }

    // TODO: use more broadly? Coord + symbol
    class Step{
        Step parent;
        List<Step> children = new ArrayList<>();
        boolean isResolved = false;
        int level = 0;
        Coord coord;
        String symbol;
        List<Step> myContainer;

        Step(List<Step> container, Step parent, Coord coord, String symbol) {
            this.myContainer = container;
            this.parent = parent;
            this.coord = coord;
            this.symbol = symbol;
            if (parent != null) {
                level = 1+parent.level;
                parent.children.add(this);
            }
        }
        boolean isInParentChain(Coord buddy) {
            if (parent == null) return false;
            if (parent.coord.equals(buddy)) return true;
            return parent.isInParentChain(buddy);
        }
        @Override
        public int hashCode() {
            return coord.hashCode() ^ symbol.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj.getClass().equals(getClass())) {
                return ((Step) obj).coord.equals(coord) && ((Step) obj).symbol.equals(symbol);
            }
            return super.equals(obj);
        }

        boolean addConclusions() { return false; }
        public String toString() {
            return symbol+"@"+coord;
        }
    }
    class MoveStep extends Step {
        MoveStep(List<Step> container, Step parent, Coord c, String symbol) {
            super(container, parent, c, symbol);
        }
        public boolean addConclusions() {
            boolean hasAdded = false;
            int symbolCode = myPuzzle.symbolToSymbolCode(symbol);
            for (Coord buddy: myPuzzle.getBuddies(coord)) {
                if (!myPuzzle.isOccupied(buddy) && !isInParentChain(buddy)) {
                    // see if buddy is already in the list
                    // if buddy is a move that would be inconsistent?
                    // if an elimination we could combine but perhaps that goes too far
                    if (myContainer.contains(buddy)) System.out.println("** buddy already present: " + buddy);

                    Set<Integer> buddyCandidates = candidatesPerCell.get(buddy);
                    if (buddyCandidates.contains(symbolCode)) {
                        if (myContainer.add(new EliminationStep(myContainer, this, buddy, symbol))) {
                            hasAdded = true;
                            // A second move on the same coord with different symbol would be inconsistent
                            // A second elimination on the same coord could be valid although strange
                        }
                    }
                }
            }
            isResolved = true;
            return hasAdded;
        }

        public String toString() {
            return "M:"+super.toString();
        }

    }

    class EliminationStep extends Step {
        EliminationStep(List<Step> container, Step parent, Coord c, String symbol)
        {
            super(container, parent, c, symbol);
        }
        public boolean addConclusions() {
            // avoid the same cells of any of your parents
            boolean hasAdded = false;
            int symbolCode = myPuzzle.symbolToSymbolCode(symbol);
            Set<Integer> candidates = new TreeSet<>(candidatesPerCell.get(coord));
            if (candidates.size() == 2) {
                if (candidates.remove(symbolCode)) {
                    // Naked single
                    String loneNumber = myPuzzle.symbolCodeToSymbol(candidates.iterator().next());
                    if (myContainer.add(new MoveStep(myContainer, this, coord, loneNumber))) {
                        hasAdded = true;
                    }
                }
            }
            // TODO consider unique value logic as well
            isResolved = true;
            return hasAdded;
        }
        public String toString() {
            return "E:"+super.toString();
        }
    }

    ForcingChainsEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() {
//        System.out.println("Forcing chains...");

        // TODO find start by finding cells with 2 then 3 etc possibilities
        // TODO or just try all and use the one with smallest total conclusions but > 0
//        Coord start = new Coord("r9c2");
//        Coord start = new Coord("r1c2");

        ForcingChainsReason bestFc = null;
        Map<Step, ForcingChainsReason> remainingFcs = new HashMap<>();
        for (Coord start: myPuzzle.getAllCells()) {
            if (!myPuzzle.isOccupied(start)) {
                // Find all chains starting at 'start' but early stop at depth > current best depth
                // Opportunity here to start with cell with only 2 or 3 candidates
                // TODO or max depth to 5 or so..

                ForcingChain fc = new ForcingChain(start);
                int maxDepth = bestFc == null ? Integer.MAX_VALUE : bestFc.getChainDepth();

                List<ForcingChainsReason> fcrs = fc.findChains(maxDepth);

                if (fcrs != null && fcrs.size() > 0) {
                    for (ForcingChainsReason fcr: fcrs) {
                        Coord removal = fcr.getRemovedFrom().iterator().next();
                        Step mv = new Step(null, null, removal, fcr.getSymbol());
                        if (fcr.compareTo(remainingFcs.get(mv)) < 0) {
                            remainingFcs.put(mv, fcr); // Better one at same cell/symbol
                        }

                        System.out.println(new StringBuilder().append("Forcing chain size ")
                                .append(fcr.getTotalChainLength()).append("/depth ").append(fcr.getChainDepth())
                                .append(" found at ").append(start)
                                .append(":").toString());
                        System.out.println(MessageFormat.format("   {0}", fcr));

                        if (bestFc == null ||
                                (fcr.getChainDepth() < bestFc.getChainDepth()) ||
                                ((fcr.getChainDepth() == bestFc.getChainDepth()) && (fcr.getTotalChainLength() < bestFc.getTotalChainLength()))) {
                            bestFc = fcr;
                        }
                    }
                } else {
//                    System.out.println("No forcing chain found from " + start);
                }
            }
        }

        // TODO: consider adding siblings of the "removed from" set. Same parent,
        // same symbol elimination but different cells.

        System.out.println("Found " + remainingFcs.size() + " chains:");
        System.out.println("   keys: " + remainingFcs.keySet());
        for (ForcingChainsReason x: remainingFcs.values()) {
            System.out.println("   " + x);
        }
        System.out.println("Best FC: " + bestFc);

        // eliminate in puzzle
        boolean hasRemoved = false;
        if (bestFc != null) {
            for (ForcingChainsReason x: remainingFcs.values()) {
                for (Coord c : x.getRemovedFrom()) {
                    if (removePossibility(myPuzzle.symbolToSymbolCode(x.getSymbol()), x.getRemovedFrom(), x))
                        hasRemoved = true;
                }
            }
        }

        return hasRemoved;
    }
}