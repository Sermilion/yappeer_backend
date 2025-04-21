package com.yappeer.domain.onboarding.model.value

/**
 * Masks an email address for logging purposes.
 * Converts "user@example.com" to "u***@e*******.com"
 */
@Suppress("ReturnCount")
fun Email.maskForLogging(): String {
    val value = this
    val parts = value.value.split("@")
    if (parts.size != 2) return "****@****.***"

    val username = parts[0]
    val domain = parts[1]

    val maskedUsername = if (username.length > 1) {
        username.first() + "*".repeat(username.length - 1)
    } else {
        "*"
    }

    val domainParts = domain.split(".")
    if (domainParts.size < 2) return "$maskedUsername@****"

    val tld = domainParts.last()
    val domainName = domainParts.dropLast(1).joinToString(".")

    val maskedDomain = if (domainName.length > 1) {
        domainName.first() + "*".repeat(domainName.length - 1)
    } else {
        "*"
    }

    return "$maskedUsername@$maskedDomain.$tld"
}
