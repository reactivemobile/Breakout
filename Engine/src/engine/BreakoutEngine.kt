package engine

class BreakoutEngine(var canvasWidth: Double,
                     var canvasHeight: Double,
                     ballRadius: Double,
                     val paddleWidth: Double,
                     val paddleHeight: Double,
                     private val blockColumns: Int,
                     blockRows: Int,
                     private val numLives: Int,
                     private val gameStateListener: GameStateListener) {

    private var ball: Ball = Ball(canvasWidth / 2, canvasHeight / 2, 1.0, 1.0, ballRadius)
    private var paddleX: Double = 0.0
    private var paddleY: Double = canvasHeight - paddleHeight
    private val blockWidth = canvasWidth / blockColumns
    private val blockHeight = (canvasWidth / 2) / blockRows
    private var blockCount = blockColumns * blockRows
    private var blocks: Array<Block> = Array(blockCount, { it -> Block((it % blockColumns).toDouble() * blockWidth, (it / blockColumns).toDouble() * blockHeight, blockWidth, blockHeight, BlockState.NEW) })

    var running = true
    private var lives = numLives

    fun resetGame() {
        resetBall()
        resetLives()
        resetBlocks()
    }

    fun resetBall() {
        ball.x = canvasWidth / 2
        ball.y = canvasWidth / 2
    }

    private fun resetBlocks() {
        for (block in blocks) {
            block.blockState = BlockState.NEW
        }
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

    private fun pause() {
        running = false
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
            block.checkHit(ball)
            gameStateListener.blockUpdated(block)
        }
    }

    fun updatePaddleLocation(value: Double) {
        if (running && (value > 0.0 && value < (canvasWidth - paddleWidth))) {
            paddleX = value
            gameStateListener.paddleMoved(value, paddleY)
        }
    }

    inner class Ball(ballX: Double,
                     ballY: Double,
                     var velocityX: Double,
                     var velocityY: Double,
                     private val radius: Double) : Rectangle(ballX, ballY, radius * 2, radius * 2) {
        fun step() {
            x += velocityX
            y += velocityY

            if (x >= canvasWidth - radius || x <= radius) {
                bounceHorizontal()
            }

            if (y + velocityY >= canvasHeight - radius - paddleHeight) {
                if (x > paddleX && x < paddleX + paddleWidth) {
                    bounceVertical()
                }
                if (y + velocityY >= canvasHeight - radius) {
                    handleBallMissedPaddle()
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

    private fun handleBallMissedPaddle() {
        lives--
        notifyNumLivesChanged()
        if (lives == 0) {
            pause()
            gameStateListener.gameLose()
        } else {
            gameStateListener.ballMissedPaddle()
        }
    }

    interface GameStateListener {
        fun ballMoved(x: Double, y: Double)
        fun paddleMoved(x: Double, y: Double)
        fun blockUpdated(block: Block)
        fun ballMissedPaddle()
        fun numberOfLivesChanged(lives: Int)
        fun gameLose()
        fun gameWin()
    }

    inner class Block(blockX: Double, blockY: Double, blockWidth: Double, blockHeight: Double, var blockState: BlockState) : Rectangle(blockX, blockY, blockWidth, blockHeight) {
        fun checkHit(ball: Ball) {
            if (blockState == BlockState.DESTROYED)
                return

            if (ball.x + ball.w + ball.velocityX > x
                    && ball.x + ball.velocityX < x + w
                    && ball.y + ball.h > y
                    && ball.y < y + h) {
                ball.bounceHorizontal()
                hit()
            }

            if (ball.x + ball.w > x
                    && ball.x < x + w
                    && ball.y + ball.h + ball.velocityY > y
                    && ball.y + ball.velocityY < y + h) {
                ball.bounceVertical()
                hit()
            }
        }

        private fun hit() {
            if (blockState == BlockState.NEW) {
                blockState = BlockState.HIT
            } else if (blockState == BlockState.HIT) {
                blockState = BlockState.DESTROYED
                blockCount--
                if (blockCount == 0) {
                    handleWin()
                }
            }
        }

        override fun toString(): String {
            return "Block(x=$x, y=$y, gameWidth=$w, gameHeight=$h, blockState=$blockState)"
        }
    }

    private fun handleWin() {
        pause()
        gameStateListener.gameWin()
    }

    enum class BlockState {
        NEW,
        HIT,
        DESTROYED
    }
}

open class Rectangle(var x: Double, var y: Double, val w: Double, val h: Double)

