package com.jarvis.ai.brain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jarvis.ai.control.JarvisAccessibilityService
import java.net.URLEncoder

/** Free, offline command handling. Returns null when the command is not recognised. */
class CommandProcessor(private val ctx: Context) {

    fun run(input: String): String? {
        val t = input.trim()
        val l = t.lowercase()
        return when {
            l.startsWith("open ") -> openApp(t.substring(5))
            l.contains("go back") || l == "back" -> withService { it.back(); "Going back." }
            l.contains("home") && l.length < 20 -> withService { it.home(); "Going home." }
            l.contains("scroll down") -> withService { if (it.scroll(true)) "Scrolled down." else "Nothing to scroll here." }
            l.contains("scroll up") -> withService { if (it.scroll(false)) "Scrolled up." else "Nothing to scroll here." }
            l.contains("read screen") -> withService { it.readScreen().ifBlank { "I can't see any text." } }
            l.startsWith("tap ") || l.startsWith("click ") -> withService {
                if (it.clickByText(t.substringAfter(' '))) "Done." else "I couldn't find that on screen."
            }
            l.startsWith("type ") -> withService { if (it.typeText(t.substring(5))) "Typed it." else "No text field is focused." }
            l.startsWith("play ") -> youtube(t.substring(5), play = true)
            l.startsWith("youtube ") -> youtube(t.substring(8), play = false)
            else -> null
        }
    }

    private fun withService(block: (JarvisAccessibilityService) -> String): String {
        val svc = JarvisAccessibilityService.instance
            ?: return "Please turn on JARVIS AI in Accessibility settings first."
        return block(svc)
    }

    private fun openApp(name: String): String {
        val pm = ctx.packageManager
        val wanted = name.trim()
        val apps = pm.getInstalledApplications(0)
        val app = apps.firstOrNull { pm.getApplicationLabel(it).toString().equals(wanted, true) }
            ?: apps.firstOrNull { pm.getApplicationLabel(it).toString().contains(wanted, true) }
            ?: return "I couldn't find an app called $wanted."
        val intent = pm.getLaunchIntentForPackage(app.packageName)
            ?: return "$wanted can't be opened."
        ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return "Opening $wanted."
    }

    private fun youtube(query: String, play: Boolean): String {
        val q = query.trim()
        val url = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(q, "UTF-8")
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return if (play) "Searching YouTube for $q. Say tap and the video title to play it."
        else "Opening YouTube search for $q."
    }
}
