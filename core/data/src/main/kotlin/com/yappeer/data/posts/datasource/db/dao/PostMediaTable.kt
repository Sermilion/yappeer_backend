package com.yappeer.data.posts.datasource.db.dao

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

object PostMediaTable : UUIDTable("post_media") {
    val postId = reference("post_id", PostTable)
    val mediaUrl = varchar("media_url", 1024)
    val createdAt = timestamp("created_at")
}
