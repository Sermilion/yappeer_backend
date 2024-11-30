package com.yappeer.data.posts.datasource

import com.yappeer.data.communities.db.dao.CommunitiesDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.mapper.TagDaoMapper.toDomainModel
import com.yappeer.data.posts.PostsMapper.toDomainModel
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsDAO
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsTable
import com.yappeer.data.posts.datasource.db.dao.PostDAO
import com.yappeer.data.posts.datasource.db.dao.PostTable
import com.yappeer.data.posts.datasource.db.dao.PostTagTable
import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import com.yappeer.domain.posts.datasource.PostDataSource
import com.yappeer.domain.posts.model.PostsResult
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.UUID

class YappeerPostDataSource : PostDataSource {
    private val logger = LoggerFactory.getLogger(YappeerPostDataSource::class.java.name)

    override fun userPosts(userId: UUID, page: Int, pageSize: Int): PostsResult? {
        return try {
            transaction {
                val query = PostTable.selectAll().where { PostTable.createdBy eq userId }

                val totalPosts = query.count()

                val posts = query
                    .limit(pageSize).offset(start = (page - 1).toLong() * pageSize)
                    .map { resultRow ->
                        val postId = resultRow[PostTable.id].value
                        val postDAO = PostDAO[postId]

                        val communities = CommunityPostsDAO.find { CommunityPostsTable.postId eq postId }.map {
                            CommunitiesDAO[it.communityId].toDomainModel()
                        }

                        val tags = (PostTagTable innerJoin TagTable)
                            .leftJoin(UserTagSubsTable, { TagTable.id }, { tagId })
                            .select(TagTable.columns + UserTagSubsTable.userId.count())
                            .where { PostTagTable.postId eq postId }
                            .groupBy(TagTable.id)
                            .map { tagRow ->
                                val tagId = tagRow[TagTable.id].value
                                val tagDAO = TagDAO[tagId]
                                val followerCount = tagRow[UserTagSubsTable.userId.count()]
                                tagDAO.toDomainModel(followerCount)
                            }
                        postDAO.toDomainModel(communities = communities, tags = tags)
                    }

                PostsResult(
                    posts = posts,
                    totalCount = totalPosts,
                    pagesCount = (totalPosts + pageSize - 1) / pageSize,
                    currentPage = page,
                )
            }
        } catch (e: ExposedSQLException) {
            logger.error("Error fetching user posts", e)
            null
        }
    }

    override fun createPost(
        title: String,
        content: String,
        tags: List<String>,
        createdBy: UUID,
    ): Boolean {
        return try {
            transaction {
                val newPost = PostDAO.new {
                    this.title = title
                    this.content = content
                    this.createdBy = EntityID(createdBy, UserTable)
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.likes = 0
                    this.dislikes = 0
                    this.shares = 0
                }

                tags.forEach { tagName ->
                    val tag = TagDAO.find { TagTable.name eq tagName }.firstOrNull() ?: TagDAO.new {
                        name = tagName
                    }
                    PostTagTable.insert {
                        it[postId] = newPost.id
                        it[tagId] = tag.id
                    }
                }
            }
            true
        } catch (e: ExposedSQLException) {
            logger.error("Error creating post", e)
            false
        }
    }
}
