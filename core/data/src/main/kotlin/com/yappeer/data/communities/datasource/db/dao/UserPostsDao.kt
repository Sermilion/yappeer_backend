package com.yappeer.data.communities.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable.autoIncrement
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable.entityId
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

class UserPostsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserPostsDAO>(UserPostsTable)

    var userId by UserPostsTable.userId
    var postId by UserPostsTable.postId
}

object UserPostsTable : IdTable<Long>("user_posts") {
    override val id = long("id").autoIncrement().entityId()
    val userId = reference("user_id", UserTable.id)
    val postId = reference("post_id", PostTable.id)
}
