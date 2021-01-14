package ottop.sudoku.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.StandardSudoku;

public class SudokuFx extends Application {

    private Controller myController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = FXMLLoader.load(getClass().getResource("/SudokuUI.fxml"));

        myController = Controller.theController;
        myController.initialize();
        myController.newPuzzle(new StandardSudoku("Empty Standard Sudoku"));

        primaryStage.setTitle("U-Kudos");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
