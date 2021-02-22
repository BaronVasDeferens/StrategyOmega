
enum class GamePhase {
    PLAYER_MOVE,
    BLOCKING_ANIMATION,
    AI_MOVE
}

data class GameState(val phase: GamePhase) {
}