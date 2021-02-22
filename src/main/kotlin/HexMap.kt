import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import java.awt.*
import java.awt.image.BufferedImage


data class Hex(val row: Int, val col: Int) {

    var poly: Polygon? = null

    fun containsPoint(x: Int, y: Int): Boolean {
        return poly?.contains(x, y) ?: false
    }
}


@FlowPreview
@ExperimentalCoroutinesApi
data class HexMap(
    private val width: Int,
    private val height: Int,
    private val rows: Int,
    private val columns: Int,
    private var hexSize: Int = 50
) {

    val cachedImage = MutableStateFlow<BufferedImage>(BufferedImage(width,height, BufferedImage.TYPE_INT_RGB))

    private val hexArray: Array<Array<Hex>> = Array(rows) { rowNum ->
        Array(columns) { colNum ->
            Hex(rowNum, colNum)
        }
    }
    private val entityToHexMap = mutableMapOf<Entity, Hex>()
    private val looseEntities = mutableListOf<Entity>()

    init {
        setHexPolygons()
        renderHexMap()
    }

    fun getHexForEntity(entity: Entity): Hex? {
        return entityToHexMap[entity]
    }

    fun assignEntityToHex(entity: Entity, row: Int, col: Int) {
        assignEntityToHex(entity, hexArray[row][col])
    }

    fun assignEntityToHex(entity: Entity, hex: Hex) {
        // Using the upper left corner as the start point, work out how to center the image in the hex
//        entity.x = hex.poly!!.xpoints[0] + hexSize/2 - entity.image.width/2
//        val heightOfHexHalf = (0.8660 * hexSize).toInt()
//        entity.y = hex.poly!!.ypoints[0] + heightOfHexHalf - entity.image.width/2
        entityToHexMap[entity] = hex
        renderHexMap()
    }

    fun addEntity(entity: Entity) {
        looseEntities.add(entity)
    }

//    fun findEntityAt(click: MouseClick): Entity? {
//        return entityToHexMap.keys.firstOrNull { it.containsPoint(click.x, click.y) && it.isClickable}
//    }

    private fun setHexPolygons() {

        var beginDrawingFromX = (0.5 * hexSize).toInt()
        var beginDrawingFromY = (0.5 * hexSize).toInt()

        var x = beginDrawingFromX
        var y = beginDrawingFromY

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (j % 2 != 0)
                    y = beginDrawingFromY + (.8660 * hexSize).toInt()
                else
                    y = beginDrawingFromY

                val poly = Polygon()
                poly.addPoint(x + hexSize / 2, y)
                poly.addPoint(x + hexSize / 2 + hexSize, y)
                poly.addPoint(x + 2 * hexSize, (.8660 * hexSize + y).toInt())
                poly.addPoint(x + hexSize / 2 + hexSize, (.8660 * 2.0 * hexSize.toDouble() + y).toInt())
                poly.addPoint(x + hexSize / 2, (.8660 * 2.0 * hexSize.toDouble() + y).toInt())
                poly.addPoint(x, y + (.8660 * hexSize).toInt())

                val hex = getHexAtRowCol(i, j)!!
                if (hex.poly == null) {
                    hex.poly = poly
                }

                //Move the pencil over
                x += (hexSize / 2) + hexSize
            }

            beginDrawingFromY += (2 * (.8660 * hexSize)).toInt()

            x = beginDrawingFromX
            y += (2.0 * .8660 * hexSize.toDouble()).toInt()

            y = if (i % 2 != 0)
                beginDrawingFromY + (.8660 * hexSize).toInt()
            else
                beginDrawingFromY

        }
    }

    fun setHexSize(size: Int) {
        hexSize = size
    }

    fun getHexAtClick(click: MouseClick): Hex? {
        return hexArray.flatten().firstOrNull { it.containsPoint(click.x, click.y) }
    }

    fun getEntityAtClick(click: MouseClick): Entity? {
        val hex = getHexAtClick(click) ?: return null
        return entityToHexMap.entries.firstOrNull { it.value == hex }?.key
    }

    fun getHexAtRowCol(row: Int, column: Int): Hex? {
        return hexArray[row][column]
    }

    fun findAdjacentHexesTo(center: Hex): Set<Hex> {
        val adjacentHexes = mutableSetOf<Hex>()

        hexArray.flatten().forEach { hex ->
            if ((hex.col == center.col) && (hex.row == center.row - 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.col == center.col) && (hex.row == center.row + 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.row == center.row) && (hex.col == center.col - 1)) {
                adjacentHexes.add(hex)
            }

            if ((hex.row == center.row) && (hex.col == center.col + 1)) {
                adjacentHexes.add(hex)
            }

            if (center.col % 2 != 0) {
                if ((center.col - 1 == hex.col) && (center.row + 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }

                if ((center.col + 1 == hex.col) && (center.row + 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }
            } else {
                if ((center.col - 1 == hex.col) && (center.row - 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }

                if ((center.col + 1 == hex.col) && (center.row - 1 == hex.row)) {
                    adjacentHexes.add(hex)
                }
            }
        }

        return adjacentHexes
    }

    fun findAllAdjacentHexesTo(center: Hex, depth: Int, adjacentSet: MutableSet<Hex>): Set<Hex> {

        if (depth == 0) {
            return adjacentSet
        }

        val adjacents = findAdjacentHexesTo(center)
        adjacentSet.addAll(adjacents)

        adjacents
            .forEach {
                adjacentSet.addAll(findAllAdjacentHexesTo(it, depth - 1, adjacentSet))
            }

        return adjacentSet
    }


    private fun renderHexMap() {

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics as Graphics2D
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)


        getEntities()
//            .map { it.getRenderItem() }
//            .filter { it.layerOrder < 0 }
//            .sortedBy { it.layerOrder }
            .filterIsInstance<Sprite>()
            .forEach { sprite ->

                val hex = getHexForEntity(sprite)!!
                val centeredCoordinates = findCenteredCoordinates(hex, sprite)

                g.drawImage(sprite.image, centeredCoordinates.first, centeredCoordinates.second, null)
            }

        g.color = Color.BLACK
        g.stroke = BasicStroke(5f)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        hexArray.flatten().forEach {
            g.drawPolygon(it.poly)
        }

        g.dispose()
        cachedImage.value = image
    }

    private fun findCenteredCoordinates(hex: Hex, entity: Sprite): Pair<Int, Int> {
        val centerX = hex.poly!!.xpoints[0] + hexSize / 2 - entity.image.width / 2
        val heightOfHexHalf = (0.8660 * hexSize).toInt()
        val centerY = hex.poly!!.ypoints[0] + heightOfHexHalf - entity.image.width / 2

        return Pair(centerX, centerY)
    }

    fun getEntities(): List<Entity> {
        return entityToHexMap.keys.toList().plus(looseEntities)
    }

}

class BiMap<K,V> {

    private val kToV = mutableMapOf<K,V>()
    private val vToK = mutableMapOf<V,K>()



}