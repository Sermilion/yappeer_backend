package com.yappeer.data.content.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

class UserUserSubsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserUserSubsDAO>(UserUserSubsTable)

    var userId by UserUserSubsTable.userId
    var subId by UserUserSubsTable.subId
}

object UserUserSubsTable : IdTable<Long>("user_user_subs") {
    override val id = long("id").autoIncrement().entityId()
    val userId = reference("user_id", UserTable.id)
    val subId = reference("sub_id", UserTable.id)
    override val primaryKey = PrimaryKey(userId, subId)
}
