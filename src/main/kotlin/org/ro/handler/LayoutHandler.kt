package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.layout.Layout

@ImplicitReflectionSerializer
class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val layout = parse(jsonStr)
            return layout.row.size > 0
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val layout = parse(jsonStr)
        logEntry.obj = layout
    }

    fun parse(jsonStr: String): Layout {
//        val s2 = Utils().quoteNulls(jsonStr)
        return JSON./*nonstrict.*/parse(Layout.serializer(), jsonStr)
    }

}
