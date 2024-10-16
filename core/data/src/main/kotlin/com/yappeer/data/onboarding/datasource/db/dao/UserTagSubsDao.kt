package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

class UserTagSubsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserTagSubsDAO>(UserTagSubsTable)

    var userId by UserTagSubsTable.userId
    var tagId by UserTagSubsTable.tagId
}

object UserTagSubsTable : LongIdTable("user_tag_subs") {
    val userId = reference("user_id", UserTable.id)
    val tagId = reference("tag_id", TagTable.id)
}
