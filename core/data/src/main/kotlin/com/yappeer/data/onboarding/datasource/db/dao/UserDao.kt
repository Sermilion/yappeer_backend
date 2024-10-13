package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var username by UserTable.username
    var bio by UserTable.bio
    val avatar by UserTable.avatar
    var email by UserTable.email
    var passwordHash by UserTable.password_hash
    var createdAt by UserTable.createdAt
    var lastLogin by UserTable.lastLogin
    var background by UserTable.background
}

object UserTable : UUIDTable("user") {
    val avatar = varchar("avatar", 500).nullable()
    val bio = varchar("bio", 500).nullable()
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 50).uniqueIndex()
    val password_hash = text("password_hash")
    val createdAt = timestamp("created_at")
    val lastLogin = timestamp("last_login").nullable()
    val background = text("background").nullable()
}
