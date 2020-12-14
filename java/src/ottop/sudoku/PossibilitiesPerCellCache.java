package ottop.sudoku;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PossibilitiesPerCellCache {
	// Map of cell to a set of possible values.
	private Map<Coord, Set<Integer>> nonCollissionMap = new HashMap<Coord, Set<Integer>> ();

	public PossibilitiesPerCellCache(List<Group> groups) {
		updateNonCollisions(groups);
	}

	// Create the map by simply walking through all cells and all cells in all groups
	// so order is 9 x 9 x 9 x {nr of cells}
	private void updateNonCollisions(List<Group> groups) {
		for (Coord c : Coord.all) {
			nonCollissionMap.put(c, getPossibilities(c, groups));
		}
	}

	public void dump() {
		for (Coord c : Coord.all) {
			Set<Integer> p = nonCollissionMap.get(c);
			if (p != null && !p.isEmpty()) {
				System.out.println("Possibilities at "+c+": "+ p);
			}
		}
	}
	
	private Set<Integer> getPossibilities(Coord c, List<Group> groups) {
		Set<Integer> s = new HashSet<Integer>();
		for (int n=1; n<=9; n++) {
			boolean isPossible = true;
			for (Group g : groups) {
				if (!g.isPossibility(n, c)) {
					isPossible = false;
					break;
				}
			}
			if (isPossible) s.add(n);
		}
		return s;
	}

	public Set<Integer> getPossibilities(Coord c) {
		return nonCollissionMap.get(c);
	}
	
	public boolean removePossibility(int digit, Coord c, String reason) {
		Set<Integer> set = nonCollissionMap.get(c);
		if (set != null) {
			boolean removed = set.remove(digit);
			if (removed) {
				System.out.println("Eliminate " + digit + " from possibilities at " + c + reason);
			}
			return removed;
		} else {
			return false;
		}
	}

	public boolean removePossibility(int digit, Set<Coord> coords, String reason) {
		Set<Coord> removed = new TreeSet<Coord>();		
		for (Coord c : coords) {
			Set<Integer> set = nonCollissionMap.get(c);
			if (set != null) {
				if (set.remove(digit)) {
					removed.add(c);
				}
			}
		}
		if (removed.size() > 0) {
			System.out.println("Eliminate " + digit + " from possibilities at " + removed + reason);
		}
		return removed.size() > 0;
	}
	
	public boolean removePossibilities(Set<Integer> possibilities, Coord c, String reason) {
		Set<Integer> removed = new TreeSet<Integer>(); 
		for (int digit : possibilities) {
			Set<Integer> set = nonCollissionMap.get(c);
			if (set != null) {
				if (set.remove(digit)) removed.add(digit);
			}
		}
		if (removed.size() > 0) {
			System.out.println("Eliminate " + removed + " from possibilities at " + c + reason);
		}
		return removed.size() > 0;
	}
	
	public Set<Integer> getPossibilities(Set<Coord> subarea) {
		Set<Integer> p = new HashSet<Integer>();
		for (Coord c : subarea) {
			p.addAll(getPossibilities(c));
		}
		return p;
	}

	public void merge(PossibilitiesPerCellCache newCache) {
		for (Coord c : nonCollissionMap.keySet()) {
			Set<Integer> p = nonCollissionMap.get(c);
			p.retainAll(newCache.nonCollissionMap.get(c));
  		}
	}
}
