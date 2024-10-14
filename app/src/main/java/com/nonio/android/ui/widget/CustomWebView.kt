import android.content.Context
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class CustomWebView : WebView {
    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs,
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr,
    ) {
        init()
    }

    private fun init() {
        this.setBackgroundColor(Color.Transparent.toArgb())

        setOnLongClickListener {
            true
        }
        isLongClickable = false
        isHapticFeedbackEnabled = false
    }

    override fun startActionMode(
        callback: ActionMode.Callback,
        type: Int,
    ): ActionMode? {
        return if (type == ActionMode.TYPE_FLOATING) {
            super.startActionMode(
                object : ActionMode.Callback {
                    override fun onCreateActionMode(
                        mode: ActionMode,
                        menu: Menu,
                    ): Boolean {
                        menu.clear()
                        return true
                    }

                    override fun onPrepareActionMode(
                        mode: ActionMode,
                        menu: Menu,
                    ): Boolean = false

                    override fun onActionItemClicked(
                        mode: ActionMode,
                        item: MenuItem,
                    ): Boolean = false

                    override fun onDestroyActionMode(mode: ActionMode) {}
                },
                type,
            )
        } else {
            super.startActionMode(callback, type)
        }
    }
}
