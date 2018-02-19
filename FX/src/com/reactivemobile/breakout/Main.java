package com.reactivemobile.breakout;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import engine.BreakoutEngine;

public class Main extends Application {

    private static final double WIDTH = 200;
    private static final double HEIGHT = 200;

    private static final double PADDLE_WIDTH = 50;
    private static final double PADDLE_HEIGHT = 5;

    private static final double BALL_RADIUS = 5;
    private static final double BALL_DIAMETER = BALL_RADIUS * 2;

    private static final double BLOCK_WIDTH = 5;
    private static final double BLOCK_HEIGHT = 5;

    private BreakoutEngine engine;

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        engine = new BreakoutEngine(WIDTH,
                HEIGHT,
                BALL_RADIUS,
                PADDLE_WIDTH,
                PADDLE_HEIGHT,
                BLOCK_WIDTH,
                BLOCK_HEIGHT,
                new BreakoutEngine.GameStateListener() {
                    @Override
                    public void ballMoved(double x, double y) {
                        gc.clearRect(0, 0, WIDTH, HEIGHT - PADDLE_HEIGHT);
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
                        engine.reset();
                    }
                });

        scene.setOnMouseMoved(event -> engine.updatePaddleLocation((int) event.getX()));

        primaryStage.show();
        runGameEngine();
    }

    private void runGameEngine() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                engine.step();
            }
        }.start();
    }
}






