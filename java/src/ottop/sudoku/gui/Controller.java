package ottop.sudoku.gui;

import javafx.event.ActionEvent;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ottop.sudoku.*;
import ottop.sudoku.group.AbstractGroup;

import java.util.Map;
import java.util.Set;

public class Controller {
    public static Controller theController = null;

    public Button clearButton;
    public Canvas gameCanvas;
    public ButtonBar digitButtonBar;
    public Label tbNotes;
    public Button undoButton;
    public CheckBox cbNakedPairs;
    public CheckBox cbXWings;
    public CheckBox cbRadiation;
    public Label tbLevel;
    public CheckBox cbBasicElimination;
    public ChoiceBox typeDropdown;
    public Button buttonMoreHelp;
    public TextArea notes;
    public Label labelPosition;
    public ChoiceBox cbPuzzleDB;

    private IPuzzle myPuzzle;
    private String currentCellSymbol = null;

    public Controller() {
        theController = this;
    }

    public void digitClicked(ActionEvent actionEvent) {
        currentCellSymbol = ((Button)actionEvent.getSource()).getText();
        //System.out.println("Button text: " + currentDigit);
    }

    public void clearClicked(ActionEvent actionEvent) {
        currentCellSymbol = null;
    }

    // TODO: support arrow keys move around in canvas
    // TODO: also support pressing keys 1-9

    private double getCellWidth() {
        double canvasWidth = gameCanvas.getWidth();
        double cellWidth = (canvasWidth-10)/myPuzzle.getWidth();
        return(cellWidth);
    }

    private double getCellHeight() {
        double canvasHeight = gameCanvas.getHeight();
        double cellHeight = (canvasHeight-10)/myPuzzle.getHeight();
        return(cellHeight);
    }

    private double getCellX(double x) {
        return(5+x*getCellWidth());
    }

    private double getCellY(double y) {
        return(5+y*getCellHeight());
    }

    public void canvasMouseClick(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth()*mouseEvent.getX()/gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight()*mouseEvent.getY()/gameCanvas.getHeight());

        if (null != currentCellSymbol) {
            IPuzzle newPuzzle = myPuzzle.doMove(x, y, currentCellSymbol);
            if (newPuzzle != null) {
                setPuzzle(newPuzzle, new Coord(x,y));
            }
        }
    }

    public void setPuzzle(IPuzzle initPuzzle) {
        setPuzzle(initPuzzle, null);
    }

    public void setPuzzle(IPuzzle initPuzzle, Coord highlight) {
        myPuzzle = initPuzzle;
        updateWholeDisplay(highlight);
    }

    private void updateWholeDisplay(Coord highlight) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // List of puzzles
        try {
            String[] puzzles = PuzzleDB.getPuzzles();
            cbPuzzleDB.getItems().setAll(puzzles);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Puzzle name and type
        typeDropdown.getItems().setAll(new String[] {StandardPuzzle.TYPE, NRCPuzzle.TYPE, SudokuLetterPuzzle.TYPE, Sudoku10x10Puzzle.TYPE});
        typeDropdown.setValue(myPuzzle.getSudokuType());
        cbPuzzleDB.setValue(myPuzzle.getName());
        undoButton.setDisable(!myPuzzle.canUndo());

        // Canvas
        double canvasHeight = gameCanvas.getHeight();
        double canvasWidth = gameCanvas.getWidth();

        // Big white background
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Background of individual cells
        for (int x=0; x<myPuzzle.getWidth(); x++) {
            for (int y = 0; y < myPuzzle.getHeight(); y++) {
                gc.setFill(myPuzzle.getCellBackground(x, y));
                gc.fillRect(getCellX(x), getCellY(y), getCellWidth(), getCellHeight());
            }
        }

        // Symbols
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        for (int x=0; x<myPuzzle.getWidth(); x++) {
            for (int y=0; y<myPuzzle.getHeight(); y++) {
                gc.setStroke(Color.BLUE);
                gc.strokeRect(getCellX(x), getCellY(y), getCellWidth(), getCellHeight());
                if (myPuzzle.isOccupied(y, x)) {
                    if (highlight != null && y== highlight.getRow() && x== highlight.getCol()) {
                        // highlight last move
                        gc.setStroke(Color.ORANGE);
                    } else {
                        gc.setStroke(Color.BLACK);
                    }
                    gc.strokeText(String.valueOf(myPuzzle.getSymbolAtCoordinates(y, x)),
                            getCellX(x+0.5), getCellY(y+0.5));
                }
            }
        }
        labelPosition.setText("");

        // Big square groups
        // TODO this is currently specific for 9x9
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (int x=0; x<3; x++) {
            for (int y = 0; y<3; y++) {
                gc.strokeRect(getCellX(x*3), getCellY(y*3), 3*getCellWidth(), 3*getCellHeight());
            }
        }

        // Hints & help
        showPuzzleHints();
    }

    public void hintsAction(ActionEvent actionEvent) {
        if ((cbRadiation.isSelected() || cbNakedPairs.isSelected() || cbXWings.isSelected()) & !cbBasicElimination.isSelected()) {
            cbBasicElimination.setSelected(true);
        }
        updateWholeDisplay(null);
    }

    public void showPuzzleHints() {
        if (myPuzzle.isSolved()) {
            tbNotes.setText("Complete");
        } else if (myPuzzle.isInconsistent()) {
            tbNotes.setText("Inconsistent");
        } else {
            SudokuSolver sv = new SudokuSolver(myPuzzle);

            if (cbRadiation.isSelected()) {
                sv.eliminateByRadiationFromIntersections();
            }
            if (cbNakedPairs.isSelected()) {
                sv.eliminateNakedPairs();
            }
            if (cbXWings.isSelected()) {
                sv.eliminateByXWings();
            }

            if (cbBasicElimination.isSelected()) {
                Map<Coord, Set<Integer>> optionsPerCell = sv.getAllPossibilities();
                for (Coord c : optionsPerCell.keySet()) {
                    drawPossibilities(c, optionsPerCell.get(c));
                }
                SolutionContainer loners = sv.getLoneNumbers();
                SolutionContainer unique = sv.getUniqueValues();
                notes.setText("Lone numbers: " + loners.size() + " " + loners +"\n");
                notes.appendText("Unique values: " + unique.size() + " " + unique +"\n");
            } else {
                notes.setText("");
            }
        }
    }

    private void drawPossibilities(Coord c, Set<Integer> values) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int x = c.getCol();
        int y = c.getRow();
        for (int i : values) {
            int subrow = (i-1) / 3 - 1;
            int subcol = (i-1) % 3 - 1;
            gc.strokeText(String.valueOf(i),
                    getCellX(x+0.5+(subcol*0.3)), getCellY(y+0.5+(subrow*0.3)));
        }
    }

    public void moreHelpAction(ActionEvent actionEvent) {
        System.out.println("More help...");
    }

    public void undoAction(ActionEvent actionEvent) {
        IPuzzle prevPuzzle = myPuzzle.undoMove();
        System.out.println("Undo.." + prevPuzzle);
        if (prevPuzzle != null) {
            setPuzzle(prevPuzzle);
        }
    }

    public void typeAction(ActionEvent actionEvent) {
        //System.out.println("Type..." + typeDropdown.getValue());
    }

    public void canvasMouseMove(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth()*mouseEvent.getX()/gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight()*mouseEvent.getY()/gameCanvas.getHeight());
        if (x>=0 && x <=9) {
            if (y>=0 && y<=9) {
                labelPosition.setText(""+new Coord(x,y));
            }
        }
    }

    public void puzzleSelectAction(ActionEvent actionEvent) {
        String puzzleName = String.valueOf(cbPuzzleDB.getValue());
        if (!puzzleName.equals(myPuzzle.getName())) { // event triggers very often
            IPuzzle p;
            try {
                p = PuzzleDB.getPuzzleByName(puzzleName);
                if (p != null) {
                    setPuzzle(p);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
