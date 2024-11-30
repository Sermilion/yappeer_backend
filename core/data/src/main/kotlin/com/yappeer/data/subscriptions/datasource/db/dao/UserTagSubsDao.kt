package com.yappeer.data.subscriptions.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

class UserTagSubsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserTagSubsDAO>(UserTagSubsTable)

    var userId by UserTagSubsTable.userId
    var tagId by UserTagSubsTable.tagId
}

object UserTagSubsTable : IdTable<Long>("user_tag_subs") {
    override val id = long("id").autoIncrement().entityId()
    val userId = reference("user_id", UserTable.id)
    val tagId = reference("tag_id", TagTable.id)

    override val primaryKey = PrimaryKey(userId, tagId)
}
