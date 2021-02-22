import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    MainBrain()
}

@FlowPreview
class MainBrain {

    private val width = 1000
    private val height = 1100
    private val imageState = MutableStateFlow(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB))

    private val keyInputState = MutableStateFlow<Set<KeyboardInputAdapter.KeyState>>(setOf())
    private val keyListener = KeyboardInputAdapter(keyInputState)

    private val hexMap = HexMap(width, height, 5, 5, 100)

    private val entities = mutableListOf<Entity>()
    private val sprites = mutableListOf<Sprite>()

    val gameFrame = GameFrame("Let's try and animations! 2021", width, height, imageState)


    init {

        val isPaused = AtomicBoolean(false)

        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 0,0)
        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 1,0)
        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 2,0)

        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 0,4)
        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 1,4)
        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 2,4)


        gameFrame.setKeyListener(keyListener)
        gameFrame.setMouseAdapter(mouseClickAdapter)
        gameFrame.showFrame()

        mouseClickChannel.onEach { click ->
            when (click.type) {
                MouseClickType.MOUSE_CLICK_PRIMARY_DOWN -> {
                    val hex = hexMap.getHexAtClick(click)
                    val entity = hexMap.getEntityAtClick(click)
                    println("$hex $entity")
                }
                else -> {

                }
            }
        }.launchIn(GlobalScope)



        while (true) {

            // TODO: process input




            if (!isPaused.get()) {
                update()
                render()
            }

            Thread.sleep(1000 / 60)
        }
    }

    private fun update() {
//        entities.forEach { entity ->
//            entity.update()
//        }
//
//        sprites.forEach { sprite ->
//            sprite.update()
//        }
//
//        playerSprite.update()
    }

    @ExperimentalCoroutinesApi
    private fun render() {
        // val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val image = hexMap.cachedImage.value
        //val g = image.graphics as Graphics2D
//        g.color = Color.BLACK
//        g.fillRect(0, 0, width, height)


//        entities.forEach { entity ->
//            g.color = entity.color
//            g.drawRect(entity.x, entity.y, 5, 5)
//        }

//        sprites.forEach { sprite ->
//            sprite.render(g)
//        }
//
//        playerSprite.render(g)

        //g.dispose()

        // imageState.value = image
        gameFrame.drawImage(image)
    }
}