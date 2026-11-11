package com.jarvis.ai.control

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Only acts while the user has enabled it in Android settings, and only on
 * screens the user can already see. It does not bypass lock screens or security.
 */
class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile var instance: JarvisAccessibilityService? = null
        @Volatile var stopped = false   // set by the EMERGENCY STOP button
    }

    override fun onServiceConnected() { instance = this }
    override fun onDestroy() { instance = null; super.onDestroy() }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private fun root(): AccessibilityNodeInfo? = rootInActiveWindow

    fun back() { performGlobalAction(GLOBAL_ACTION_BACK) }
    fun home() { performGlobalAction(GLOBAL_ACTION_HOME) }

    fun scroll(forward: Boolean): Boolean {
        val node = findFirst(root()) { it.isScrollable } ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return node.performAction(action)
    }

    fun clickByText(text: String): Boolean {
        val nodes = root()?.findAccessibilityNodeInfosByText(text).orEmpty()
        var node: AccessibilityNodeInfo = nodes.firstOrNull() ?: return false
        while (!node.isClickable) node = node.parent ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun typeText(text: String): Boolean {
        val field = findFirst(root()) { it.className?.toString()?.contains("EditText") == true } ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun readScreen(): String {
        val sb = StringBuilder()
        collect(root(), sb)
        return sb.toString().take(1500)
    }

    private fun collect(n: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (n == null) return
        n.text?.takeIf { it.isNotBlank() }?.let { sb.append(it).append(". ") }
        for (i in 0 until n.childCount) collect(n.getChild(i), sb)
    }

    private fun findFirst(n: AccessibilityNodeInfo?, pred: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (n == null) return null
        if (pred(n)) return n
        for (i in 0 until n.childCount) {
            findFirst(n.getChild(i), pred)?.let { return it }
        }
        return null
    }
}
