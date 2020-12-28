package ottop.sudoku.puzzle;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractPuzzle implements IPuzzle {
    final String name;
    IPuzzle previousPuzzle = null;
    List<AbstractGroup> groups;
    int[][] board; // [x][y]
    String[] possibleSymbols;
    Coord[] allCells;

    public AbstractPuzzle(String name) {
        this.name = name;
        board = new int[getWidth()][getHeight()];

        List<Coord> cells = new ArrayList<>();
        for (int x=0; x<getWidth(); x++) {
            for (int y=0; y<getHeight(); y++) {
                cells.add(new Coord(x, y));
            }
        }
        this.allCells = cells.toArray(new Coord[0]);
    }

    @Override
    public void resetState() {
        initGroups();
    }

    abstract void initGroups();

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                result.append(getSymbolAtCoordinates(new Coord(x,y)));
            }
            result.append("\n");
        }
        return result.toString();
    }

    @Override
    public boolean isSolved() {
        boolean isSolved = true;
        for (AbstractGroup g : groups) {
            if (!g.solved()) isSolved = false;
        }
        return isSolved;
    }

    @Override
    public IPuzzle doMove(Coord coord, String symbol) { // x, y start at 0
        //if (isOccupied(yNew, xNew)) return null;
        int[][] newBoard = new int[getWidth()][getHeight()];
        for (Coord c: getAllCells()) {
            if (c.equals(coord)) {
                newBoard[c.getX()][c.getY()] = symbolToSymbolCode(symbol);
            } else {
                newBoard[c.getX()][c.getY()] = board[c.getX()][c.getY()];
            }
        }

        IPuzzle nextPuzzle = newInstance(this.name, newBoard);
        ((AbstractPuzzle)nextPuzzle).previousPuzzle = this; // link new puzzle state to current

        return nextPuzzle;
    }

    abstract protected IPuzzle newInstance(String name, int[][] brd);

    @Override
    public boolean canUndo() {
        return previousPuzzle != null;
    }

    @Override
    public IPuzzle undoMove() {
        return previousPuzzle;
    }

    @Override
    public boolean isInconsistent() {
        boolean isInconsistent = false;
        for (AbstractGroup g : groups) {
            if (g.isInconsistent()) {
                isInconsistent = true;
                break;
            }
        }
        return isInconsistent;
    }

    @Override
    public AbstractGroup[] getGroups() {
        return groups.toArray(new AbstractGroup[0]);
    }

    @Override
    public Coord[] getAllCells() { return allCells; };

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSymbolCodeRange() {
        return possibleSymbols.length;
    }

    @Override
    public int getSymbolCodeAtCoordinates(Coord coord) {
        return board[coord.getX()][coord.getY()];
    }

    @Override
    public String getSymbolAtCoordinates(Coord coord) {
        return symbolCodeToSymbol(board[coord.getX()][coord.getY()]);
    }

    @Override
    public boolean isOccupied(Coord coord) {
        return board[coord.getX()][coord.getY()] != 0;
    }

    @Override
    public String symbolCodeToSymbol(int i) {
        return possibleSymbols[i];
    }

    @Override
    public int symbolToSymbolCode(String symbol) {
        int idx = Math.max(0, Arrays.asList(possibleSymbols).indexOf(symbol)); // return 0 if not found or empty
        return idx;
    }

    @Override
    public void drawPuzzleOnCanvas(Canvas canvas, Coord highlight) {
        // Canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasHeight = canvas.getHeight();
        double canvasWidth = canvas.getWidth();

        // Big white background
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Background of individual cells
        for (int x=0; x<getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                gc.setFill(getCellBackground(x, y));
                gc.fillRect(getCellX(canvas, x), getCellY(canvas, y), getCellWidth(canvas), getCellHeight(canvas));
            }
        }

        // Symbols
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (Coord c: getAllCells()) {
            gc.setStroke(Color.BLUE);
            gc.strokeRect(getCellX(canvas, c.getX()), getCellY(canvas, c.getY()), getCellWidth(canvas), getCellHeight(canvas));
            if (isOccupied(c)) {
                if (c.equals(highlight)) {
                    // highlight last move
                    gc.setStroke(Color.DARKGRAY);
                } else {
                    gc.setStroke(Color.BLACK);
                }
                gc.strokeText(String.valueOf(getSymbolAtCoordinates(c)),
                        getCellX(canvas,c.getX()+0.5), getCellY(canvas,c.getY()+0.5));
            }
        }

        drawGroupBoundaries(canvas, gc);

        // Highlight last move
        if (highlight != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeRect(getCellX(canvas, highlight.getX()), getCellY(canvas, highlight.getY()), getCellWidth(canvas), getCellHeight(canvas));
        }
    }

    protected void drawGroupBoundaries(Canvas canvas, GraphicsContext gc) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (int x=0; x<3; x++) {
            for (int y = 0; y<3; y++) {
                gc.strokeRect(getCellX(canvas,x*3), getCellY(canvas,y*3), 3*getCellWidth(canvas), 3*getCellHeight(canvas));
            }
        }
    }

    @Override
    public void drawPossibilities(Canvas canvas, Coord c, Set<Integer> symbolCodes) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int x = c.getX();
        int y = c.getY();
        for (int symbolCode : symbolCodes) {
            int subrow = (symbolCode-1) / 3 - 1;
            int subcol = (symbolCode-1) % 3 - 1;
            gc.strokeText(symbolCodeToSymbol(symbolCode),
                    getCellX(canvas,x+0.5+(subcol*0.3)), getCellY(canvas,y+0.5+(subrow*0.3)));
        }
    }

    double getCellX(Canvas canvas, double x) {
        return(5+x*getCellWidth(canvas));
    }

    double getCellY(Canvas canvas, double y) {
        return(5+y*getCellHeight(canvas));
    }

    double getCellWidth(Canvas canvas) {
        double canvasWidth = canvas.getWidth();
        double cellWidth = (canvasWidth-10)/getWidth();
        return(cellWidth);
    }

    double getCellHeight(Canvas canvas) {
        double canvasHeight = canvas.getHeight();
        double cellHeight = (canvasHeight-10)/getHeight();
        return(cellHeight);
    }

    // TODO we could automatically detect overlapping groups
    protected Paint getCellBackground(int x, int y) {
        return Color.WHITE;
    }

    protected int[][] readSingleCharBoard(String[] sudokuRows) {
        if (sudokuRows.length != getHeight())
            throw new IllegalArgumentException("Initialization must have " + getHeight() + " rows");

        int[][] brd = new int[getWidth()][getHeight()];

        for (int y=0; y<getHeight(); y++) {
            String s = sudokuRows[y];
            if (s.length() != getWidth())
                throw new IllegalArgumentException("Initialization must have " + getWidth() + " chars for each row");
            for (int x = 0; x<s.length(); x++) {
                String symbol = s.substring(x, x+1);
                brd[x][y] = symbolToSymbolCode(symbol);
            }
        }

        return brd;
    }

    protected int[][] readCommaSeparatedBoard(String[] sudokuRows) {
        if (sudokuRows.length != getHeight())
            throw new IllegalArgumentException("Initialization must have " + getHeight() + " rows");

        int[][] brd = new int[getWidth()][getHeight()];

        for (int y=0; y<getHeight(); y++) {
            String s = sudokuRows[y];
            String[] aRow = s.split(",");
            if (aRow.length != getWidth())
                throw new IllegalArgumentException("Initialization must have " + getWidth() + " chars for each row");
            for (int x = 0; x<getWidth(); x++) {
                String symbol = aRow[x];
                brd[x][y] = symbolToSymbolCode(symbol);
            }
        }

        return brd;
    }
}
