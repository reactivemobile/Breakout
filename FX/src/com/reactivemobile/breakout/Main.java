package com.reactivemobile.breakout;

import engine.BreakoutEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class Main extends Application implements BreakoutEngine.GameStateListener {

    private static final double GAME_WIDTH = 240;
    private static final double GAME_HEIGHT = 240;
    private static final double FOOTER_HEIGHT = 20;
    private static final double CANVAS_HEIGHT = FOOTER_HEIGHT + GAME_HEIGHT;

    private BreakoutEngine engine;
    private GraphicsContext gc;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Canvas canvas = new Canvas(GAME_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        runGameEngine(scene);
    }

    private void runGameEngine(Scene scene) {
        engine = new BreakoutEngine(GAME_WIDTH, GAME_HEIGHT, this);

        scene.setOnMouseMoved(event -> engine.updatePaddleLocation((int) event.getX()));
        scene.setOnMouseClicked(event -> {
            if (!engine.getRunning()) {
                clearCanvas();
                engine.resetGame();
                engine.resume();
            }
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                engine.step();
            }
        }.start();
        showBottomMessage("Click to Start!");
        engine.pause();
    }

    private void clearPaddleRect() {
        gc.clearRect(0, GAME_HEIGHT - engine.getPaddleHeight(), GAME_WIDTH, engine.getPaddleHeight());
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, GAME_WIDTH, GAME_HEIGHT - engine.getPaddleHeight());
    }

    @Override
    public void ballMoved(double x, double y, double radius) {
        clearCanvas();
        gc.setFill(Color.DODGERBLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void paddleMoved(double x, double y, double w, double h) {
        clearPaddleRect();
        gc.setFill(Color.DEEPSKYBLUE);
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void ballMissedPaddle() {
        gc.setFill(Color.RED);
        gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        clearPaddleRect();
        engine.resetBall();
    }

    @Override
    public void numberOfLivesChanged(int lives) {
        showBottomMessage("Lives: " + lives);
    }

    @Override
    public void gameLose() {
        showBottomMessage("** Game Over - Click to restart **");
    }

    @Override
    public void gameWin() {
        showBottomMessage("!! You Win !!");
    }

    private void showBottomMessage(String text) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, GAME_HEIGHT, GAME_WIDTH, FOOTER_HEIGHT);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, GAME_WIDTH / 2, CANVAS_HEIGHT - 6);
    }

    @Override
    public void blockUpdated(@NotNull BreakoutEngine.Block block) {
        Color colour = Color.BLACK;
        switch (block.getBlockState()) {
            case NEW:
                colour = Color.SLATEBLUE;
                break;
            case HIT:
                colour = Color.STEELBLUE;
                break;
            case DESTROYED:
                colour = Color.TRANSPARENT;
                break;
        }
        gc.setFill(colour);
        gc.fillRect(block.getX() + 1, block.getY() + 1, block.getW() - 2, block.getH() - 2);
    }
}