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

data class MouseActionEvent(val type: MouseClickType, val point: Point, val timeStamp: Long = System.currentTimeMillis()) {
    val x = point.x
    val y = point.y

    override fun equals(other: Any?): Boolean {
        return if (other !is MouseActionEvent) { false } else {
            type == other.type && x == other.x && y == other.y && timeStamp == other.timeStamp
        }
    }
}

val mouseActionFlow = MutableStateFlow(MouseActionEvent(NADA, Point(0, 0)))


val mouseActionAdapter = object : MouseInputAdapter() {

    override fun mousePressed(e: MouseEvent?) {
        super.mousePressed(e)

        e?.apply {
            if (button == MouseEvent.BUTTON1) {
                mouseActionFlow.value = MouseActionEvent(MOUSE_CLICK_PRIMARY_DOWN, point)
            }
        }
    }

    override fun mouseReleased(e: MouseEvent?) {
        super.mouseReleased(e)
        mouseActionFlow.value = MouseActionEvent(MOUSE_CLICK_PRIMARY_UP, e!!.point)
    }

    override fun mouseMoved(e: MouseEvent?) {
        super.mouseMoved(e)
        mouseActionFlow.value = MouseActionEvent(MOUSE_MOVE, e!!.point)
    }

    override fun mouseDragged(e: MouseEvent?) {
        super.mouseDragged(e)
        mouseActionFlow.value = MouseActionEvent(MOUSE_CLICK_PRIMARY_DRAG, e!!.point)
    }
}
