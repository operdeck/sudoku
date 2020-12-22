package ottop.sudoku.gui;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableListBase;
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

import java.util.Arrays;
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
    private String currentDigit = null;

    public Controller() {
        theController = this;
    }

    public void digitClicked(ActionEvent actionEvent) {
        currentDigit = ((Button)actionEvent.getSource()).getText();
        System.out.println("Button text: " + currentDigit);
    }

    public void clearClicked(ActionEvent actionEvent) {
        currentDigit = null;
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

        if (currentDigit != null) {
            IPuzzle newPuzzle = myPuzzle.doMove(x, y, currentDigit.charAt(0));
            if (newPuzzle != null) {
                myPuzzle = newPuzzle;

                System.out.println("Undoing. Prev puzzle:");
                System.out.println(myPuzzle);

                drawPuzzleOnCanvas();

                GraphicsContext gc = gameCanvas.getGraphicsContext2D();

                // highlight last move
                gc.setFill(Color.WHITE);
                gc.setLineWidth(1);
                gc.setStroke(Color.ORANGE);
                gc.strokeText(currentDigit, getCellX(x + 0.5), getCellY(y + 0.5));
            }
        }
    }

    public void setPuzzle(IPuzzle initPuzzle) {
        myPuzzle = initPuzzle;
        drawPuzzleOnCanvas();
    }

    private void drawPuzzleOnCanvas() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        typeDropdown.getItems().setAll(new String[] {"Standard", "NRC"});
        typeDropdown.setValue("NRC");

        double canvasHeight = gameCanvas.getHeight();
        double canvasWidth = gameCanvas.getWidth();
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Filled cells
        for (int x=0; x<myPuzzle.getWidth(); x++) {
            for (int y=0; y<myPuzzle.getHeight(); y++) {
                gc.setLineWidth(1);
                gc.strokeRect(getCellX(x), getCellY(y), getCellWidth(), getCellHeight());
                if (myPuzzle.isOccupied(y, x)) {
                    gc.strokeText(String.valueOf(myPuzzle.getValueAtCell(y, x)),
                            getCellX(x+0.5), getCellY(y+0.5));
                }
            }
        }

        // Groups
        gc.setLineWidth(3);
        gc.setStroke(Color.DARKBLUE);
        for (AbstractGroup g: myPuzzle.getSquareGroups()) {
            gc.strokeRect(getCellX(g.getMinX()), getCellY(g.getMinY()),
                    getCellWidth()*(g.getMaxX()-g.getMinX()),
                    getCellHeight()*(g.getMaxY()-g.getMinY()));
        }

        showPuzzleHints();

        undoButton.setDisable(!myPuzzle.canUndo());

        // TODO: populate puzzles dropdown
        PuzzleDB.getPuzzles();
    }

    public void hintsAction(ActionEvent actionEvent) {
        if ((cbRadiation.isSelected() || cbNakedPairs.isSelected() || cbXWings.isSelected()) & !cbBasicElimination.isSelected()) {
            cbBasicElimination.setSelected(true);
        }
        drawPuzzleOnCanvas();
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
            }

            SolutionContainer loners = sv.getLoneNumbers();
            SolutionContainer unique = sv.getUniqueValues();
            notes.setText("Lone numbers: " + loners.size() + " " + loners +"\n");
            notes.appendText("Unique values: " + unique.size() + " " + unique +"\n");
        }
    }

    // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/canvas/GraphicsContext.html#setTextAlign-javafx.scene.text.TextAlignment-

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
            myPuzzle = prevPuzzle;
            drawPuzzleOnCanvas();
        }
    }

    public void typeAction(ActionEvent actionEvent) {
        System.out.println("Type..." + typeDropdown.getValue());
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
}
