package ottop.sudoku.solver;

// will keep map of candidates

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.EliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Eliminator {
    ISudoku myPuzzle;
    Map<Coord, Set<Integer>> candidatesPerCell;
    Map<Coord, List<EliminationReason>> removalReasons;

    Eliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<EliminationReason>> removalReasons)
    {
        this.myPuzzle = myPuzzle;
        this.candidatesPerCell = candidatesPerCell;
        this.removalReasons = removalReasons;
    }

    void recordEliminationReason(Coord coord, EliminationReason reason) {
        removalReasons.put(coord,
                reason.combine(removalReasons.get(coord)));
    }

    public abstract boolean eliminate();
}
