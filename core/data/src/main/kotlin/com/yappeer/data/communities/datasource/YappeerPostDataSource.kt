package com.yappeer.data.communities.datasource

import com.yappeer.data.communities.CommunitiesMapper.toDomainModel
import com.yappeer.data.communities.datasource.db.dao.CommunitiesDAO
import com.yappeer.data.communities.datasource.db.dao.CommunityPostsDAO
import com.yappeer.data.communities.datasource.db.dao.CommunityPostsTable
import com.yappeer.data.communities.datasource.db.dao.PostDAO
import com.yappeer.data.communities.datasource.db.dao.PostTable
import com.yappeer.data.communities.datasource.db.dao.PostTagTable
import com.yappeer.data.communities.datasource.db.dao.UserPostsTable
import com.yappeer.data.onboarding.mapper.TagDaoMapper.toDomainModel
import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import com.yappeer.domain.posts.datasource.PostDataSource
import com.yappeer.domain.posts.model.PostsResult
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
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
                val query = PostTable.innerJoin(UserPostsTable, { PostTable.id }, { postId })
                    .selectAll().where { UserPostsTable.userId eq userId }

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
                            .leftJoin(UserTagSubsTable, { TagTable.id }, { UserTagSubsTable.tagId })
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
}
