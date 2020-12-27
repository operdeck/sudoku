package ottop.sudoku.group;


import ottop.sudoku.puzzle.IPuzzle;

public class SquareGroup extends AbstractGroup {

	public SquareGroup(int startX, int startY, IPuzzle myPuzzle, String id) {
		super(startX, startY, myPuzzle, id);
	}

	@Override
	public int internalIndexToRelativeX(int idx) { return idx % 3; }
	@Override
	public int internalIndexToRelativeY(int idx) { return idx / 3; }
}
