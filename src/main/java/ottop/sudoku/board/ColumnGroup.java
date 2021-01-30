package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class ColumnGroup extends RectangularGroup {

    public ColumnGroup(int startX, int startY, int size) {
        super(startX, startY, startX+1, startY+size, "Column "+(1+startX));
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        return 0;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return idx;
//    }

    public int getColumn() {
        return coords.keySet().iterator().next().getX();
    }
}
