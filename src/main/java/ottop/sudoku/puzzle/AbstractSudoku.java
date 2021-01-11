package ottop.sudoku.puzzle;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.solver.Updateable;

import java.util.*;

public abstract class AbstractSudoku implements ISudoku {
    final String name;
    final List<String> possibleSymbols;
    Coord[] allCells;
    int[][] board; // [x][y] to symbolCode

    // Groups also keep state of which cells in the group are occupied

    List<AbstractGroup> groups = new ArrayList<>();
    final List<AbstractGroup> groupsWithBoundaries = new ArrayList<>();

    Updateable solver = null;
    List<Coord> undoStack = new ArrayList<>();
    int undoStackPointer = -1;

    public AbstractSudoku(String name, String[] symbols, int[][] board) {
        this.name = name;
        this.possibleSymbols = Arrays.asList(symbols);
        this.board = board; // new int[getWidth()][getHeight()];

        // Static list of all coordinates in the board
        List<Coord> cells = new ArrayList<>();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                cells.add(new Coord(x, y));
            }
        }
        this.allCells = cells.toArray(new Coord[0]);

        // Groups of cells - different for different Sudoku types
        initGroups();
    }

    abstract void initGroups();

    @Override
    public ISudoku clone() {
        AbstractSudoku c = null;
        try {
            c = (AbstractSudoku) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        c.allCells = this.allCells.clone();

        // Deep copy of board
        c.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            c.board[i] = Arrays.copyOf(board[i], board[i].length);
        }

        c.initGroups();

//        c.undoStack = new ArrayList<>();
//        c.undoStackPointer = -1;

        return c;
    }

    @Override
    public void setSolver(Updateable s) { solver = s; }

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
    public boolean isComplete() {
        for (AbstractGroup g : groups) {
            if (!g.isComplete()) return false;
        }
        return true;
    }

    @Override
    public boolean doMove(Coord coord, String symbol) { // x, y start at 0

        board[coord.getX()][coord.getY()] = symbolToSymbolCode(symbol);
        for (AbstractGroup g: getBuddyGroups(coord)) {
            g.resetGroup(this);
        }

        // Put on undo stack, remove any entries after (because of undo/redo)

        undoStackPointer++;
        undoStack.add(undoStackPointer, coord);
        while (canRedo()) {
            undoStack.remove(undoStack.size()-1);
        }

        // Update all candidates
        solver.update();

        return true;
    }

    @Override
    public boolean canUndo() {
        return undoStackPointer >= 0;
    }

    @Override
    public boolean canRedo() {
        return (undoStack.size()-1) > undoStackPointer;
    }

    @Override
    public Coord undoMove() {
        if (canUndo()) {
            Coord coord = undoStack.get(undoStackPointer);

            board[coord.getX()][coord.getY()] = 0;
            for (AbstractGroup g: getBuddyGroups(coord)) {
                g.resetGroup(this);
            }

            undoStackPointer--;

            // Update all candidates
            solver.update();

            if (undoStackPointer >= 0) {
                return undoStack.get(undoStackPointer); // last move
            }
            return null;
        }
        return null;
    }

    @Override
    public Map.Entry<Coord, String> redoMove() {
        if (canRedo()) {
            undoStackPointer++;

            Coord coord = undoStack.get(undoStackPointer);
            int symbolCode = getSymbolCodeAtCoordinates(coord);

            board[coord.getX()][coord.getY()] = symbolCode;
            for (AbstractGroup g: getBuddyGroups(coord)) {
                g.resetGroup(this);
            }

            // Update all candidates
            solver.update();

            return new AbstractMap.SimpleEntry<>(coord, symbolCodeToSymbol(symbolCode));
        }
        return null;
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
    public List<AbstractGroup> getBuddyGroups(Coord c) {
        List<AbstractGroup> grps = new ArrayList<>();
        for (AbstractGroup g : groups) {
            if (g.isInGroup(c)) {
                grps.add(g);
            }
        }
        return grps;
    }

    @Override
    public List<AbstractGroup> getGroupsWithVisualBoundary()
    {
        return groupsWithBoundaries;
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
        return possibleSymbols.size();
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
        return Math.max(0, possibleSymbols.indexOf(symbol));
    }

    @Override
    public boolean isAtOverlay(Coord c) {
        return false;
    }

    @Override
    public Set<Coord> getBuddies(Coord coord) {
        Set<Coord> buddies = new TreeSet<>();

        if (coord != null) {
            for (AbstractGroup g : groups) {
                if (g.getCoords().contains(coord)) {
                    buddies.addAll(g.getCoords());
                }
            }
            buddies.remove(coord); // you are not your own buddy
        }

        return buddies;
    }

    static int[][] readCommaSeparatedBoard(String[] sudokuRows, int width, int height, String[] symbols) {
        if (sudokuRows.length != height)
            throw new IllegalArgumentException("Initialization must have " + height + " rows");

        int[][] brd = new int[width][height];

        for (int y = 0; y < height; y++) {
            String s = sudokuRows[y];
            String[] aRow = s.split(",");
            if (aRow.length != width)
                throw new IllegalArgumentException("Initialization must have " + width + " chars for each row");
            for (int x = 0; x < width; x++) {
                String symbol = aRow[x];
                brd[x][y] = Math.max(0, Arrays.asList(symbols).indexOf(symbol));
            }
        }

        return brd;
    }

    static int[][] readSingleCharBoard(String[] sudokuRows, int width, int height, String[] symbols) {
        if (sudokuRows.length != height)
            throw new IllegalArgumentException("Initialization must have " + height + " rows");

        int[][] brd = new int[width][height];

        for (int y = 0; y < height; y++) {
            String s = sudokuRows[y];
            if (s.length() != width)
                throw new IllegalArgumentException("Initialization must have " + width + " chars for each row");
            for (int x = 0; x < s.length(); x++) {
                String symbol = s.substring(x, x + 1);
                brd[x][y] = Math.max(0, Arrays.asList(symbols).indexOf(symbol));
            }
        }

        return brd;
    }
}
