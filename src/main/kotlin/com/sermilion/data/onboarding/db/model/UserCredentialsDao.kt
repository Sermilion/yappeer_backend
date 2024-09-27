package com.sermilion.data.onboarding.db.model

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class UserCredentialsDAO(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<UserCredentialsDAO>(UserCredentialsTable)

    var username by UserCredentialsTable.username
    var email by UserCredentialsTable.email
    var passwordHash by UserCredentialsTable.password_hash
    var createdAt by UserCredentialsTable.createdAt
    var lastLogin by UserCredentialsTable.lastLogin
}

object UserCredentialsTable : UUIDTable("user_credentials") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 50).uniqueIndex()
    val password_hash = text("password_hash")
    val createdAt = text("created_at")
    val lastLogin = text("last_login")
}
