package engine

class BreakoutEngine(var width: Double, var height: Double, val ballRadius: Double, val paddleWidth: Double, val paddleHeight: Double, val blockWidth: Double, val blockHeight: Double, val numLives: Int, val gameStateListener: GameStateListener) {
    internal var ball: Ball = Ball(width / 2, height / 2, 1.0, 1.0, width, height, ballRadius)
    var paddleX: Double = 0.0
    var paddleY: Double = height - paddleHeight

    var running = true
    var lives = numLives
    fun resetBall() {
        ball.x = width / 2
        ball.y = height / 2
    }

    fun resetGame() {
        resetBall()
        resetLives()
    }

    private fun resetLives() {
        lives = numLives
        notifyNumLivesChanged()
    }

    private fun notifyNumLivesChanged() {
        gameStateListener.numberOfLivesChanged(lives)
    }

    fun resume() {
        running = true
    }

    fun step() {
        if (running) {
            gameStateListener.ballMoved(ball.x, ball.y)
            ball.step()
        }
    }

    fun updatePaddleLocation(value: Double) {
        if (running && (value > 0.0 && value < (width - paddleWidth))) {
            paddleX = value
            gameStateListener.paddleMoved(value, paddleY)
        }
    }

    inner class Ball(var x: Double, var y: Double, var velocityX: Double, var velocityY: Double, val width: Double, val height: Double, val radius: Double) {
        fun step() {
            x += velocityX
            y += velocityY

            if (x >= width - radius || x <= radius) {
                velocityX = -velocityX
            }

            if (y >= height - radius - paddleHeight) {
                if (x > paddleX && x < paddleX + paddleWidth) {
                    velocityY = -velocityY
                }
                if (y >= height - radius) {
                    lives--
                    notifyNumLivesChanged()
                    if (lives == 0) {
                        running = false
                        gameStateListener.gameOver()
                    } else {
                        gameStateListener.ballMissedPaddle()
                    }
                }
            }
            if (y <= radius) {
                velocityY = -velocityY
            }
        }
    }

    interface GameStateListener {
        fun ballMoved(x: Double, y: Double)
        fun paddleMoved(x: Double, y: Double)
        fun ballMissedPaddle()
        fun numberOfLivesChanged(lives: Int)
        fun gameOver()
    }
}

