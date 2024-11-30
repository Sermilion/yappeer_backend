package com.yappeer.data.communities.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class CommunitiesDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CommunitiesDAO>(CommunitiesTable)

    var name by CommunitiesTable.name
    var description by CommunitiesTable.description
    var creatorId by CommunitiesTable.creatorId
    var createdAt by CommunitiesTable.createdAt
    var updatedAt by CommunitiesTable.updatedAt
    var isPrivate by CommunitiesTable.isPrivate
}

object CommunitiesTable : UUIDTable("community") {
    val name = CommunitiesTable.text("name")
    val description = CommunitiesTable.text("description").nullable()
    val creatorId = reference("creator_id", UserTable.id)
    val createdAt = CommunitiesTable.timestamp("created_at")
    val updatedAt = CommunitiesTable.timestamp("updated_at").nullable()
    val isPrivate = CommunitiesTable.bool("is_private")
}
