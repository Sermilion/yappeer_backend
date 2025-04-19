package com.yappeer.data.posts.datasource.db.dao

import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import org.jetbrains.exposed.sql.Table

object PostTagTable : Table("post_tag") {
    val tagId = reference("tag_id", TagTable.id)
    val postId = reference("post_id", PostTable.id)

    override val primaryKey = PrimaryKey(postId, tagId)
}
