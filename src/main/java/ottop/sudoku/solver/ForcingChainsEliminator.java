package ottop.sudoku.solver;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.ForcingChainsReason;
import ottop.sudoku.puzzle.ISudoku;

import java.text.MessageFormat;
import java.util.*;


// forcing chain forces a move
// http://www.sadmansoftware.com/sudoku/forcingchain.php
// xyz wing forces an elimination
// http://www.sadmansoftware.com/sudoku/xyzwing.php
// both if/then style

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
                chains[i].add(new NakedSingleMoveStep(chains[i], null, c, symbol));
                i++;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder().append("Forcing Chain starting at ").
                    append(startingPoint).append(" candidates: ").append(Arrays.toString(startingSymbols)).append("\n");
            for (int i=0; i< chains.length; i++) {
                sb.append(getSubChainStrings("   "+startingSymbols[i] +"@"+ startingPoint,
                        chains[i].get(0), null));
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

//        private List<Coord> getSequenceLeadingUpTo___old(Step step) {
//            List<Coord> sequence = new ArrayList<>();
//            if (step.parent != null) {
//                sequence.addAll(getSequenceLeadingUpTo(step.parent));
//            }
//            if (step instanceof EliminationStep) {
//                sequence.add(step.coord);
//            }
//            return sequence;
//        }

        private List<Step> getSequenceLeadingUpTo(Step step) {
            List<Step> sequence = new ArrayList<>();
            if (step.parent != null) {
                sequence.addAll(getSequenceLeadingUpTo(step.parent));
            }
            sequence.add(step);
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
                // This should not be happening
                System.out.println("**UNEXPECTED** All conclusions should be eliminations. Shared conclusions: " + shared);
                System.out.println(this);
//                if (shared.size()==2) {
//                    System.out.println(String.valueOf(myPuzzle));
//                }
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
                    List<Step> stepChain = null;
                    for (Step s: chains[i]) {
                        if (s.equals(sharedConclusion)) {
                            if (s.level > currentChainMaxLevel) currentChainMaxLevel = s.level;
                            List<Step> currentStepChain = getSequenceLeadingUpTo(s); // s.parent to exclude last
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
    public class Step{
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
        public Coord getCoord() { return coord; }

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

    abstract class MoveStep extends Step {
        MoveStep(List<Step> container, Step parent, Coord c, String symbol) {
            super(container, parent, c, symbol);
        }
        boolean addConclusions() {
            boolean hasAdded = false;
            int symbolCode = myPuzzle.symbolToSymbolCode(symbol);
            for (Coord buddy: myPuzzle.getBuddies(coord)) {
                if (!myPuzzle.isOccupied(buddy) && !isInParentChain(buddy)) {
                    // see if buddy is already in the list
                    // if buddy is a move that would be inconsistent?
                    // if an elimination we could combine but perhaps that goes too far
                    if (myContainer.contains(buddy)) {
                        System.out.println("**UNEXPECTED** buddy " + buddy + " already present in " + this);
                    }

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
    }

    class NakedSingleMoveStep extends MoveStep {
        NakedSingleMoveStep(List<Step> container, Step parent, Coord c, String symbol) {
            super(container, parent, c, symbol);
        }
        public String toString() {
            return "S:"+super.toString();
        }
    }

    class UniqueValueMoveStep extends MoveStep {
        private final AbstractGroup uniqueInGroup;

        UniqueValueMoveStep(List<Step> container, Step parent, Coord c, String symbol, AbstractGroup uniqueInGroup) {
            super(container, parent, c, symbol);
            this.uniqueInGroup = uniqueInGroup;
        }
        public String toString() {
            return "U("+uniqueInGroup+"):"+super.toString();
        }
    }

    class EliminationStep extends Step {
        EliminationStep(List<Step> container, Step parent, Coord c, String symbol)
        {
            super(container, parent, c, symbol);
        }
        boolean addConclusions() {
            boolean hasAdded = false;

            // Check # of candidates in current cell
            // removing any eliminations in this cell by the parent chain
            // TODO: instead of just applying the parent chain, in principle we
            // could also apply any eliminations from the same chain. However this
            // would become rather difficult to understand. We would also have to
            // include those in the explanations.
            Set<Integer> candidates = new TreeSet<>(candidatesPerCell.get(coord)); // is a copy of the candidates
            Step p = this;
            while (p != null) {
                // For longer chains there could be multiple eliminations in
                // the same cell. But unfortunately this does not happen often.
                if (p.coord.equals(this.coord)) {
                    candidates.remove(myPuzzle.symbolToSymbolCode(p.symbol));
                }
                p = p.parent;
            }

            // Check for NO candidates. If that happens this chain is resulting
            // in an inconsistent state so should be dismissed.
            if (candidates.size() == 0) {
                //System.out.println("***** INCONSISTENT **** nothing remaining at " + coord);
                isResolved = true;
                return false;
            }

            // Naked single if one candidate remaining
            if (candidates.size() == 1) {
                String loneNumber = myPuzzle.symbolCodeToSymbol(candidates.stream().findAny().get());
                if (myContainer.add(new NakedSingleMoveStep(myContainer, this, coord, loneNumber))) {
                    hasAdded = true;
                }
            }

            // Now check for lone symbols
            if (!hasAdded) {
                int symbolCode = myPuzzle.symbolToSymbolCode(symbol);

                for (AbstractGroup buddyGrp : myPuzzle.getBuddyGroups(coord)) {

                    // For a buddy group see where the current symbol is a possibility. Start with
                    // the possibilities remaining from the current puzzle. Exclude the current cell
                    // as the elimination is there. But also eliminate any cells from the parent chain
                    // that eliminate the symbol and are in this same group.

                    Set<Coord> possibilities = new HashSet<>();
                    for (Coord c : buddyGrp.getCoords()) {
                        if (!c.equals(coord)) { // it is eliminated at this coord so not adding as a possibility
                            if (buddyGrp.isPossibility(symbolCode, c)) {
                                possibilities.add(c);
                            }
                        }
                    }

                    // TODO: this is incomplete.. We should consider the full chain I'm afraid
                    // because just checking the parent chain may miss out on sibling eliminations.

                    // TODO may work, see tour 10 removal 8 from r4c4...
                    // however now only at 42/95 in the magic tour, only 1 extra...

                    // See if there is a parent that is in the same buddy group and that
                    // eliminates the same symbol
//                    p = this.parent;
//                    while (p != null) {
//                        if (p.symbol.equals(this.symbol) && buddyGrp.isInGroup(p.coord)) {
//                            possibilities.remove(p.coord);
//                        }
//                        p = p.parent;
//                    }

                    for (Step s : myContainer) {
                        if (s.symbol.equals(this.symbol) && buddyGrp.isInGroup(s.coord)) {
                            possibilities.remove(s.coord);
                        }
                    }


                    // TODO: this SEEMS to increase magic tour to 46/95 from 41/95.
                    // TODO: double check this carefully
                    // TODO: hey we lost one, now 45...

                    // We have only 1 possibility for "symbolCode" in this buddy group
                    if (possibilities.size() == 1) {
                        //                        System.out.println("Unique symbol " + symbol + " (eliminated at " + coord +
                        //                                ") in " + buddyGrp + ": " + possibilities);

                        // Unique symbol
                        if (myContainer.add(new UniqueValueMoveStep(myContainer, this,
                                possibilities.stream().findAny().get(), symbol,
                                buddyGrp))) {
                            hasAdded = true;
                        }
                    }
                }
            }

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

        ForcingChainsReason bestFc = null;
        Map<Step, ForcingChainsReason> remainingFcs = new HashMap<>();
        for (Coord start: myPuzzle.getAllCells()) {
            if (!myPuzzle.isOccupied(start) && candidatesPerCell.get(start).size()>1) {
                // Find all chains starting at 'start' but early stop at depth > current best depth
                // Opportunity here to start with cell with only 2 or 3 candidates
                // TODO or max depth to 5 or so..

                ForcingChain fc = new ForcingChain(start);
                int maxDepth = bestFc == null ? Integer.MAX_VALUE : bestFc.getChainDepth();

                List<ForcingChainsReason> fcrs = fc.findChains(maxDepth);

//                System.out.println("\n"+fc);
//                System.out.println("Results in " + (fcrs==null?0:fcrs.size()) + " possible chains");

                if (fcrs != null && fcrs.size() > 0) {
                    for (ForcingChainsReason fcr: fcrs) {
                        Coord removal = fcr.getRemovedFrom().stream().findAny().get();
                        Step mv = new Step(null, null, removal, fcr.getSymbol());
                        if (fcr.compareTo(remainingFcs.get(mv)) < 0) {
                            remainingFcs.put(mv, fcr); // Better one at same cell/symbol
                        }

                        if (verbose) {
                            System.out.println(new StringBuilder().append("Forcing chains size ")
                                    .append(fcr.getTotalChainLength()).append("/depth ").append(fcr.getChainDepth())
                                    .append(" found at ").append(start)
                                    .append(":").toString());
                            System.out.println(MessageFormat.format("   {0}", fcr));
                        }

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

        if (verbose) {
            System.out.println("Found " + remainingFcs.size() + " chains:");
            System.out.println("   keys: " + remainingFcs.keySet());
            for (ForcingChainsReason x : remainingFcs.values()) {
                System.out.println("   " + x);
            }
            System.out.println("Best FC: " + bestFc);
        }

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