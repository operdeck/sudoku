package ottop.sudoku.solver;

// will keep map of candidates

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.puzzle.ISudoku;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Eliminator {
    ISudoku myPuzzle;
    Map<Coord, Set<Integer>> candidatesPerCell;
    Map<Coord, List<Explanation>> removalReasons;
    boolean verbose = false;

    Eliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons)
    {
        this.myPuzzle = myPuzzle;
        this.candidatesPerCell = candidatesPerCell;
        this.removalReasons = removalReasons;
    }

    public Eliminator setVerbose() {
        return setVerbose(true);
    }

    public Eliminator setVerbose(boolean onOff) {
        this.verbose = onOff;
        return this;
    }

    void recordEliminationReason(Coord coord, Explanation reason) {
        removalReasons.put(coord,
                reason.combine(coord, removalReasons.get(coord)));
    }

    boolean removePossibility(int symbolCode, Set<Coord> coords, Explanation reason) {
        boolean anyRemoved = false;
        for (Coord c : coords) {
            boolean removedAtCurrentCoord = false;
            Set<Integer> currentPossibilities = candidatesPerCell.get(c);
            if (currentPossibilities != null) {
                if (currentPossibilities.remove(symbolCode)) {
                    anyRemoved = true;
                    removedAtCurrentCoord = true;
                }
            }
            if (removedAtCurrentCoord) {
                recordEliminationReason(c, reason);
            }
        }
        return anyRemoved;
    }

    boolean removePossibilities(Set<Integer> symbolCodes, Coord coord, Explanation reason) {
        boolean anyRemoved = false;
        for (int symbolCode : symbolCodes) {
            Set<Integer> currentPossibilities = candidatesPerCell.get(coord);
            if (currentPossibilities != null) {
                if (currentPossibilities.remove(symbolCode)) {
                    anyRemoved = true;
                }
            }
        }
        if (anyRemoved) {
            recordEliminationReason(coord, reason);
        }
        return anyRemoved;
    }

    Set<Integer> getCandidatesInArea(Set<Coord> subarea) {
        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(candidatesPerCell.get(c));
        }
        return p;
    }

    public abstract boolean eliminate();
}
