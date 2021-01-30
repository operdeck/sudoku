package ottop.sudoku.board;


import ottop.sudoku.puzzle.ISudoku;

public class SquareGroup extends RectangularGroup {

    public SquareGroup(int startX, int startY, String id) {
        super(startX, startY, startX+3, startY+3, id);
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        return idx % 3;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return idx / 3;
//    }
}
