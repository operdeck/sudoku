package ottop.sudoku.board;

import java.util.Arrays;

// TODO: make this an "overlay group" perhaps
// int --> Coord part of normal group
// just different way to initialize

public class MultiCellGroup extends AbstractGroup {
    private Coord[] cells;

    private static Coord[] asCoords(String[] cells) {
        Coord[] result = new Coord[cells.length];
        for (int i=0; i< cells.length; i++) result[i] = new Coord(cells[i]);
        return result;
    }


    public MultiCellGroup(String[] cells, String id) {
        super(asCoords(cells), id);
        this.cells = asCoords(cells);
    }

    @Override
    public int internalIndexToRelativeX(int idx) {
        // int absX = startX + internalIndexToRelativeX(internalIndex);
        return this.cells[idx].getX() - startX;
    }

    @Override
    public int internalIndexToRelativeY(int idx) {
        return this.cells[idx].getY() - startY;
    }
}