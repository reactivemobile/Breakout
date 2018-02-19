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

public class Main extends Application {

    private static final double WIDTH = 200;
    private static final double HEIGHT = 200;

    private static final double PADDLE_WIDTH = 50;
    private static final double PADDLE_HEIGHT = 5;

    private static final double BALL_RADIUS = 5;
    private static final double BALL_DIAMETER = BALL_RADIUS * 2;

    private static final double BLOCK_WIDTH = 5;
    private static final double BLOCK_HEIGHT = 5;
    private static final int INITIAL_LIVES = 5;

    private BreakoutEngine engine;

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        runGameEngine(scene, gc);
    }

    private void runGameEngine(Scene scene, GraphicsContext gc) {

        engine = new BreakoutEngine(WIDTH,
                HEIGHT,
                BALL_RADIUS,
                PADDLE_WIDTH,
                PADDLE_HEIGHT,
                BLOCK_WIDTH,
                BLOCK_HEIGHT,
                INITIAL_LIVES,
                new BreakoutEngine.GameStateListener() {
                    @Override
                    public void ballMoved(double x, double y) {
                        clearCanvas(gc);
                        gc.setFill(Color.GREEN);
                        gc.fillOval(x - BALL_RADIUS, y - BALL_RADIUS, BALL_DIAMETER, BALL_DIAMETER);
                    }

                    @Override
                    public void paddleMoved(double x, double y) {
                        clearPaddleRect();
                        gc.setFill(Color.BLUE);
                        gc.fillRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
                    }

                    private void clearPaddleRect() {
                        gc.clearRect(0, HEIGHT - PADDLE_HEIGHT, WIDTH, PADDLE_HEIGHT);
                    }

                    @Override
                    public void ballMissedPaddle() {
                        clearPaddleRect();
                        engine.resetBall();
                    }

                    @Override
                    public void gameOver() {
                        engine.pause();
                        showGameOver(gc);
                    }
                });

        scene.setOnMouseMoved(event -> engine.updatePaddleLocation((int) event.getX()));

        scene.setOnMouseClicked(event -> {
            if (!engine.getRunning()) {
                clearCanvas(gc);
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
    }

    private void showGameOver(GraphicsContext gc) {
        clearCanvas(gc);
        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", WIDTH / 2, HEIGHT / 4);
    }

    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT - PADDLE_HEIGHT);
    }
}