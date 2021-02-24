enum class GamePhase {
    PLAYER_MOVE,
    BLOCKING_ANIMATION,
    AI_MOVE
}

data class GameState(
    val hexMap: HexMap,
    val selectedHex: Hex? = null,
    val highlightedHexes: Set<Hex> = setOf()
) {


    fun processClick(click: MouseClick): GameState {
        return if (selectedHex == null) {
            val hexAtClick = hexMap.getHexAtClick(click)
            this.copy(selectedHex = hexAtClick, highlightedHexes = hexMap.findAllAdjacentHexesTo(hexAtClick, 1))
        } else {
            this.copy(selectedHex = null, highlightedHexes = setOf())
        }
    }

}