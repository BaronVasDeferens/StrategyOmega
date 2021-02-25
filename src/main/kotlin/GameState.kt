import java.awt.image.BufferedImage

enum class GamePhase {
    PLAYER_MOVE,
    BLOCKING_ANIMATION,
    AI_MOVE
}

data class GameState(
    val hexMap: HexMap,
    val selectedHex: Hex? = null,
    val highlightedHexes: Set<Hex> = setOf(),
    val animations: List<MovementAnimation> = listOf()
) {



    fun processClick(click: MouseClick): GameState {

        val hexAtClick = hexMap.getHexAtClick(click)
        val residentEntity = hexMap.getEntityAtClick(click)


        if (selectedHex == null) {
            // No hex is selected.
            if (residentEntity != null) {
                // User has clicked on an occupied hex. Select that hex and highlight neighbors
                println("Entity chosen. Showing selection and highlighting neighbors...")
                return this.copy(
                    selectedHex = hexAtClick,
                    highlightedHexes = hexMap.findAllAdjacentHexesTo(hexAtClick, 1) // TODO depth depends on unit, unit type
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
                    return this.copy(selectedHex = null, highlightedHexes = setOf(), animations = animations.plus(MovementAnimation(entityInSelectedHex, selectedHex, hexAtClick!!)))
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
        return this
    }
}

data class MovementAnimation(val sprite: Sprite,
                             val originHex: Hex,
                             val destinationHex: Hex) {

    // TODO: include some flag to indicate that this is finished so that it can be removed

    private val x: Int = sprite.x
    private val y: Int = sprite.y

    fun updateAnimation() {

    }

    fun drawImage(): BufferedImage {
        return sprite.image
    }

    fun onComplete() {

    }

}
