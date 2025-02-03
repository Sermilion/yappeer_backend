package com.yappeer.data.posts.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object PostLikesDislikesTable : UUIDTable("post_likes_dislikes") {
  val postId = reference("post_id", PostTable)
  val userId = reference("user_id", UserTable)
  val likeStatus = integer("like_status")
  val createdAt = timestamp("created_at")
}

class PostLikesDislikesDAO(id: EntityID<UUID>) : UUIDEntity(id) {
  companion object : UUIDEntityClass<PostLikesDislikesDAO>(PostLikesDislikesTable)

  var postId by PostDAO referencedOn PostLikesDislikesTable.postId
  var userId by UserDAO referencedOn PostLikesDislikesTable.userId
  var likeStatus by PostLikesDislikesTable.likeStatus
  var createdAt by PostLikesDislikesTable.createdAt
}