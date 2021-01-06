package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class RowGroup extends AbstractGroup {

    public RowGroup(int startX, int startY, ISudoku myPuzzle) {
        super(startX, startY, myPuzzle, "Row " + (1+startY));
    }

    @Override
    public int internalIndexToRelativeX(int idx) {
        return idx;
    }

    @Override
    public int internalIndexToRelativeY(int idx) {
        return 0;
    }

    public int getRow() {
        return super.startY;
    }
}
