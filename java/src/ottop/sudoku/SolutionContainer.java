package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

/**
 * Container of an array of digits placed in cells, as a solution
 * for the next iteration of the puzzle solver.
 */
public class SolutionContainer {
    private final Map<Coord, String> loneSymbolMap = new HashMap<>();
    private final Map<Coord, AbstractMap.SimpleEntry<String, List<AbstractGroup>>> uniqueSymbolGroups = new HashMap<>();
    private final IPuzzle myPuzzle;
    private final boolean trace;

    public SolutionContainer(IPuzzle p, boolean trace) {
        myPuzzle = p;
        this.trace = trace;
    }

    public boolean addLoneSymbolCode(Coord c, int symbolCode) {
        String symbol = myPuzzle.symbolCodeToSymbol(symbolCode);
        if (loneSymbolMap.containsKey(c) && loneSymbolMap.get(c) != symbol) {
            return false;
//			throw new RuntimeException("ERROR: trying to place " + symbol + " at " + c +
//					" which is occupied by " + loneSymbolMap.get(c));
        }
        if (uniqueSymbolGroups.containsKey(c) && uniqueSymbolGroups.get(c).getKey() != symbol) {
            return false;
//			throw new RuntimeException("ERROR: trying to place " + symbol + " at " + c +
//					" which is occupied by " + uniqueSymbolGroups.get(c));
        }
        loneSymbolMap.put(c, symbol);
        return true;
    }

    public boolean addUniqueSymbolCode(Coord c, int symbolCode, AbstractGroup group) {
        String symbol = myPuzzle.symbolCodeToSymbol(symbolCode);
        if (loneSymbolMap.containsKey(c) && loneSymbolMap.get(c) != symbol) {
            return false;
//			throw new RuntimeException("ERROR: trying to place " + symbol + " at " + c +
//					" which is occupied by " + loneSymbolMap.get(c));
        }
        if (uniqueSymbolGroups.containsKey(c) && uniqueSymbolGroups.get(c).getKey() != symbol) {
            return false;
//			throw new RuntimeException("ERROR: trying to place " + symbol + " at " + c +
//					" which is occupied by " + uniqueSymbolGroups.get(c));
        }
        if (uniqueSymbolGroups.containsKey(c)) {
            AbstractMap.SimpleEntry<String, List<AbstractGroup>> grps = uniqueSymbolGroups.get(c);
            grps.getValue().add(group);
        } else {
            AbstractMap.SimpleEntry<String, List<AbstractGroup>> grps =
                    new AbstractMap.SimpleEntry<>(symbol, new ArrayList<>(Collections.singleton(group)));
            uniqueSymbolGroups.put(c, grps);
        }
        return true;
    }

    // Try lone symbols first, then unique symbols
    public Map.Entry<Coord, String> getFirstMove() {
        Map.Entry<Coord, String> result = null;
        Iterator<Map.Entry<Coord, String>> it = loneSymbolMap.entrySet().iterator();
        if (it.hasNext()) {
            result = it.next();
        } else {
            Iterator<Map.Entry<Coord, AbstractMap.SimpleEntry<String, List<AbstractGroup>>>> it2 =
                    uniqueSymbolGroups.entrySet().iterator();
            if (it2.hasNext()) {
                Map.Entry<Coord, AbstractMap.SimpleEntry<String, List<AbstractGroup>>> res2 = it2.next();
                result = new AbstractMap.SimpleEntry<>(res2.getKey(), res2.getValue().getKey());
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        result.append("(");
        for (Coord c : loneSymbolMap.keySet()) {
            if (!isFirst) result.append(", ");
            result.append(c).append(":").append(loneSymbolMap.get(c));
            if (uniqueSymbolGroups.containsKey(c)) {
                result.append(" ").append(uniqueSymbolGroups.get(c));
            }
            isFirst = false;
        }
        result.append(")");
        return result.toString();
    }

    public Map<Coord, String> getLoneSymbols() {
        return loneSymbolMap;
    }

    public Map<Coord, String> getUniqueSymbols() {
        Map<Coord, String> unique = new HashMap<>();
        for (Coord c : uniqueSymbolGroups.keySet()) {
            AbstractMap.SimpleEntry<String, List<AbstractGroup>> e = uniqueSymbolGroups.get(c);
            unique.put(c, e.getKey());
        }
        return unique;
    }

    public int size() {
        Set<Coord> c = new HashSet<>();
        c.addAll(loneSymbolMap.keySet());
        c.addAll(uniqueSymbolGroups.keySet());
        return c.size();
    }

    public String getLoneSymbolAt(Coord coord) {
        if (loneSymbolMap.containsKey(coord)) {
            return loneSymbolMap.get(coord);
        }
        return null;
    }

    public AbstractMap.SimpleEntry<String, List<AbstractGroup>> getUniqueSymbolAt(Coord coord) {
        if (uniqueSymbolGroups.containsKey(coord)) {
            return uniqueSymbolGroups.get(coord);
        }
        return null;
    }
}
