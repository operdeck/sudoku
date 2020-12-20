package ottop.sudoku;

import org.jetbrains.annotations.NotNull;
import ottop.sudoku.group.AbstractGroup;

import java.util.*;

public class PossibilitiesContainer {
	// Map of cell to a set of possible values.
	private Map<Coord, Set<Integer>> nonCollissionMap = new HashMap<> ();

	public PossibilitiesContainer(AbstractGroup[] groups) {
		updateNonCollisions(groups);
	}

	// Create the map by simply walking through all cells and all cells in all groups
	// so order is 9 x 9 x 9 x {nr of cells}
	private void updateNonCollisions(AbstractGroup[] groups) {
		for (Coord c : Coord.all) {
			nonCollissionMap.put(c, getPossibilities(c, groups));
		}
	}

	public Map<Coord, Set<Integer>> getAllPossibilities()
	{
		Map<Coord, Set<Integer>> allPossibilities = new HashMap<>();

		for (Coord c : Coord.all) {
			Set<Integer> p = nonCollissionMap.get(c);
			if (p != null && !p.isEmpty()) {
				allPossibilities.put(c, p);
			}
		}

		return (allPossibilities);
	}

	public String toString()
	{
		StringBuffer result = new StringBuffer();
		Map<Coord, Set<Integer>> allPossibilities = getAllPossibilities();
		for (Coord c : allPossibilities.keySet()) {
			result.append("Possibilities at " + c + ":" + allPossibilities.get(c) + "\n");
		}
		return result.toString();
	}

	private Set<Integer> getPossibilities(Coord c, AbstractGroup[] groups) {
		Set<Integer> s = new HashSet<>();
		for (int n=1; n<=9; n++) {
			boolean isPossible = true;
			for (AbstractGroup g : groups) {
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
		Set<Coord> removed = new TreeSet<>();
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
		Set<Integer> removed = new TreeSet<>();
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
	
	public Set<Integer> getPossibilities(@NotNull Set<Coord> subarea) {
		Set<Integer> p = new HashSet<>();
		for (Coord c : subarea) {
			p.addAll(getPossibilities(c));
		}
		return p;
	}

	public void merge(PossibilitiesContainer newCache) {
		for (Coord c : nonCollissionMap.keySet()) {
			Set<Integer> p = nonCollissionMap.get(c);
			p.retainAll(newCache.nonCollissionMap.get(c));
  		}
	}
}
