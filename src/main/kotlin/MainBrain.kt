import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean

fun main(args: Array<String>) {
    MainBrain()
}

@ExperimentalCoroutinesApi
@FlowPreview
class MainBrain {

    private val width = 1000
    private val height = 1100
    private val imageState = MutableStateFlow(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB))

    private val keyInputState = MutableStateFlow<Set<KeyboardInputAdapter.KeyState>>(setOf())
    private val keyListener = KeyboardInputAdapter(keyInputState)

    private val hexMap = HexMap(width, height, 11, 12, 50)

    private val gameFrame = GameFrame("Let's try strategy and animations! 2021!", width, height, imageState)

    private val gameStateFlow = MutableStateFlow(GameState(HexMap(1, 1, 1, 1)))

    init {

        val isPaused = AtomicBoolean(false)


        hexMap.generateRegions(7)


//        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 0, 0)
//        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 1, 0)
//        hexMap.assignEntityToHex(Sprite(imageFileName = "soldier_1.png"), 2, 0)

//        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 0, 4)
//        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 1, 4)
//        hexMap.assignEntityToHex(Sprite(imageFileName = "crab_2.png"), 2, 4)

        gameStateFlow.value = GameState(hexMap)

        gameFrame.setKeyListener(keyListener)
        gameFrame.setMouseAdapter(mouseActionAdapter)
        gameFrame.showFrame()

        gameStateFlow.value.update()

        mouseActionFlow.onEach { click ->

            val state = gameStateFlow.value

            when (click.type) {
                MouseClickType.MOUSE_CLICK_PRIMARY_DOWN -> {
                    gameStateFlow.value = state.processPrimaryClick(click)
                    gameStateFlow.value.hexMap.renderGameState(gameStateFlow.value)
                }

                MouseClickType.MOUSE_CLICK_SECONDARY_DOWN -> {
                    gameStateFlow.value = state.processSecondaryClick(click)
                    gameStateFlow.value.hexMap.renderGameState(gameStateFlow.value)
                }

                MouseClickType.MOUSE_CLICK_SECONDARY_UP -> {
                    hexMap.generateRegions(7)
                    gameStateFlow.value = state.clearSecondaryAction()
                    gameStateFlow.value.hexMap.renderGameState(gameStateFlow.value)
                }

                else -> {

                }
            }
        }.launchIn(GlobalScope)



        while (true) {

            if (!isPaused.get()) {
                update()
                render()
            }

            Thread.sleep(1000 / 60)
        }
    }

    private fun update() {
        val state = gameStateFlow.value
        gameStateFlow.value = state.update()
    }

    private fun render() {
        gameStateFlow.value.hexMap.renderGameState(gameStateFlow.value)
        gameFrame.drawImage(hexMap.cachedImage.value)
    }
}
