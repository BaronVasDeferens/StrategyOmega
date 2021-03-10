import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.sqrt

enum class GamePhase {
    PLAYER_MOVE,
    ANIMATING,
    AI_MOVE
}

data class GameState(
    val hexMap: HexMap,
    val selectedHex: Hex? = null,
    val highlightedHexes: Set<Hex> = setOf(),
    val animations: List<AnimateHexToHexMove> = listOf()
) {


    fun processClick(actionEvent: MouseActionEvent): GameState {

//        if (phase == GamePhase.ANIMATING || phase == GamePhase.AI_MOVE) {
//            return this
//        }

        val hexAtClick = hexMap.getHexAtClick(actionEvent)
        val residentEntity = hexMap.getEntityAtClick(actionEvent)

        if (selectedHex == null) {
            // No hex is selected.
            if (residentEntity != null) {
                // User has clicked on an occupied hex. Select that hex and highlight neighbors
                println("Entity chosen. Showing selection and highlighting neighbors...")
                return this.copy(
                    selectedHex = hexAtClick,
                    highlightedHexes = hexMap.findAllAdjacentHexesTo(
                        hexAtClick,
                        1
                    ) // TODO depth depends on unit, unit type
                )
            } else {
                // No hex is selected. Hex clicked has no entity.
                println("No selected hex/entity...")
                return this.copy(selectedHex = null, highlightedHexes = setOf())
            }
        } else {
            // There is a selected hex

            if (hexAtClick == selectedHex) {
                // Clicked hex is the selected hex. Deselect all.
                println("Highlighted hex clicked again. Deselect all...")
                return this.copy(selectedHex = null, highlightedHexes = setOf())
            }

            if (highlightedHexes.contains(hexAtClick)) {
                // The clicked hex is highlighted
                if (hexMap.getEntityForHex(hexAtClick) != null) {
                    // The highlighted hex contains an entity
                    // TODO: examine turn state here
                    println("Selected entity performs action on entity in clicked hex...TODO")
                    return this
                } else {
                    // Clicked hex was highlighted, but contained no entity.
                    // TODO: perform move
                    val entityInSelectedHex = hexMap.getEntityForHex(selectedHex) as Sprite
                    println("Move: $entityInSelectedHex from $selectedHex to $hexAtClick")
                    return this.copy(
                        selectedHex = null,
                        highlightedHexes = setOf(),
                        animations = animations.plus(
                            AnimateHexToHexMove(
                                entityInSelectedHex,
                                selectedHex,
                                hexAtClick!!,
                            ) {
                                hexMap.assignEntityToHex(entityInSelectedHex, hexAtClick)
                            })
                    )
                }
            } else {
                // Clicked hex was not highlighted. De-select/unhighlight everything
                println("Highlighted entity cannot do that. Deselect all...")
                return this.copy(selectedHex = null, highlightedHexes = setOf())
            }
        }
    }

    fun update(): GameState {
        animations.forEach { it.updateAnimation() }
        return this.copy(animations = animations.toMutableList().filterNot { it.isComplete })
    }
}


data class AnimateHexToHexMove(
    val sprite: Sprite,
    val originHex: Hex,
    val destinationHex: Hex,
    val onCompleteFun: () -> Unit
) {

    var x: Int = 0
    var y: Int = 0

    var deltaX: Int = 0
    var deltaY: Int = 0

    var isComplete: Boolean = false
    var count = 0
    var maxCount = 60

    // TODO: include some flag to indicate that this is finished so that it can be removed
    init {
        val centeredOrigin = findCenteredCoordinatesForSprite(originHex, sprite)
        val centeredDestination = findCenteredCoordinatesForSprite(destinationHex, sprite)

        val xDiff = (centeredOrigin.first - centeredDestination.first)
        val yDiff = (centeredOrigin.second - centeredDestination.second)

        println(">>> cenOrg: $centeredOrigin centDest: $centeredDestination")
        println(">>> xDiff: $xDiff yDiff: $yDiff")

        val hypotenuse = sqrt(abs(xDiff * xDiff).toDouble() + abs(yDiff * yDiff)) .toInt()
        // maxCount = abs(hypotenuse)

        deltaX = xDiff / maxCount * -1
        deltaY = yDiff / maxCount * -1

        x = centeredOrigin.first
        y = centeredOrigin.second

        println(">>> hypotenuse: $hypotenuse dx: $deltaX dy: $deltaY maxCount: $maxCount")
    }



    fun updateAnimation() {

        x += deltaX
        y += deltaY

        count++
        if (count >= maxCount) {
            isComplete = true
            onCompleteFun()
        }
    }

    fun drawImage(): BufferedImage {
        return sprite.image
    }
}
