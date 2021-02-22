import MouseClickType.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.event.MouseInputAdapter

enum class MouseClickType {
    NADA,
    MOUSE_MOVE,
    MOUSE_CLICK_PRIMARY_DOWN,
    MOUSE_CLICK_PRIMARY_UP,
    MOUSE_CLICK_PRIMARY_DRAG
}

data class MouseClick(val type: MouseClickType, val point: Point, val timeStamp: Long = System.currentTimeMillis()) {
    val x = point.x
    val y = point.y

    override fun equals(other: Any?): Boolean {
        return if (other !is MouseClick) { false } else {
            type == other.type && x == other.x && y == other.y && timeStamp == other.timeStamp
        }
    }
}

val mouseClickChannel = MutableStateFlow(MouseClick(NADA, Point(0, 0)))


val mouseClickAdapter = object : MouseInputAdapter() {

    override fun mousePressed(e: MouseEvent?) {
        super.mousePressed(e)


        mouseClickChannel.value = MouseClick(MOUSE_CLICK_PRIMARY_DOWN, e!!.point)
    }

    override fun mouseReleased(e: MouseEvent?) {
        super.mouseReleased(e)
        mouseClickChannel.value = MouseClick(MOUSE_CLICK_PRIMARY_UP, e!!.point)
    }

    override fun mouseMoved(e: MouseEvent?) {
        super.mouseMoved(e)
        mouseClickChannel.value = MouseClick(MOUSE_MOVE, e!!.point)
    }

    override fun mouseDragged(e: MouseEvent?) {
        super.mouseDragged(e)
        mouseClickChannel.value = MouseClick(MOUSE_CLICK_PRIMARY_DRAG, e!!.point)
    }
}
