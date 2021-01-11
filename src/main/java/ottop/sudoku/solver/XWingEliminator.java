package ottop.sudoku.solver;

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.EliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class XWingEliminator extends Eliminator {

    XWingEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<EliminationReason>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() { return false; }
}
