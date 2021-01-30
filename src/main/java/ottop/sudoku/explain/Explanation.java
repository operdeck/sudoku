package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.*;

public abstract class Explanation {
    final Set<String> symbols;

    // TODO: or just one Coord?
    final Set<Coord> coords;

    protected Explanation(Set<String> symbols, Set<Coord> coords) {
        this.symbols = symbols;
        this.coords = coords;
    }

    protected Explanation(String symbol, Set<Coord> coords) {
        this.symbols = new HashSet<>();
        this.symbols.add(symbol);
        this.coords = coords;
    }

    protected Explanation(Set<String> symbols, Coord coord) {
        this.symbols = symbols;
        this.coords = Collections.singleton(coord);
    }

    protected Explanation(String symbol, Coord coord) {
        this.symbols = new HashSet<>();
        this.symbols.add(symbol);
        this.coords = Collections.singleton(coord);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("Removed ");
        if (symbols.size() > 1) {
            result.append(symbols);
        } else {
            result.append(symbols.iterator().next());
        }

        return result.toString();
    }

    public List<Explanation> combine(Coord coord, List<Explanation> eliminationReasons) {
        if (eliminationReasons == null) {
            eliminationReasons = new ArrayList<>();
        }
        eliminationReasons.add(this);
        return eliminationReasons;
    }

    public List<AbstractGroup> getHighlightGroups() {
        return null;
    }

    public Map<String, Set<Coord>> getHighlightCells() {
        return null;
    }

    Map<String, Set<Coord>> getHighlightCells(Set<Coord> cells) {
        Map<String, Set<Coord>> result = new HashMap<>();
        result.put("", cells);
        return result;
    }

    public abstract int getDifficulty();
}
