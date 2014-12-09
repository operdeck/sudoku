package perdo.sudoku;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;


public class Puzzle {
	private String name;
	protected List<Group> groups;
	private String[] mySudoku;
	private Set<GroupIntersection> subareas;
	
	public static List<Puzzle> all = new ArrayList<Puzzle>();
	
	public Puzzle(String[] mySudoku) {
		this(null, mySudoku);
	}
	public Puzzle(String name, String[] mySudoku) {
		this(name);
		addStandardGroups(mySudoku);
		setSubAreas();
	}

	protected Puzzle(String name) {
		this.name = name;
		all.add(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public void reInit(String[] mySudoku) {
		addStandardGroups(mySudoku);
		setSubAreas();
	}
	
	protected void setSubAreas() {
		subareas = new LinkedHashSet<GroupIntersection>();
		for (Group a:groups) {
			for (Group b:groups) {
				if (a != b) {
					GroupIntersection overlap = new GroupIntersection(a, b);
					if (!overlap.isEmpty()) {
						subareas.add(overlap);
					}
				}
			}
		}
	}

	protected void addStandardGroups(String[] mySudoku) {
		groups = new ArrayList<Group>();
		this.mySudoku = mySudoku;
		
		int cnt=0;
		for (int y=0; y<3; y++) {
			for (int x=0; x<3; x++) {
				groups.add(new SquareGroup(x*3, y*3, mySudoku, "Group "+(++cnt)));
			}
		}
		for (int i=0; i<9;i++) {
			groups.add(new RowGroup(0, i, mySudoku, "Row "+(1+i)));
			groups.add(new ColumnGroup(i, 0, mySudoku, "Column "+(1+i)));
		}
	}

	public boolean solved() {
		boolean isSolved = true;
		for (Group g:groups) {
			if (!g.solved()) isSolved = false;
		}
		return isSolved;
	}

	//http://www.extremesudoku.info/sudoku.html
	//http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
	public int solve() {
		boolean updated = false;
		int iterations = 0;
		SolutionContainer sols = new SolutionContainer();
		PossibilitiesPerCellCache cache = null;
		do {
			updated = false;
			
			PossibilitiesPerCellCache newCache = 
					new PossibilitiesPerCellCache(groups);
			if (cache == null) {
				cache = newCache;
			} else {
				cache.merge(newCache);
			}

			// Try progressively, simple solutions first
			System.out.println("Check unique cells:");
			for (Group g : groups) {
				if (g.fillUniqueCells(cache, sols)) updated = true;
			}
			if (!updated) {
				System.out.println("Check lone numbers (that can't be placed anywhere else):");
				for (Group g : groups) {
					if (g.fillLoneNumbers(cache, sols)) updated = true;
				}
			}
			if (!updated) {
				System.out.println("Eliminate by radiation from intersections:");
				if (eliminateByRadiationFromIntersections(cache)) updated = true;
			}
			if (!updated) {
				System.out.println("Eliminate naked pairs:");
				for (Group g : groups) {
					if (g.eliminateNakedPairs(this, cache)) updated = true;
				}
			}
			if (!updated) {
				System.out.println("Eliminate by X-Wings:");
				if (eliminateByXWings(cache)) updated = true;
			}
			
			if (updated) {
				iterations++;
				System.out.println(sols.toString(mySudoku));
				reInit(sols.merge(mySudoku));
			}
		} while (updated && !solved());

		if (!solved()) {
			cache.dump();
		}

		return iterations;
	}

	private boolean eliminateByXWings(PossibilitiesPerCellCache cache) {
		boolean updated = false;
		for (int digit : Digits.all) {
			// For each digit, figure out in which rows of each column it occurs. Then
			// get the set of columns that have the same row set. Same for rows x cols.
			// For those entries that have the same size of {columns} x {rows}, we now
			// know that 'digit' has to be in (one or more of) the intersections of those,
			// so it can be eliminated from the possibilities in each of those groups 
			// outside of the intersections.
			
			Map<Set<Group>, Set<Group>> map = new HashMap<Set<Group>, Set<Group>>();
			for (Group g : groups) {
				Set<Group> intersectingGroups = null;
				if (g instanceof ColumnGroup) {
					// list of rows intersecting with column g
					intersectingGroups = toRowGroups(g.getRowSet(digit, cache));
				} else if (g instanceof RowGroup) {
					// list of columns intersecting with row g
					intersectingGroups = toColumnGroups(g.getColSet(digit, cache));
				} 
				if (intersectingGroups != null && !intersectingGroups.isEmpty()) {
					Set<Group> grps = map.get(intersectingGroups);
					if (grps == null) {
						grps = new HashSet<Group>();
						map.put(intersectingGroups, grps);
					}
					grps.add(g);
				}
			}
			
			// Now, with this map, if the set of rows is of the same size as the set of
			// columns that have identical row sets, eliminate 'digit' from other cells 
			// in the rows. Same for col vs row.
			for (Entry<Set<Group>, Set<Group>> entry : map.entrySet()) {
				if (entry.getKey().size() > 1 && 
						entry.getKey().size() == entry.getValue().size()) { // becomes * 2 now ??
					for (Group g : entry.getKey()) {
						// Eliminate 'digit' from this row 'g' except for the groups it intersects
						Set<Coord> candidateRemovals = new TreeSet<Coord>();
						candidateRemovals.addAll(g.getCoords());
						for (Group other : entry.getValue()) {
							candidateRemovals.removeAll(other.getCoords());
						}
						if (cache.removePossibility(digit, candidateRemovals, 
								" of " + g + " because " + digit + " has to be in " +
								entry.getKey() + " X " + entry.getValue() + " (X-Wing)")) updated = true;
					}
				}
			}
		}
		return updated;
	}

	private boolean eliminateByRadiationFromIntersections(PossibilitiesPerCellCache cache) {
		boolean result = false;
		
		for (GroupIntersection a : subareas) {
			Set<Integer> pa = cache.getPossibilities(a.intersection);
			for (int digit : Digits.all) {
				if (pa.contains(digit)) {
					@SuppressWarnings("unchecked")
					Set<Coord>[] r = new Set[2];
					@SuppressWarnings("unchecked")
					Set<Integer>[] pr = new Set[2];
					for (int i=0; i<2; i++) {
						r[i] = new HashSet<Coord>(a.grps[i].getCoords());
						r[i].removeAll(a.intersection);
						pr[i] = cache.getPossibilities(r[i]);
					}
					for (int i=0; i<2; i++) {
						if (!pr[i].contains(digit)) {
							// If 'digit' is not possible anywhere else in this group, then it
							// has to be in the intersection. Which means it cannot be 
							// anywhere else in the other group either.
							if (cache.removePossibility(digit, r[1-i],  
									" (in " + a.grps[1-i] + ") because " + 
											digit + " has to be in " + 
											a.grps[i] + " in one of " + a + " (Intersection Radiation)")) result = true;;
							}
					}
				}
			}
		}
		return result;
	}
	
	private Set<Group> toRowGroups(Set<Integer> rowset) {
		Set<Group> result = new HashSet<Group>();
		for (Group g : groups) {
			if (g instanceof RowGroup) {
				if (rowset.contains(((RowGroup)g).getRow())) {
					result.add(g);
				}
			}
		}
		return result;
	}

	private Set<Group> toColumnGroups(Set<Integer> colset) {
		Set<Group> result = new HashSet<Group>();
		for (Group g : groups) {
			if (g instanceof ColumnGroup) {
				if (colset.contains(((ColumnGroup)g).getColumn())) {
					result.add(g);
				}
			}
		}
		return result;
	}
}
