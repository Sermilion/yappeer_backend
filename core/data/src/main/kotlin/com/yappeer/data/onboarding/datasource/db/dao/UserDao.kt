package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var username by UserTable.avatar
    var email by UserTable.bio
}

object UserTable : UUIDTable("user") {
    val avatar = varchar("avatar", 500)
    val bio = varchar("bio", 500)
}
