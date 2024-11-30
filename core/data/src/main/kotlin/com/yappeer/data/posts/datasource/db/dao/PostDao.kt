package com.yappeer.data.posts.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class PostDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PostDAO>(PostTable)

    var title by PostTable.title
    var content by PostTable.content
    var createdBy by PostTable.createdBy
    var createdAt by PostTable.createdAt
    var updatedAt by PostTable.updatedAt
    var likes by PostTable.likes
    var dislikes by PostTable.dislikes
    var shares by PostTable.shares
}

object PostTable : UUIDTable("post") {
    val title = PostTable.text("title").nullable()
    val content = PostTable.text("content")
    val createdBy = reference("created_by", UserTable.id)
    val createdAt = PostTable.timestamp("created_at")
    val updatedAt = PostTable.timestamp("updated_at").nullable()
    val likes = PostTable.integer("likes")
    val dislikes = PostTable.integer("dislikes")
    val shares = PostTable.integer("shares")
}
