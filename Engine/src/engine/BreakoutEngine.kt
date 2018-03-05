package engine

class BreakoutEngine(var width: Double, var height: Double, val ballRadius: Double, val paddleWidth: Double, val paddleHeight: Double, val blockColumns: Int, val blockRows: Int, val numLives: Int, val gameStateListener: GameStateListener) {
    internal var ball: Ball = Ball(width / 2, height / 2, 1.0, 1.0, ballRadius)
    var paddleX: Double = 0.0
    var paddleY: Double = height - paddleHeight

    val blockWidth = width / blockColumns
    val blockHeight = (width / 2) / blockRows
    var blocks: Array<Block> = Array(blockColumns * blockRows, { it -> Block((it % blockColumns).toDouble() * blockWidth, (it / blockColumns).toDouble() * blockHeight, blockWidth, blockHeight, BlockState.NEW) })

    var running = true
    var lives = numLives
    fun resetBall() {
        ball.x = width / 2
        ball.y = width / 2
    }

    fun resetGame() {
        resetBall()
        resetLives()
        resetBlocks()
    }

    private fun resetBlocks() {

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
            stepBlocks()
        }
    }

    private fun stepBlocks() {
        for (block in blocks) {
            println(block.toString())
            block.checkHit(ball)
            gameStateListener.blockUpdated(block)
        }
    }

    fun updatePaddleLocation(value: Double) {
        if (running && (value > 0.0 && value < (width - paddleWidth))) {
            paddleX = value
            gameStateListener.paddleMoved(value, paddleY)
        }
    }

    inner class Ball(var ballX: Double, var ballY: Double, var velocityX: Double, var velocityY: Double, val radius: Double) : Rectangle(ballX, ballY, radius * 2, radius * 2) {
        fun step() {
            x += velocityX
            y += velocityY

            if (x >= width - radius || x <= radius) {
                bounceHorizontal()
            }

            if (y >= height - radius - paddleHeight) {
                if (x > paddleX && x < paddleX + paddleWidth) {
                    bounceVertical()
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
                bounceVertical()
            }
        }

        fun bounceHorizontal() {
            velocityX = -velocityX
        }

        fun bounceVertical() {
            velocityY = -velocityY
        }
    }

    interface GameStateListener {
        fun ballMoved(x: Double, y: Double)
        fun paddleMoved(x: Double, y: Double)
        fun blockUpdated(block: Block)
        fun ballMissedPaddle()
        fun numberOfLivesChanged(lives: Int)
        fun gameOver()
    }

    class Block(blockX: Double, blockY: Double, blockWidth: Double, blockHeight: Double, var blockState: BlockState) : Rectangle(blockX, blockY, blockWidth, blockHeight) {
        var beingHit = false
        fun checkHit(ball: Ball) {
            println("hit = ${intersects(ball)} blockState = ${blockState.name}")
            if (intersects(ball)) {
                if (!beingHit) {
                    if (blockState == BlockState.NEW) {
                        blockState = BlockState.HIT
                    } else if (blockState == BlockState.HIT) {
                        blockState = BlockState.DESTROYED
                    }
                    beingHit = true;
                }

            } else {
                beingHit = false
            }
        }

        override fun toString(): String {
            return "Block(x=$x, y=$y, gameWidth=$w, gameHeight=$h, blockState=$blockState)"
        }

    }

    enum class BlockState {
        NEW,
        HIT,
        DESTROYED
    }
}

open class Rectangle(var x: Double, var y: Double, val w: Double, val h: Double) {
    fun intersects(r: Rectangle): Boolean {
        return x < r.x + r.w && x + w > r.x && y < r.y + r.h && y + h > r.y;
    }
}

