package ottop.sudoku.board;

import java.util.Arrays;

// TODO: make this an "overlay group" perhaps
// int --> Coord part of normal group
// just different way to initialize

public class MultiCellGroup extends AbstractGroup {
//    private Coord[] cells;

    public MultiCellGroup(String[] cells, String id) {
        super(Coord.toCoords(cells), id);
//        this.cells = Coord.toCoords(cells);
    }

//    @Override
//    public int internalIndexToRelativeX(int idx) {
//        // int absX = startX + internalIndexToRelativeX(internalIndex);
//        return this.cells[idx].getX() - startX;
//    }
//
//    @Override
//    public int internalIndexToRelativeY(int idx) {
//        return this.cells[idx].getY() - startY;
//    }
}