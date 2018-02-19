package engine

class BreakoutEngine(var width: Double, var height: Double, val ballRadius: Double, val paddleWidth: Double, val paddleHeight: Double, val blockWidth: Double, val blockHeight: Double, val gameStateListener: GameStateListener) {
    private var ball: Ball = Ball(width / 2, height / 2, 1.0, 1.0, width, height, ballRadius)
    var paddleX: Double = 0.0
    var paddleY: Double = height - paddleHeight

    fun reset() {
        ball.x = width / 2
        ball.y = height / 2
    }

    fun step() {
        gameStateListener.ballMoved(ball.x, ball.y)
        ball.step()
    }

    fun updatePaddleLocation(value: Double) {
        if ((value > 0.0 && value < (width - paddleWidth))) {
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
                    gameStateListener.ballMissedPaddle()
                }
            }
            if (y <= radius) {
                velocityY = -velocityY
            }
        }

        override fun toString(): String {
            val stringBuilder = StringBuilder()

            val horizontalWall = "X".repeat(width.toInt())
            stringBuilder.append("X${horizontalWall}X  Ball=($x, $y)").append('\n')
            for (i in 0 until height.toInt()) {
                val row = CharArray(width.toInt())
                row.fill(' ')
                if (i == y.toInt()) {
                    row[x.toInt()] = 'o'
                }

                stringBuilder.append("X${row}X").append('\n')
            }
            stringBuilder.append("X${horizontalWall}X").append('\n')
            return stringBuilder.toString()
        }
    }

    interface GameStateListener {
        fun ballMoved(x: Double, y: Double)
        fun paddleMoved(x: Double, y: Double)
        fun ballMissedPaddle()
    }
}

