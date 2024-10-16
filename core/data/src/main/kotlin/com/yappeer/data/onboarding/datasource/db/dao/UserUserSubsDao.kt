package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object UserUserSubsTable : LongIdTable("user_user_subs") {
    val userId = reference("user_id", UserTable)
    val subId = reference("sub_id", UserTable)
}

class UserUserSubsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserUserSubsDAO>(UserUserSubsTable)

    var userId by UserUserSubsTable.userId
    var subId by UserUserSubsTable.subId
}
