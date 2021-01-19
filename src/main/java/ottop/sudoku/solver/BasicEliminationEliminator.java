package ottop.sudoku.solver;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.SimpleEliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public class BasicEliminationEliminator extends Eliminator {

    public BasicEliminationEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    // This is not just eliminating, it really resets the whole set of candidates
    public boolean eliminate() {
        boolean hasEliminated = false;
        candidatesPerCell.clear();

        // Start by assigning all possibilities to all cells
        Set<Integer> allPossibilities = new TreeSet<>();
        int symbolCodeRange = myPuzzle.getSymbolCodeRange();
        for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
            allPossibilities.add(symbolCode);
        }
        for (Coord c: myPuzzle.getAllCells()) {
            if (myPuzzle.isOccupied(c)) {
                candidatesPerCell.put(c, new TreeSet<>());
            } else {
                candidatesPerCell.put(c, new TreeSet<>(allPossibilities));
            }
        }

        // Eliminate per group
        for (AbstractGroup g: myPuzzle.getGroups()) {
            Set<Coord> groupCells = g.getCoords();
            Set<Integer> groupSymbolCodes = new TreeSet<>();
            for (Coord c: groupCells) {
                groupSymbolCodes.add(myPuzzle.getSymbolCodeAtCoordinates(c));
            }
            for (Coord c: groupCells) {
                candidatesPerCell.get(c).remove(myPuzzle.getSymbolCodeAtCoordinates(c));

                Set<Integer> removedCodes = new TreeSet<>(groupSymbolCodes);
                removedCodes.retainAll(candidatesPerCell.get(c)); // TODO: this correct?
                if (removedCodes.size() > 0) {
                    candidatesPerCell.get(c).removeAll(removedCodes);
                    // TODO: relatively expensive to do this over & over again
                    Set<String> removedSymbols = new TreeSet<>();
                    for (int x: removedCodes) {
                        removedSymbols.add(myPuzzle.symbolCodeToSymbol(x));
                    }
                    recordEliminationReason(c,
                            new SimpleEliminationReason(removedSymbols, c, g));
                    hasEliminated = true;
                }
            }
        }
        return hasEliminated;
    }

    public boolean eliminate2() {
        boolean hasEliminated = false;
        candidatesPerCell.clear();
        for (Coord c : myPuzzle.getAllCells()) {
            int symbolCodeRange = myPuzzle.getSymbolCodeRange();
            Set<Integer> possibilitiesAtCell = new TreeSet<>();
            for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
                boolean isPossible = true;
                for (AbstractGroup g : myPuzzle.getBuddyGroups(c)) {

                    if (!g.isPossibility(symbolCode, c)) {
                        isPossible = false;
                        hasEliminated = true;
                        // TODO: combining these later on is expensive
                        recordEliminationReason(c,
                                new SimpleEliminationReason(myPuzzle.symbolCodeToSymbol(symbolCode), c, g));
                        break;
                    }
                }
                if (isPossible) possibilitiesAtCell.add(symbolCode);
            }
            candidatesPerCell.put(c, possibilitiesAtCell);
        }
        return hasEliminated;
    }
}