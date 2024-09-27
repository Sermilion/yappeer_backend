package com.sermilion.domain.onboarding.model.registration

import com.sermilion.domain.onboarding.model.Validateable

@JvmInline
value class Username(val value: String) : Validateable {
    override fun validate(): Boolean {
        return value.isNotEmpty() && value.length > 2
    }
}
