package com.yappeer.data.posts.datasource.db.dao

import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class PostTagDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PostTagDAO>(PostTagTable)

    var tagId by PostTagTable.tagId
    var postId by PostTagTable.postId
}

object PostTagTable : UUIDTable("post_tag") {
    val tagId = reference("tag_id", TagTable.id)
    val postId = reference("post_id", PostTable.id)
}
