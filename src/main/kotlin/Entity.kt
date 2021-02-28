import java.awt.image.BufferedImage
import javax.imageio.ImageIO

sealed class Entity(initialX: Int, initialY: Int) {

    var x: Int = initialX
    var y: Int = initialY

}

class Sprite(initialX: Int = 0, initialY: Int = 0, imageFileName: String): Entity(initialX, initialY) {
    val image: BufferedImage = ImageIO.read(this.javaClass.getResourceAsStream(imageFileName))
}

interface Clickable {
    fun onClick()
}




//data class Sprite(var x: Int, var y: Int, val imageFileName: String) {
//
//    private val random = Random(System.currentTimeMillis())
//    fun randomRange(min: Int, max: Int): Int {
//        return random.nextInt(max) + min
//    }
//
//
//    private var image: BufferedImage = ImageIO.read(javaClass.classLoader.getResourceAsStream(imageFileName))
//
//    // sprite1.png is 92x74px per frame
//    private val width = 32
//    private val height = 74;
//
//    private var subImage = image.getSubimage(0, 0, width, height)
//
//    private val spriteArrayMax = 5 // 6 = frames 0 - 5
//    private val currentSpriteIndex = AtomicInteger(randomRange(0, 5));
//    private val updateAfterTicks = 8
//    private val currentTick = AtomicInteger(0)
//
//    private val isRunning = AtomicBoolean(false)
//    private val movementAllotment = 2;
//
//    fun move(directions: Set<KeyboardInputAdapter.KeyState>) {
//
//        if (directions.isEmpty()) {
//            isRunning.set(false)
//        }
//
//        directions.forEach { state ->
//
//            when (state) {
//
//                KeyboardInputAdapter.KeyState.MOVE_UP -> {
//                    y -= movementAllotment
//                    isRunning.set(true)
//                }
//
//                KeyboardInputAdapter.KeyState.MOVE_DOWN -> {
//                    y += movementAllotment
//                    isRunning.set(true)
//                }
//
//                KeyboardInputAdapter.KeyState.MOVE_LEFT -> {
//                    x -= movementAllotment
//                    isRunning.set(true)
//                }
//
//                KeyboardInputAdapter.KeyState.MOVE_RIGHT -> {
//                    x += movementAllotment
//                    isRunning.set(true)
//                }
//            }
//        }
//
//    }
//
//    fun update() {
//
//        if (isRunning.get()) {
//
//            if (currentTick.incrementAndGet() == updateAfterTicks) {
//                currentTick.set(0)
//
//                if (currentSpriteIndex.incrementAndGet() > spriteArrayMax) {
//                    currentSpriteIndex.set(0)
//                }
//
//                subImage = image.getSubimage(width * currentSpriteIndex.get(), 0, width, height)
//            }
//        } else {
//            subImage = image.getSubimage(0, height, width, height)
//        }
//    }
//
//    fun render(graphics2D: Graphics2D) {
//        graphics2D.drawImage(subImage, x, y, null)
//    }
//}
//
//data class Entity(var x: Int, var y: Int, var color: Color = Color.WHITE) {
//
//    fun containsPoint(click: MouseClick) { return true }
//
//    companion object {
//        val random = Random(System.currentTimeMillis())
//    }
//
//    fun update() {
//        x += random.nextInt(2) * if (random.nextInt(2) == 1) 1 else -1
//        y += random.nextInt(2) * if (random.nextInt(2) == 1) 1 else -1
//    }
//}