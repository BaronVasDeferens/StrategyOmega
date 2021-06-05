import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.random.Random


data class Hex(val row: Int, val col: Int) {

    lateinit var poly: Polygon
    var hexSize = 50
    var color: Color? = null

    fun setPolyAndSize(poly: Polygon, hexSize: Int) {
        this.poly = poly
        this.hexSize = hexSize
    }

    fun containsPoint(x: Int, y: Int): Boolean {
        return poly.contains(x, y) ?: false
    }
}


@FlowPreview
@ExperimentalCoroutinesApi
data class HexMap(
    private val width: Int,
    private val height: Int,
    private val rows: Int,
    private val columns: Int,
    var hexSize: Int = 50
) {

    private var stroke = BasicStroke(1.0f)

    // Cached image: the last render of the map. Only updates when a change to the map occurs
    val cachedImage = MutableStateFlow(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB))

    private val hexArray: Array<Array<Hex>> = Array(rows) { rowNum ->
        Array(columns) { colNum ->
            Hex(rowNum, colNum)
        }
    }
    private val entityToHexMap = mutableMapOf<Entity, Hex>()
    private val looseEntities = mutableListOf<Entity>()

    init {
        setHexPolygons()
    }

    fun generateRegions(regions: Int = 1) {

        // Reset
        hexArray.flatten().forEach { it.color = null }

        // Find the "seed" (first) of a region
        val seeds = hexArray.flatten().shuffled().take(regions)

        // generate a color for each seed
        val seedColors = seeds.associateWith { hex ->
            Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
        }

        // The frontier is the list of potential additions to the map
        val frontier = mutableSetOf<Hex>()

        seeds.forEach { seed ->
            seed.color = seedColors[seed]
            frontier.add(seed)
        }

        var index = 0
        while (frontier.isNotEmpty()) {
            // cycle through the colors; helps improve "fairness"
            val candidate = frontier.filter { it.color == seeds[index].color }.shuffled().firstOrNull()
            index = (index + 1) % seeds.size
            if (candidate == null) {
                continue
            }

            // Are we dealing with a seed?
            // The candidate is a seed.
            if (seeds.contains(candidate)) {
                findAdjacentHexesTo(candidate).filter { it.color == null }
                    .shuffled()
                    .firstOrNull()
                    ?.apply {
                        this.color = candidate.color
                        frontier.remove(this)

                        findAdjacentHexesTo(this)
                            .filter { it.color == null }
                            .forEach {
                                it.color = candidate.color
                                frontier.add(it)
                            }
                    }

            } else {
                val newCandidate = findAdjacentHexesTo(candidate).filter { it.color == null }
                    .shuffled()
                    .firstOrNull()

                if (newCandidate != null) {
                    newCandidate.color = candidate.color
                    frontier.remove(newCandidate)
                    findAdjacentHexesTo(newCandidate).filter { it.color == null }.forEach {
                        it.color = candidate.color
                        frontier.add(it)
                    }
                }
            }

            frontier.remove(candidate)
        }
    }

    @Synchronized
    fun generateTunnel(tunnelHexSize: Int = 65): Set<Hex> {
        val tunnelHexes = mutableSetOf<Hex>()

        val startHex = hexArray.flatten().random()
        tunnelHexes.add(startHex)

        var failures = 0

        while (tunnelHexes.size < tunnelHexSize) {

            val candidate = tunnelHexes.shuffled().first()

            // Rank each hex: if it has few neighbors in the tunnel, it has a lower score (more desireable)
            val newHex = findAdjacentHexesTo(candidate).sortedBy {
                findAdjacentHexesTo(it).sumBy {
                    // Rank each hex: if it has few neighbors in the tunnel, it has a lower score (more desireable)
                    when (tunnelHexes.contains(it)) {
                        true -> 1
                        else -> 0
                    }
                }

            }.firstOrNull()

            if (newHex != null) {
                tunnelHexes.add(newHex)
            } else {
                failures++
            }

            if (failures >= 1000) {
                println(">>> TOO MANY FAILURES!")
                failures = 0;
                tunnelHexes.clear()
            }
        }

        return tunnelHexes.toSet()
    }

    fun getHexForEntity(entity: Entity?): Hex? {
        return entityToHexMap[entity]
    }

    fun getEntityForHex(hex: Hex?): Entity? {
        return if (hex == null) {
            null
        } else {
            entityToHexMap.entries.firstOrNull { it.value == hex }?.key
        }
    }

    fun assignEntityToHex(entity: Entity, row: Int, col: Int) {
        assignEntityToHex(entity, hexArray[row][col])
    }

    fun assignEntityToHex(entity: Entity, hex: Hex) {
        entityToHexMap[entity] = hex
        renderHexMap()
    }

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

                getHexAtRowCol(i, j)!!.setPolyAndSize(poly, hexSize)


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

    fun getHexAtClick(actionEvent: MouseActionEvent): Hex? {
        return hexArray.flatten().firstOrNull { it.containsPoint(actionEvent.x, actionEvent.y) }
    }

    fun getEntityAtClick(actionEvent: MouseActionEvent): Entity? {
        val hex = getHexAtClick(actionEvent) ?: return null
        return entityToHexMap.entries.firstOrNull { it.value == hex }?.key
    }

    private fun getHexAtRowCol(row: Int, column: Int): Hex? {
        return hexArray[row][column]
    }

    private fun findAdjacentHexesTo(center: Hex): Set<Hex> {
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

    fun findAllAdjacentHexesTo(center: Hex?, depth: Int, adjacentSet: MutableSet<Hex> = mutableSetOf()): Set<Hex> {

        if (center == null) return setOf()

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

    fun renderHexMap() {

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics as Graphics2D
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)


        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.stroke = stroke
        hexArray.flatten().forEach { hex ->

            hex.color?.apply {
                g.color = this
                g.fillPolygon(hex.poly)
            }
            g.color = Color.BLACK
            g.drawPolygon(hex.poly)
        }


        getEntities()
            .filterIsInstance<Sprite>()
            .forEach { sprite ->
                val hex = getHexForEntity(sprite)!!
                val centeredCoordinates = findCenteredCoordinatesForSprite(hex, sprite)
                g.drawImage(sprite.image, centeredCoordinates.first, centeredCoordinates.second, null)
            }

        g.dispose()
        cachedImage.value = image
    }

    fun renderGameState(gameState: GameState) {

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics as Graphics2D
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)


        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.stroke = stroke
        hexArray.flatten().forEach { hex ->
            if (gameState.selectedHex == hex) {
                g.color = Color.RED
                g.fillPolygon(hex.poly)
            } else if (gameState.highlightedHexes.contains(hex)) {
                g.color = Color.PINK
                g.fillPolygon(hex.poly)
            }

            hex.color?.apply {
                g.color = this
                g.fillPolygon(hex.poly)
            }

            g.color = Color.BLACK
            g.drawPolygon(hex.poly)
        }

        gameState.selectedHexes.forEach { hex ->
            g.color = Color.BLACK
            g.fillPolygon(hex.poly)
        }

        getEntities()
            .filterIsInstance<Sprite>()
            .filterNot { sprite -> gameState.animations.map { it.sprite }.any { it == sprite } }
            .forEach { sprite ->
                val hex = getHexForEntity(sprite)!!
                val centeredCoordinates = findCenteredCoordinatesForSprite(hex, sprite)
                g.drawImage(sprite.image, centeredCoordinates.first, centeredCoordinates.second, null)
            }

        gameState.animations.forEach { anim ->
            g.drawImage(anim.drawImage(), anim.x, anim.y, null)
        }

        // draw any transient lines
        for (list in gameState.transientLines.windowed(2)) {
            g.color = Color.GREEN
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f)
            g.drawLine(list[0].first, list[0].second, list[1].first, list[1].second)
        }

        g.dispose()
        cachedImage.value = image
    }


    private fun getEntities(): List<Entity> {
        return entityToHexMap.keys.toList().plus(looseEntities)
    }

}
