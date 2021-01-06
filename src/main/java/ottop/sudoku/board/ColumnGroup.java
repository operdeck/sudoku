package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class ColumnGroup extends AbstractGroup {

    // TODO drop id argument, generate it yourself
    public ColumnGroup(int startX, int startY, ISudoku myPuzzle) {
        super(startX, startY, myPuzzle, "Column "+(1+startX));
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
