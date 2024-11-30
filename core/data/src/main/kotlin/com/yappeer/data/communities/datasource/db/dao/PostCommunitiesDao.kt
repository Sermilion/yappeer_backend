package com.yappeer.data.communities.datasource.db.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class CommunityPostsDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CommunityPostsDAO>(CommunityPostsTable)

    var communityId by CommunityPostsTable.communityId
    var postId by CommunityPostsTable.postId
}

object CommunityPostsTable : UUIDTable("community_posts") {
    val communityId = reference("community_id", CommunitiesTable.id)
    val postId = reference("post_id", PostTable.id)
}
