package ottop.sudoku.group;

import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.Standard9x9Puzzle;

public class ColumnGroup extends AbstractGroup {

	public ColumnGroup(int startX, int startY, IPuzzle myPuzzle, String id) {
		super(startX, startY, myPuzzle, id);
	}

	@Override
	public int internalIndexToRelativeX(int idx) {
		return 0;
	}

	@Override
	public int internalIndexToRelativeY(int idx) {
		return idx;
	}

	public int getColumn() {
		return super.startX;
	}
}
