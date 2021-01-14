package ottop.sudoku.fx;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.puzzle.ISudoku;

import java.util.Set;

public class FxUtils {
    public static void drawGroup(Canvas canvas, ISudoku p, AbstractGroup g) {
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
        gc.strokeRect(getCellX(canvas, p, xMin), getCellY(canvas, p, yMin),
                (xMax-xMin+1)*getCellWidth(canvas, p), (yMax-yMin+1)*getCellHeight(canvas, p));

    }

    public static void drawPuzzleOnCanvas(Canvas canvas, ISudoku p, Coord highlight, Set<Coord> highlightedSubArea) {
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
        Set<Coord> buddies = p.getBuddies(highlight);

        // Background of individual cells
        for (Coord c: p.getAllCells()) {
            Color background;
            if (p.isAtOverlay(c)) {
                background = Color.SEAGREEN;
                if (highlightedSubArea != null && highlightedSubArea.contains(c)) background = Color.BURLYWOOD;
                else if (highlight != null && p.getBuddies(highlight).contains(c)) background = Color.DARKGREEN;
            } else {
                background = Color.WHITE;
                if (highlightedSubArea != null && highlightedSubArea.contains(c)) background = Color.BURLYWOOD;
                else if (highlight != null && p.getBuddies(highlight).contains(c)) background = Color.LIGHTGRAY;
            }
            gc.setFill(background);
            gc.fillRect(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
                    getCellWidth(canvas, p), getCellHeight(canvas, p));
        }

        // Text in cells
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        for (Coord c : p.getAllCells()) {
            gc.setStroke(Color.BLUE);
            gc.strokeRect(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
                    getCellWidth(canvas, p), getCellHeight(canvas, p));
            if (p.isOccupied(c)) {
                if (c.equals(highlight)) {
                    gc.setStroke(Color.DARKGRAY);
                } else {
                    gc.setStroke(Color.BLACK);
                }
                gc.strokeText(p.getSymbolAtCoordinates(c),
                        getCellX(canvas, p, c.getX() + 0.5), getCellY(canvas, p, c.getY() + 0.5));
            }
        }

        // Group boundaries
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (AbstractGroup g: p.getGroupsWithVisualBoundary()) {
            drawGroup(canvas, p, g);
        }
    }

    public static void drawPossibilities(Canvas canvas, ISudoku p, Coord c, Set<Integer> symbolCodes) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int n = p.getSymbolCodeRange() - 1; // minus empty cell code
        int nmarkerrows = (int) Math.sqrt(n);
        int nmarkercols = (int) Math.ceil(n / (double) nmarkerrows);

        for (int symbolCode : symbolCodes) {
            int subrow = (symbolCode - 1) / nmarkercols;
            int subcol = (symbolCode - 1) % nmarkercols;
            gc.strokeText(p.symbolCodeToSymbol(symbolCode),
                    getCellX(canvas, p, c.getX() + 0.15 + 0.7 * subcol / (double) (nmarkercols - 1)),
                    getCellY(canvas, p, c.getY() + 0.15 + 0.7 * subrow / (double) (nmarkerrows - 1)));
        }
    }



    static double getCellX(Canvas canvas, ISudoku p, double x) {
        return (5 + x * getCellWidth(canvas, p));
    }

    static double getCellY(Canvas canvas, ISudoku p, double y) {
        return (5 + y * getCellHeight(canvas, p));
    }

    static double getCellWidth(Canvas canvas, ISudoku p) {
        double canvasWidth = canvas.getWidth();
        return (canvasWidth - 10) / p.getWidth();
    }

    static double getCellHeight(Canvas canvas, ISudoku p) {
        double canvasHeight = canvas.getHeight();
        return (canvasHeight - 10) / p.getHeight();
    }


}
