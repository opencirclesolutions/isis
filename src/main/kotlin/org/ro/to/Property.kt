package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable  // is Property a Variant of Member?
data class Property(val id: String = "",
                    val memberType: String = "",
                    val links: List<Link> = emptyList(),
                    val optional: Boolean? = null,
                    @Optional val title: String? = null,
                    @Optional val value: String? = null,
                    val extensions: Extensions? = null,
                    @Optional val format: String? = null,
                    @Optional val disabledReason: String? = null,
                    @Optional val parameters: List<Parameter> = emptyList(),
                    @Optional val maxLength: Int = 0) {

    fun descriptionLink(): Link? {
        for (l in links) {
            if (l.rel == "describedby")  // introduce enum LinkType?
                return l
        }
        return null
    }
}