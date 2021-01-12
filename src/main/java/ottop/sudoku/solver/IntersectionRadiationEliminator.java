package ottop.sudoku.solver;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.IntersectionRadiationEliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public class IntersectionRadiationEliminator extends Eliminator {

    IntersectionRadiationEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() {
        boolean updated = false;
        // TODO maybe not even need to explicitly create these intersections
        // TODO intersections can be smaller anyway
        Set<GroupIntersection> groupIntersections =
                GroupIntersection.createGroupIntersections(myPuzzle.getGroups());

        for (GroupIntersection intersection : groupIntersections) {
            Set<Integer> possibilitiesAtGroupIntersection =
                    getCandidatesInArea(intersection.getIntersection());
            for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
                if (possibilitiesAtGroupIntersection.contains(symbolCode)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] groupCoordSet = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i = 0; i < 2; i++) {
                        groupCoordSet[i] = new HashSet<>(intersection.getIntersectionGroup(i).getCoords());
                        groupCoordSet[i].removeAll(intersection.getIntersection());
                        pr[i] = getCandidatesInArea(groupCoordSet[i]);
                    }
                    for (int i = 0; i < 2; i++) {
                        if (!pr[i].contains(symbolCode)) {
                            // If 'digit' is not possible anywhere else in this group, then it
                            // has to be in the intersection. Which means it cannot be
                            // anywhere else in the other group either.
                            if (removePossibility(symbolCode,
                                    groupCoordSet[1 - i],
                                    new IntersectionRadiationEliminationReason(myPuzzle.symbolCodeToSymbol(symbolCode),
                                            groupCoordSet[1 - i],
                                            intersection.getIntersectionGroup(i),
                                            intersection.getIntersectionGroup(1 - i),
                                            intersection.getIntersection()))) updated = true;
                        }
                    }
                }
            }
        }
        return updated;
    }

    static class GroupIntersection {
        private final Set<Coord> intersection;
        private final AbstractGroup[] grps = new AbstractGroup[2];

        public GroupIntersection(AbstractGroup a, AbstractGroup b) {
            intersection = new TreeSet<>(a.getCoords());
            intersection.retainAll(b.getCoords());
            grps[0] = a;
            grps[1] = b;
        }

        public static Set<GroupIntersection> createGroupIntersections(List<AbstractGroup> groups) {
            Set<GroupIntersection> intersections = new LinkedHashSet<>();
            for (AbstractGroup a : groups) {
                for (AbstractGroup b : groups) {
                    if (a != b) {
                        GroupIntersection overlap = new GroupIntersection(a, b);
                        // Intersections of 1 don't count. These would be seen as "lone values" anyway.
                        // TODO: if all elements of the intersection are occupied skip it also
                        if (overlap.intersection.size() > 1) {
                            intersections.add(overlap);
                        }
                    }
                }
            }
            return intersections;
        }

        public AbstractGroup getIntersectionGroup(int i) {
            return grps[i];
        }

        @Override
        public String toString() {
            return intersection.toString();
        }

        @Override
        public int hashCode() {
            return intersection.hashCode();
        }

        @Override
        // Consider equal if the intersection is equal but don't care about the order of the two group references.
        public boolean equals(Object obj) {
            if (obj instanceof GroupIntersection) {
                GroupIntersection other = (GroupIntersection) obj;
                if ((grps[0] == other.grps[0] && grps[1] == other.grps[1]) || (grps[0] == other.grps[1] && grps[1] == other.grps[0])) {
                    if (intersection == null && other.intersection == null) {
                        return true;
                    } else if (intersection == null || other.intersection == null) {
                        return false;
                    } else {
                        return intersection.equals(other.intersection);
                    }
                } else {
                    return false;
                }
            }
            return super.equals(obj);
        }

        public Set<Coord> getIntersection() {
            return intersection;
        }
    }
}