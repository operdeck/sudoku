package ottop.sudoku.solver;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.NakedGroupEliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public class NakedGroupEliminator extends Eliminator {

    NakedGroupEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() {
        boolean updated = false;

        for (AbstractGroup g : myPuzzle.getGroups()) {
            // create map from sets of possibilities to the coordinates (in this group) that have those (same) possibilities
            Map<Set<Integer>, Set<Coord>> nakedGroupMap = new LinkedHashMap<>();
            for (Coord c : g.getCoords()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> pc = candidatesPerCell.get(c);
                    Set<Coord> coordSet = nakedGroupMap.computeIfAbsent(pc, k -> new HashSet<>());
                    coordSet.add(c);
                }
            }

            // find groups of cells that all have the same possibilities, and which is of the same size as the
            // nr of possibilities: the possibilities can be removed from the rest of the group
            if (eliminateInGroup(g, nakedGroupMap, false)) updated = true;

            // Combine elements in this map. For example, if there are entries
            // {26} --> [a], {27} --> [c], {26} --> [c] can be combined
            // to a new entry {267} --> [abc]
            combineNakedGroups(g, nakedGroupMap);

            // Do further elimination (in two steps just to improve reporting)
            if (eliminateInGroup(g, nakedGroupMap, true)) updated = true;

        }

        return updated;
    }

    private boolean eliminateInGroup(AbstractGroup g,
                                     Map<Set<Integer>, Set<Coord>> nakedGroupMap,
                                     boolean isExtended) {
        boolean hasEliminated = false;

        for (Map.Entry<Set<Integer>, Set<Coord>> entry : nakedGroupMap.entrySet()) {
            Set<Integer> nakedGroupSymbolCodes = entry.getKey();
            Set<Coord> nakedGroupCoords = entry.getValue();
            Set<String> nakedGroupSymbols = new HashSet<>();
            for (Integer p : nakedGroupSymbolCodes) {
                nakedGroupSymbols.add(myPuzzle.symbolCodeToSymbol(p));
            }
            if (nakedGroupSymbolCodes.size() > 1 && nakedGroupSymbolCodes.size() == nakedGroupCoords.size()) {
                for (Coord c : g.getCoords()) {
                    if (!myPuzzle.isOccupied(c)) {
                        if (!nakedGroupCoords.contains(c)) {

                            // naked pair symbols to be removed at c but find the
                            // intersection with the remaining possibilities so only
                            // really remove the ones not already removed earlier
                            Set<Integer> currentPossibilities = candidatesPerCell.get(c);
                            Set<Integer> actualRemovals = new HashSet<>(nakedGroupSymbolCodes);
                            actualRemovals.retainAll(currentPossibilities);

                            // translate actual removals to symbols
                            Set<String> actualRemovalSymbols = new HashSet<>();
                            for (Integer p : actualRemovals) {
                                actualRemovalSymbols.add(myPuzzle.symbolCodeToSymbol(p));
                            }

                            if (removePossibilities(actualRemovals, c,
                                    new NakedGroupEliminationReason(actualRemovalSymbols, c,
                                            g,
                                            nakedGroupSymbols, nakedGroupCoords, isExtended))) hasEliminated = true;
                        }
                    }
                }
            }
        }

        return hasEliminated;
    }

    private void combineNakedGroups(AbstractGroup g, Map<Set<Integer>, Set<Coord>> nakedGroupMap) {
        final int range = (1 << g.getGroupSize()); // range of possibilities for 9 digits: 2^9
        final int mask = range - 1;

        // Representing the possibilities in a bitmap (9 digits), find out which of
        // all possible bitmaps (total of 512, 2^9), are a superset of the bitmap of
        // a set of possibilities. Combine the mapped coordinates of those.
        Map<Integer, Set<Coord>> newCombinationsMap = new HashMap<>();
        for (Set<Integer> key : nakedGroupMap.keySet()) { // TODO: loop over entry set instead? Avoid the get later on.
            int keyAsBitSet = toBitSet(key);
            for (int counter = 0; counter < range; counter++) {
                // bitwise operation to verify that all of "key" are contained in the digit set represented by "i"
                if ((counter | (~keyAsBitSet & mask)) == mask) {
                    Set<Coord> coords = newCombinationsMap.computeIfAbsent(counter, k -> new HashSet<>());
                    Set<Coord> originalCoords = nakedGroupMap.get(key); // TODO: get twice can be removed both...
                    if (originalCoords != null) {
                        coords.addAll(nakedGroupMap.get(key));
                    }
                }
            }
        }

        // Iterate through the combinations and if they are candidates for naked pair
        // reduction, add them to the original map.
        for (Map.Entry<Integer, Set<Coord>> entry : newCombinationsMap.entrySet()) {
            int possibilitiesAsBitSet = entry.getKey();
            Set<Coord> coordinates = entry.getValue();

            // Only add the coordinates if the size of the set of coordinates leaves
            // at least 1 unfilled cell in this group
            if (coordinates.size() > 1 && coordinates.size() < (g.getGroupSize() - g.getGroupOccupiedSize())) {
                if (getBitSetSize(possibilitiesAsBitSet) == coordinates.size()) {
                    nakedGroupMap.put(fromBitSet(possibilitiesAsBitSet), coordinates);
                }
            }
        }
    }

    private static int getBitSetSize(int possibilitiesAsBitSet) {
        int result = 0;
        while (possibilitiesAsBitSet != 0) {
            if ((possibilitiesAsBitSet & 1) != 0) result++;
            possibilitiesAsBitSet = possibilitiesAsBitSet >> 1;
        }
        return result;
    }

    private static int toBitSet(Set<Integer> key) {
        int result = 0;
        for (int i : key) {
            result += (1 << (i - 1));
        }
        return result;
    }

    private static Set<Integer> fromBitSet(int i) {
        Set<Integer> result = new HashSet<>();
        int val = 1;
        while (i != 0) {
            if ((i & 1) != 0) result.add(val);
            i = i >> 1;
            val++;
        }
        return result;
    }

}