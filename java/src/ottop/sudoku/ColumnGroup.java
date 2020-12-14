package ottop.sudoku;

public class ColumnGroup extends Group {

	public ColumnGroup(int startX, int startY, Puzzle myPuzzle, String id) {
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
