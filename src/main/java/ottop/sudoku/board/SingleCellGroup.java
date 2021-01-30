package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

public class SingleCellGroup extends AbstractGroup {
    public SingleCellGroup(Coord cell) {
        super(new Coord[]{cell}, "Cell: " + cell.toString());
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        return 0;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return 0;
//    }
}
