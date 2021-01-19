package ottop.sudoku.solver;

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public class ForcingChainsEliminator extends Eliminator {

    class ForcingChain {
        Coord startingPoint;
        ArrayList<Step>[] chains;

        ForcingChain(Coord c) {
            startingPoint = c;
            Set<Integer> candidates = candidatesPerCell.get(c);
            chains = new ArrayList[candidates.size()];
            int i=0;
            for (Integer candidate: candidates) {
                String symbol = myPuzzle.symbolCodeToSymbol(candidate);
                chains[i] = new ArrayList<>();
                chains[i].add(new MoveStep(chains[i], null, c, symbol));
                i++;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Forcing Chain starting at " + startingPoint + "\n");
            for (int i=0; i< chains.length; i++) {
                sb.append("   chain " + i + ": ").append(String.valueOf(chains[i])).append("\n");
            }
            return sb.toString();
        }

        public boolean nextConclusion() {
            boolean hasReachedNextConclusion = false;
            while (!hasReachedNextConclusion) {
                Step s = findNextStep();
                if (s == null) return false;
                hasReachedNextConclusion = s.addConclusions();
            }
            return hasReachedNextConclusion;
        }

        private Step findNextStep() {
            Step bestStep = null;
            for (int i=0; i<chains.length; i++) {
                for (int j=0; j<chains[i].size(); j++) {
                    Step aStep = chains[i].get(j);
                    if (!aStep.isResolved) {
                        if (bestStep == null || aStep.level < bestStep.level) {
                            bestStep = aStep;
                        }
                    }
                }
            }
            return bestStep;
        }

        public Set<Step> getSharedConclusions() {
            // find overlap between all chains
            Set<Step> shared = new HashSet<>(chains[0]);
            for (int i=1; i<chains.length; i++) {
                shared.retainAll(chains[i]);
            }
            return shared;
        }
    }

    abstract class Step {
        Step parent;
        boolean isResolved = false;
        int level = 0;
        Coord coord;
        String symbol;
        ArrayList<Step> myContainer;

        Step(ArrayList<Step> container, Step parent, Coord coord, String symbol) {
            this.myContainer = container;
            this.parent = parent;
            this.coord = coord;
            this.symbol = symbol;
            if (parent != null) level = 1+parent.level;
        }
        abstract boolean addConclusions();
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

        String getPathAsString() {
            StringBuilder sb = new StringBuilder();
            if (parent != null) {
                sb.append(parent.getPathAsString()).append("-->");
            }
            sb.append(this);
            return sb.toString();
        }
    }
    class MoveStep extends Step {
        MoveStep(ArrayList<Step> container, Step parent, Coord c, String symbol) {
            super(container, parent, c, symbol);
        }
        public boolean addConclusions() {
            boolean hasAdded = false;
            int symbolCode = myPuzzle.symbolToSymbolCode(symbol);
            for (Coord buddy: myPuzzle.getBuddies(coord)) {
                if (!myPuzzle.isOccupied(buddy) && !isInParentChain(buddy)) {
                    Set<Integer> buddyCandidates = candidatesPerCell.get(buddy);
                    if (buddyCandidates.contains(symbolCode)) {
                        // TODO: what happens if the step is already there? Same coordinates perhaps different action.
                        myContainer.add(new EliminationStep(myContainer, this, buddy, symbol));
                        hasAdded = true;
                    }
                }
            }
            isResolved = true;
            return hasAdded;
        }

        public String toString() {
            return "M: "+symbol+"@"+coord+"{"+level+"/"+isResolved+"}";
        }
    }
    class EliminationStep extends Step {
        EliminationStep(ArrayList<Step> container, Step parent, Coord c, String symbol)
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
                    // TODO: what happens if the step is already there? Same coordinates perhaps different action.
                    myContainer.add(new MoveStep(myContainer, this, coord, loneNumber));
                    hasAdded = true;
                }
            }
            // TODO consider unique value logic as well
            isResolved = true;
            return hasAdded;
        }
        public String toString() {
            return "E: "+symbol+"@"+coord+"{"+level+"/"+isResolved+"}";
        }
    }

    ForcingChainsEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() {
        System.out.println("Forcing chains...");

        // TODO find start by finding cells with 2 then 3 etc possibilities
        Coord start = new Coord("r2c1");

        ForcingChain fc = new ForcingChain(start);
        System.out.println(String.valueOf(fc));

        // Too slow, currently only at level 9
        // TODO: avoid duplicates
        // TODO: implement equals and/or hash so we should find the overlap
        Set<Step> shared = null;
        while (true) {
            boolean hasNext = fc.nextConclusion();
            System.out.println(String.valueOf(fc));
            if (hasNext) {
                // see if there is any overlap between the chains
                shared = fc.getSharedConclusions();
                if (shared.size() > 0) {
                    break;
                }
            } else {
                break;
            }
        }
        System.out.println("Shared: " + String.valueOf(shared));
        for (Step s: shared) {
            System.out.println("Path: " + s.getPathAsString());
        }




        // Old...

        List<Integer> candidates = new ArrayList<>(candidatesPerCell.get(start));
        System.out.println(candidates);

        ISudoku[] futures = new ISudoku[candidates.size()];
        SudokuSolver[] solvers = new SudokuSolver[candidates.size()];

        ArrayList<Step> [] steps = new ArrayList[candidates.size()];

        for (int i=0; i<candidates.size(); i++) {
            futures[i] = myPuzzle.clone();
            solvers[i] = new SudokuSolver(futures[i]);
            solvers[i].setSimplest();
            futures[i].doMove(start, myPuzzle.symbolCodeToSymbol(candidates.get(i)));
            System.out.println("Future " + i);
            System.out.println(futures[i]);

        }



        // Progress all futures 1 step at a time
        boolean haveForcedMoves = true;
        while (haveForcedMoves) {
            for (int i = 0; haveForcedMoves && i < candidates.size(); i++) {
                Map.Entry<Coord, String> move = solvers[i].nextMove(new SolveStats());
                // TODO: iterate over all possible moves, each gives a fork

                System.out.println("Future " + i + " next move " + move);
                if (move == null) {
                    haveForcedMoves = false;
                } else {
                    futures[i].doMove(move.getKey(), move.getValue());
                }
            }
            Map.Entry<Coord, String> commonMove = checkCommonCell(futures);
            if (commonMove != null) {
                System.out.println("Common cell found: " + commonMove);

                // eliminate other symbols at that location in the original
                // puzzle and return true

                // However - how to explain? And is this the shortest path?
                Set<Integer> currentCandidates = candidatesPerCell.get(commonMove.getKey());
                System.out.println(currentCandidates);
                currentCandidates.retainAll(Collections.singleton(myPuzzle.symbolToSymbolCode(commonMove.getValue())));
                System.out.println(candidatesPerCell.get(commonMove.getKey()));

                return true;
            }
        }

        return false;
    }

    // Is there a cell in all futures that is not occupied in
    // the original puzzle but has the same value in all futures?
    private Map.Entry<Coord, String> checkCommonCell(ISudoku[] futures) {
        Map.Entry<Coord, String> move = null;

        for (Coord c : myPuzzle.getAllCells()) {
            if (!myPuzzle.isOccupied(c)) {
                boolean allFuturesHaveSameValue = true;

                // Original must not be occupied, all futures must be
                for (int i=0; i<futures.length; i++) {
                    if (!futures[i].isOccupied(c)) {
                        allFuturesHaveSameValue = false;
                        break;
                    }
                }

                // Check value itself
                if (allFuturesHaveSameValue) {
                    String symbol = futures[0].getSymbolAtCoordinates(c);
                    for (int i = 1; allFuturesHaveSameValue && i < futures.length; i++) {
                        if (!symbol.equals(futures[i].getSymbolAtCoordinates(c))) {
                            allFuturesHaveSameValue = false;
                        }
                    }
                    if (allFuturesHaveSameValue) {
                        move = new AbstractMap.SimpleEntry<>(c, symbol);
                        System.out.println("Same for all futures: " + move);
                        return move;
                    }
                }
             }
        }

        // Same elimination somewhere? - separate method

        return null;
    }
}