package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NakedSingleSolution extends Explanation {
    public NakedSingleSolution(String symbol, Coord coord) {
        super(symbol, coord);
    }

    @Override
    public int getDifficulty() {
        return 1;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("Naked Single: ").append(symbols.iterator().next());
        return result.toString();
    }

    public Map<String, Set<Coord>> getHighlightCells() {
        return getHighlightCells(coords);
    }
}
