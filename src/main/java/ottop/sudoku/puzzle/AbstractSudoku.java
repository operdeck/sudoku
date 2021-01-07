package ottop.sudoku.puzzle;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.*;

public abstract class AbstractSudoku implements ISudoku {
    ISudoku previousPuzzle = null;

    final String name;
    // I think below two should be final and can be if we refactor the constructors
    String[] possibleSymbols;
    Coord[] allCells;

    int[][] board; // [x][y]

    // Groups are stateful

    List<AbstractGroup> groups;
    List<AbstractGroup> groupsWithBoundaries = new ArrayList<>();

    public AbstractSudoku(String name) {
        this.name = name;
        board = new int[getWidth()][getHeight()];

        // TODO: below is the reset functionality

        List<Coord> cells = new ArrayList<>();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                cells.add(new Coord(x, y));
            }
        }
        this.allCells = cells.toArray(new Coord[0]);

        // TODO: should call init groups now
    }

    @Override
    public void initAllGroups() { // TODO: this really is just reset ALL groups

        // separate method not needed - just use initAllGroups

        initGroups();
    }

    abstract void initGroups();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                result.append(getSymbolAtCoordinates(new Coord(x, y)));
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

    // TODO: see if we can do a move w/o recreating the whole board.
    // - only if not occupied - if occupied then reset like we do now
    // - shallow copy of the current board into previous puzzle, perhaps null groups etc.
    //   really only keep the current "board"
    // - put the symbol at coord
    // - reset the groups you're part of
    @Override
    public ISudoku doMove(Coord coord, String symbol) { // x, y start at 0
        //if (isOccupied(yNew, xNew)) return null;
        int[][] newBoard = new int[getWidth()][getHeight()];
        for (Coord c : getAllCells()) {
            if (c.equals(coord)) {
                newBoard[c.getX()][c.getY()] = symbolToSymbolCode(symbol);
            } else {
                newBoard[c.getX()][c.getY()] = board[c.getX()][c.getY()];
            }
        }

        ISudoku nextPuzzle = newInstance(this.name, newBoard);
        ((AbstractSudoku) nextPuzzle).previousPuzzle = this; // link new puzzle state to current

        return nextPuzzle;
    }

    abstract protected ISudoku newInstance(String name, int[][] brd);

    @Override
    public boolean canUndo() {
        return previousPuzzle != null;
    }

    @Override
    public ISudoku undoMove() {
        // TODO: of DoMove becomes cheaper then undo will have to
        // reset the full puzzle. Only board should be deep copied.

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
    public List<AbstractGroup> getGroups() {
        return groups;
    }

    @Override
    public List<AbstractGroup> getGroups(Coord c) {
        List<AbstractGroup> grps = new ArrayList<>();
        for (AbstractGroup g : groups) {
            if (g.isInGroup(c)) {
                grps.add(g);
            }
        }
        return grps;
    }

    @Override
    public Coord[] getAllCells() {
        return allCells;
    }

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
        return Math.max(0, Arrays.asList(possibleSymbols).indexOf(symbol));
    }

    @Override
    public void drawPuzzleOnCanvas(Canvas canvas, Coord highlight, Set<Coord> currentHighlightedSubArea) {
        // Canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasHeight = canvas.getHeight();
        double canvasWidth = canvas.getWidth();

        // Big white background
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // For buddy cells
        Set<Coord> buddies = getBuddies(highlight);

        // Background of individual cells
        for (Coord c: allCells) {
            gc.setFill(getCellBackground(c.getX(), c.getY(),
                    (buddies != null) && buddies.contains(c),
                    (currentHighlightedSubArea != null) && currentHighlightedSubArea.contains(c)));
            gc.fillRect(getCellX(canvas, c.getX()), getCellY(canvas, c.getY()),
                    getCellWidth(canvas), getCellHeight(canvas));
        }

        // Symbols
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (Coord c : getAllCells()) {
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
                        getCellX(canvas, c.getX() + 0.5), getCellY(canvas, c.getY() + 0.5));
            }
        }

        // Group boundaries
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (AbstractGroup g: getGroupsWithVisualBoundary()) {
            drawGroup(canvas, g);
        }
    }

    protected List<AbstractGroup> getGroupsWithVisualBoundary() {
        return groupsWithBoundaries;
    }

    @Override
    public void drawGroup(Canvas canvas, AbstractGroup g) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Set<Coord> coords = g.getCoords();
        int xMin=Integer.MAX_VALUE, yMin=Integer.MAX_VALUE;
        int xMax=Integer.MIN_VALUE, yMax= Integer.MIN_VALUE;
        for (Coord c: coords) {
           xMin = Math.min(xMin, c.getX());
           xMax = Math.max(xMax, c.getX());
           yMin = Math.min(yMin, c.getY());
           yMax = Math.max(yMax, c.getY());
        }
        gc.strokeRect(getCellX(canvas, xMin), getCellY(canvas, yMin),
                (xMax-xMin+1)*getCellWidth(canvas), (yMax-yMin+1)*getCellHeight(canvas));

    }

    private Set<Coord> getBuddies(Coord highlight) {
        Set<Coord> buddies = new TreeSet<>();

        if (highlight != null) {
            for (AbstractGroup g : groups) {
                if (g.getCoords().contains(highlight)) {
                    buddies.addAll(g.getCoords());
                }
            }
            buddies.remove(highlight); // you are not your own buddy
        }

        return buddies;
    }

    @Override
    public void drawPossibilities(Canvas canvas, Coord c, Set<Integer> symbolCodes) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int n = getSymbolCodeRange() - 1; // minus empty cell code
        int nmarkerrows = (int) Math.sqrt(n);
        int nmarkercols = (int) Math.ceil(n / (double) nmarkerrows);

        for (int symbolCode : symbolCodes) {
            int subrow = (symbolCode - 1) / nmarkercols;
            int subcol = (symbolCode - 1) % nmarkercols;
            gc.strokeText(symbolCodeToSymbol(symbolCode),
                    getCellX(canvas, c.getX() + 0.15 + 0.7 * subcol / (double) (nmarkercols - 1)),
                    getCellY(canvas, c.getY() + 0.15 + 0.7 * subrow / (double) (nmarkerrows - 1)));
        }
    }

    double getCellX(Canvas canvas, double x) {
        return (5 + x * getCellWidth(canvas));
    }

    double getCellY(Canvas canvas, double y) {
        return (5 + y * getCellHeight(canvas));
    }

    double getCellWidth(Canvas canvas) {
        double canvasWidth = canvas.getWidth();
        return (canvasWidth - 10) / getWidth();
    }

    double getCellHeight(Canvas canvas) {
        double canvasHeight = canvas.getHeight();
        return (canvasHeight - 10) / getHeight();
    }

    // TODO: we could automatically detect overlapping groups
    protected Paint getCellBackground(int x, int y, boolean isBuddy, boolean isInHighlightedSubArea) {
        if (isInHighlightedSubArea) return Color.BURLYWOOD;
        if (isBuddy) return Color.LIGHTGRAY;

        return Color.WHITE;
    }

    protected int[][] readSingleCharBoard(String[] sudokuRows) {
        if (sudokuRows.length != getHeight())
            throw new IllegalArgumentException("Initialization must have " + getHeight() + " rows");

        int[][] brd = new int[getWidth()][getHeight()];

        for (int y = 0; y < getHeight(); y++) {
            String s = sudokuRows[y];
            if (s.length() != getWidth())
                throw new IllegalArgumentException("Initialization must have " + getWidth() + " chars for each row");
            for (int x = 0; x < s.length(); x++) {
                String symbol = s.substring(x, x + 1);
                brd[x][y] = symbolToSymbolCode(symbol);
            }
        }

        return brd;
    }

    protected int[][] readCommaSeparatedBoard(String[] sudokuRows) {
        if (sudokuRows.length != getHeight())
            throw new IllegalArgumentException("Initialization must have " + getHeight() + " rows");

        int[][] brd = new int[getWidth()][getHeight()];

        for (int y = 0; y < getHeight(); y++) {
            String s = sudokuRows[y];
            String[] aRow = s.split(",");
            if (aRow.length != getWidth())
                throw new IllegalArgumentException("Initialization must have " + getWidth() + " chars for each row");
            for (int x = 0; x < getWidth(); x++) {
                String symbol = aRow[x];
                brd[x][y] = symbolToSymbolCode(symbol);
            }
        }

        return brd;
    }
}
