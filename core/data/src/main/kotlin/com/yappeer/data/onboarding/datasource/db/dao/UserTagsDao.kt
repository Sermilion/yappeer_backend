package com.yappeer.data.onboarding.datasource.db.dao

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID

class UserTagDAO(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<UserTagDAO>(UserTagTable)

    var userId by UserTagTable.userId
    var tagId by UserTagTable.tagId
}

object UserTagTable : CompositeIdTable("user_tag") {
    val userId = reference("user_id", UserTable.id)
    val tagId = reference("tag_id", TagTable.id)

    override val primaryKey = PrimaryKey(userId, tagId)
}
