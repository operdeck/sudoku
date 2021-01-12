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
        for (Coord c : myPuzzle.getAllCells()) {
            int symbolCodeRange = myPuzzle.getSymbolCodeRange();
            Set<Integer> possibilitiesAtCell = new TreeSet<>();
            for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
                boolean isPossible = true;
                for (AbstractGroup g : myPuzzle.getBuddyGroups(c)) {
                    if (!g.isPossibility(symbolCode, c)) {
                        isPossible = false;
                        hasEliminated = true;
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