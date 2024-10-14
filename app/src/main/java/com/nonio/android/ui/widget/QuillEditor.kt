package com.nonio.android.ui.widget

import CustomWebView
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import org.json.JSONArray
import org.json.JSONObject

class QuillEditorController {
    private var content: String = ""

    private var listener: ((String?) -> Unit)? = null

    fun getContent(): String = content

    fun contentIsEmpty(): Boolean {
        runCatching {
            val jsonObject = JSONObject(content)
            if (!jsonObject.has("ops")) return true
            val opsArray: JSONArray = jsonObject.getJSONArray("ops")
            for (i in 0 until opsArray.length()) {
                val opObject: JSONObject = opsArray.getJSONObject(i)
                if (opObject.has("insert")) {
                    val insertContent: String = opObject.getString("insert")
                    if (insertContent.trim().isNotEmpty()) {
                        return false // Found non-empty content
                    }
                }
            }
        }
        return true // No non-empty content found
    }

    // 监听
    fun setListener(listener: (String?) -> Unit) {
        this.listener = listener
    }

    internal fun setContent(content: String?) {
        this.content = content ?: ""
        listener?.invoke(content)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun QuillEditor(
    modifier: Modifier = Modifier,
    controller: QuillEditorController =
        remember {
            QuillEditorController()
        },
) {
    val state = rememberWebViewState("file:///android_asset/quill.html")
    WebView(
        state,
        factory = { context ->
            CustomWebView(context)
        },
        client =
            remember {
                object : AccompanistWebViewClient() {
                    override fun onPageFinished(
                        view: WebView,
                        url: String?,
                    ) {
                        super.onPageFinished(view, url)
                        // 调用焦点获取
                        view.loadUrl("javascript:editorFocus()")
                        view.requestFocus()
                        showSoftKeyboard(view)
                    }
                }
            },
        modifier = modifier,
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.addJavascriptInterface(WebInterface(controller), "Android")
        },
        onDispose = {
            controller.setContent("")
        },
    )
}

internal class WebInterface(
    private val controller: QuillEditorController,
) {
    @JavascriptInterface
    fun getContent(content: String?) {
        controller.setContent(content)
    }
}

fun showSoftKeyboard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val windowHeightMethod =
        InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
    val height = windowHeightMethod.invoke(imm) as Int
    if (height > 0) {
        imm.toggleSoftInput(0, 0)
    }
}
