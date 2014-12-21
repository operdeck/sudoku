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
	protected String[] mySudoku;
	private Set<GroupIntersection> subareas;
	protected Map<Character, Integer> toInternalRepresentation = null;
	protected List<Character> fromInternalRepresentation = null;
	
	public static List<Puzzle> all = new ArrayList<Puzzle>();
	
	public Puzzle(String[] mySudoku) {
		this(null, mySudoku);
	}
	
	public Puzzle(String name, String[] mySudoku) {
		this(name);
		init(mySudoku);
	}
	
	public void init(String[] mySudoku) {
		this.mySudoku = mySudoku;
		setRepresentation(mySudoku);
		addGroups();
		setSubAreas();
	}

	protected void setRepresentation(String[] mySudoku) {
		toInternalRepresentation = new HashMap<Character, Integer>();
		fromInternalRepresentation = new ArrayList<Character>();
		fromInternalRepresentation.add(null); // we'll start at index 1
		
		Set<Character> chars = new TreeSet<Character>();
		for (String s : mySudoku) {
			for (int i = 0; i<s.length(); i++) {
				char c = s.charAt(i);
				if (isOccupied(c)) {
					chars.add(c);
				}
			}
		}
		for (Character c : chars) {
			if (!toInternalRepresentation.containsKey(c)) {
				int idx = fromInternalRepresentation.size();
				fromInternalRepresentation.add(c);
				toInternalRepresentation.put(c, idx);
			}
		}
		
//		for (int i = 0; i<fromInternalRepresentation.size(); i++) {
//			System.out.println(i + ": " + fromInternalRepresentation.get(i) + "->" + toInternalRepresentation.get(fromInternalRepresentation.get(i)));
//		}
	}
	
	protected Puzzle(String name) {
		this.name = name;
		all.add(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	private void setSubAreas() {
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

	void addGroups() {
		groups = new ArrayList<Group>();
		
		int cnt=0;
		for (int y=0; y<3; y++) {
			for (int x=0; x<3; x++) {
				groups.add(new SquareGroup(x*3, y*3, this, "Group "+(++cnt)));
			}
		}
		for (int i=0; i<9;i++) {
			groups.add(new RowGroup(0, i, this, "Row "+(1+i)));
			groups.add(new ColumnGroup(i, 0, this, "Column "+(1+i)));
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
		SolutionContainer sols = new SolutionContainer(this);
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
				System.out.println(sols.toString(this));
				init(sols.merge(this));
			}
		} while (updated && !solved());

		if (!solved()) {
			cache.dump();
		} else {
			System.out.println("Final solution in " + iterations + " iterations:");
			System.out.println(sols.toString(this));
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
								" of " + g + " because " + toChar(digit) + " has to be in " +
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
											toChar(digit) + " has to be in " + 
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

	public int getInternalRepresentation(int y, int x) {
		return toInternalRepresentation(getOriginalCharacter(y,x));
	}

	public char getOriginalCharacter(int y, int x) {
		return mySudoku[y].charAt(x);
	}

	public boolean isOccupied(int y, int x) {
		return isOccupied(getOriginalCharacter(y,x));
	}
	
	private boolean isOccupied(char c) {
		return c != '.' && c != ' ';
	}
	
	private int toInternalRepresentation(char charAt) {
		if (!isOccupied(charAt)) return -1;
		return toInternalRepresentation.get(charAt);
	}

	char toChar(int internalRepresentation) {
		if (internalRepresentation == -1) return '.';
		return fromInternalRepresentation.get(internalRepresentation);
	}
	
	
}