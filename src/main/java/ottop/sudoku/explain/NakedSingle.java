package ottop.sudoku.explain;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;

import java.util.List;
import java.util.Set;

public class NakedSingle extends EliminationReason {
    public NakedSingle(String symbol, Coord coord) {
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

    public Set<Coord> getHighlightSubArea() {
        return coords;
    }
}
