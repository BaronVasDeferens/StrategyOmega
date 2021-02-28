/**
 * Given a hex and an image, returns the points (x,y) where an image, when drawn at x,y, will be centered within the hex
 * TODO: check whether image "fits" inside
 */
fun findCenteredCoordinatesForSprite(hex: Hex, entity: Sprite): Pair<Int, Int> {
    val centerX = hex.poly.xpoints[0] + hex.hexSize / 2 - entity.image.width / 2
    val heightOfHexHalf = (0.8660 * hex.hexSize).toInt()
    val centerY = hex.poly.ypoints[0] + heightOfHexHalf - entity.image.width / 2

    return Pair(centerX, centerY)
}