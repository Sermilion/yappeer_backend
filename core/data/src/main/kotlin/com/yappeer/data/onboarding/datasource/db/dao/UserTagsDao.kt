package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID

class UserTagSubsDAO(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<UserTagSubsDAO>(UserTagSubsTable)

    var userId by UserTagSubsTable.userId
    var tagId by UserTagSubsTable.tagId
}

object UserTagSubsTable : CompositeIdTable("user_tag_subs") {
    val userId = reference("user_id", UserTable.id)
    val tagId = reference("tag_id", TagTable.id)

    override val primaryKey = PrimaryKey(userId, tagId)
}
