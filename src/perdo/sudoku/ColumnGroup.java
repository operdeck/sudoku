package perdo.sudoku;

public class ColumnGroup extends Group {

	public ColumnGroup(int startX, int startY, String[] mySudoku, String id) {
		super(startX, startY, mySudoku, id);
	}

	@Override
	protected int internalIndexToRelativeX(int idx) {
		return 0;
	}

	@Override
	protected int internalIndexToRelativeY(int idx) {
		return idx;
	}

	public int getColumn() {
		return startX;
	}
}
