package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class RowGroup extends RectangularGroup {

    public RowGroup(int startX, int startY, int size) {
        super(startX, startY, startX+size, startY+1,"Row " + (1+startY));
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        return idx;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return 0;
//    }

    public int getRow() {
        return coords.keySet().iterator().next().getY();
    }
}
