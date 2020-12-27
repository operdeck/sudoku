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

import java.util.List;
import java.util.Set;

public abstract class AbtractPuzzle implements IPuzzle {
    private final String name;
    protected IPuzzle previousPuzzle = null;
    protected List<AbstractGroup> groups;
    protected int[][] board; // [x][y]
    protected List<String> possibleSymbols;

    public AbtractPuzzle(String name) {
        this.name = name;
        board = new int[getWidth()][getHeight()];
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
        int[][] newBoard = new int[9][9];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (x == coord.getX() && y == coord.getY()) {
                    newBoard[x][y] = symbolToSymbolCode(symbol);
                } else {
                    newBoard[x][y] = board[x][y];
                }
            }
        }

        IPuzzle nextPuzzle = newInstance(this.name, newBoard);
        ((AbtractPuzzle)nextPuzzle).previousPuzzle = this; // link new puzzle state to current

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
    public String getName() {
        return name;
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
        return possibleSymbols.get(i);
    }

    @Override
    public int symbolToSymbolCode(String symbol) {
        int idx = Math.max(0, possibleSymbols.indexOf(symbol)); // return 0 if not found or empty
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
        for (int x=0; x<getWidth(); x++) {
            for (int y=0; y<getHeight(); y++) {
                Coord c = new Coord(x,y);
                gc.setStroke(Color.BLUE);
                gc.strokeRect(getCellX(canvas, x), getCellY(canvas, y), getCellWidth(canvas), getCellHeight(canvas));
                if (isOccupied(c)) {
                    if (highlight != null && y== highlight.getY() && x== highlight.getX()) {
                        // highlight last move
                        gc.setStroke(Color.DARKGRAY);
                    } else {
                        gc.setStroke(Color.BLACK);
                    }
                    gc.strokeText(String.valueOf(getSymbolAtCoordinates(c)),
                            getCellX(canvas,x+0.5), getCellY(canvas,y+0.5));
                }
            }
        }

        // Big square groups
        // TODO this is currently specific for 9x9
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (int x=0; x<3; x++) {
            for (int y = 0; y<3; y++) {
                gc.strokeRect(getCellX(canvas,x*3), getCellY(canvas,y*3), 3*getCellWidth(canvas), 3*getCellHeight(canvas));
            }
        }

        // Highlight last move
        if (highlight != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeRect(getCellX(canvas, highlight.getX()), getCellY(canvas, highlight.getY()), getCellWidth(canvas), getCellHeight(canvas));
        }
    }

    @Override
    public void drawPossibilities(Canvas canvas, Coord c, Set<Integer> values) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int x = c.getX();
        int y = c.getY();
        for (int i : values) {
            int subrow = (i-1) / 3 - 1;
            int subcol = (i-1) % 3 - 1;
            gc.strokeText(String.valueOf(i),
                    getCellX(canvas,x+0.5+(subcol*0.3)), getCellY(canvas,y+0.5+(subrow*0.3)));
        }
    }

    private double getCellX(Canvas canvas, double x) {
        return(5+x*getCellWidth(canvas));
    }

    private double getCellY(Canvas canvas, double y) {
        return(5+y*getCellHeight(canvas));
    }

    private double getCellWidth(Canvas canvas) {
        double canvasWidth = canvas.getWidth();
        double cellWidth = (canvasWidth-10)/getWidth();
        return(cellWidth);
    }

    private double getCellHeight(Canvas canvas) {
        double canvasHeight = canvas.getHeight();
        double cellHeight = (canvasHeight-10)/getHeight();
        return(cellHeight);
    }


    protected Paint getCellBackground(int x, int y) {
        return Color.WHITE;
    }

}
