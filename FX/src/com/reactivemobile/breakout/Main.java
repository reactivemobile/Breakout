package com.reactivemobile.breakout;

import engine.BreakoutEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class Main extends Application implements BreakoutEngine.GameStateListener {

    private static final double GAME_WIDTH = 200;
    private static final double GAME_HEIGHT = 200;

    private static final double FOOTER_HEIGHT = 20;
    private static final double CANVAS_HEIGHT = FOOTER_HEIGHT + GAME_HEIGHT;

    private static final double PADDLE_WIDTH = 50;
    private static final double PADDLE_HEIGHT = 5;

    private static final double BALL_RADIUS = 5;
    private static final double BALL_DIAMETER = BALL_RADIUS * 2;

    private static final int BLOCK_ROWS = 2;
    private static final int BLOCK_COLUMNS = 4;
    private static final int INITIAL_LIVES = 5;

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

        engine = new BreakoutEngine(GAME_WIDTH,
                GAME_HEIGHT,
                BALL_RADIUS,
                PADDLE_WIDTH,
                PADDLE_HEIGHT,
                BLOCK_ROWS,
                BLOCK_COLUMNS,
                INITIAL_LIVES,
                this);

        scene.setOnMouseMoved(event -> engine.updatePaddleLocation((int) event.getX()));

        scene.setOnMouseClicked(event -> {
            if (!engine.getRunning()) {
                clearCanvas();
                engine.resetGame();
                engine.resume();
            }
        });

        numberOfLivesChanged(INITIAL_LIVES);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                engine.step();
            }
        }.start();

        engine.pause();
    }

    private void clearPaddleRect() {
        gc.clearRect(0, GAME_HEIGHT - PADDLE_HEIGHT, GAME_WIDTH, PADDLE_HEIGHT);
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, GAME_WIDTH, GAME_HEIGHT - PADDLE_HEIGHT);
    }

    @Override
    public void ballMoved(double x, double y) {
        clearCanvas();
        gc.setFill(Color.DODGERBLUE);
        gc.fillOval(x - BALL_RADIUS, y - BALL_RADIUS, BALL_DIAMETER, BALL_DIAMETER);
    }

    @Override
    public void paddleMoved(double x, double y) {
        clearPaddleRect();
        gc.setFill(Color.DEEPSKYBLUE);
        gc.fillRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
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
        gc.setFill(Color.POWDERBLUE);
        gc.fillRect(0, GAME_HEIGHT, GAME_WIDTH, FOOTER_HEIGHT);
        gc.setFill(Color.DARKBLUE);
        gc.fillText("Lives: " + lives, 4, CANVAS_HEIGHT - 6);
    }

    @Override
    public void gameLose() {
        gc.setFill(Color.RED);
        gc.fillRect(0, GAME_HEIGHT, GAME_WIDTH, FOOTER_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.fillText("GAME OVER!", 4, CANVAS_HEIGHT - 6);
    }

    @Override
    public void gameWin() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, GAME_HEIGHT, GAME_WIDTH, FOOTER_HEIGHT);
        gc.setFill(Color.BLACK);
        gc.fillText("You Win!", 4, CANVAS_HEIGHT - 6);
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