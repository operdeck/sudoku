package ottop.sudoku.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ottop.sudoku.IPuzzle;
import ottop.sudoku.NRCPuzzle;

public class Sudoku extends Application {

    private Controller myController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("SudokuUI.fxml"));

        myController = Controller.theController;
        myController.setPuzzle(initPuzzle());
        myController.showPuzzleHints();

        primaryStage.setTitle("Sudoku");
        primaryStage.setScene(new Scene(root, root.getBoundsInParent().getMaxX(), root.getBoundsInParent().getMaxY()));
        primaryStage.show();
    }

    private IPuzzle initPuzzle() {
        IPuzzle myPuzzle = new NRCPuzzle("NRC_5dec14", new String[] {
                "....65...",
                ".......6.",
                "1......78",
                ".........",
                "..27.....",
                ".3..9...1",
                "..6..45..",
                ".8...2...",
                "........." });

        return myPuzzle;
    }
}
