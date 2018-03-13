package engine

class BreakoutEngine(var width: Double,
                     var height: Double,
                     ballRadius: Double,
                     val paddleWidth: Double,
                     val paddleHeight: Double,
                     val blockColumns: Int,
                     blockRows: Int,
                     val numLives: Int,
                     val gameStateListener: GameStateListener) {

    private var ball: Ball = Ball(width / 2, height / 2, 1.0, 1.0, ballRadius)
    var paddleX: Double = 0.0
    var paddleY: Double = height - paddleHeight

    private val blockWidth = width / blockColumns
    private val blockHeight = (width / 2) / blockRows
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
        // TODO
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

        override fun hitHorizontal() {
            bounceHorizontal()
        }

        override fun hitVertical() {
            bounceVertical()
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
            if (blockState != BlockState.DESTROYED && intersects(ball)) {
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
    }

    enum class BlockState {
        NEW,
        HIT,
        DESTROYED
    }
}

open class Rectangle(var x: Double, var y: Double, val w: Double, val h: Double) {
    fun intersects(r: Rectangle): Boolean {

        val hit = x < r.x + r.w
                && x + w > r.x
                && y < r.y + r.h
                && y + h > r.y;

        if (hit) {
            // Get the intersection rectangle to find out which way to bounce.
            val iRect = intersection(r)

            if (x + w / 2 < iRect.x + iRect.w / 2) {
                r.hitHorizontal()
            } else if (x + w / 2 > iRect.x + iRect.w / 2) {
                r.hitHorizontal()
            } else if (y + h / 2 < iRect.y + iRect.h / 2) {
                r.hitVertical()
            } else if (y + h / 2 > iRect.y + iRect.h / 2) {
                r.hitVertical()
            }
        }
        return hit
    }

    open fun hitHorizontal() {
    }

    open fun hitVertical() {
    }


    private fun intersection(r: Rectangle): Rectangle {
        var tx1 = this.x
        var ty1 = this.y
        val rx1 = r.x
        val ry1 = r.y
        var tx2 = tx1
        tx2 += this.w
        var ty2 = ty1
        ty2 += this.h
        var rx2 = rx1
        rx2 += r.w
        var ry2 = ry1
        ry2 += r.h
        if (tx1 < rx1) tx1 = rx1
        if (ty1 < ry1) ty1 = ry1
        if (tx2 > rx2) tx2 = rx2
        if (ty2 > ry2) ty2 = ry2
        tx2 -= tx1
        ty2 -= ty1
        // tx2,ty2 will never overflow (they will never be
        // larger than the smallest of the two source w,h)
        // they might underflow, though...
        if (tx2 < Integer.MIN_VALUE) tx2 = Integer.MIN_VALUE.toDouble()
        if (ty2 < Integer.MIN_VALUE) ty2 = Integer.MIN_VALUE.toDouble()
        return Rectangle(tx1, ty1, tx2, ty2)
    }

    override fun toString(): String {
        return "Rectangle(x=$x, y=$y, w=$w, h=$h)"
    }
}

