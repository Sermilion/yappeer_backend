package com.yappeer.data.posts.datasource

import com.yappeer.data.communities.db.dao.CommunitiesDAO
import com.yappeer.data.communities.db.dao.CommunitiesTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.mapper.TagDaoMapper.toDomainModel
import com.yappeer.data.posts.PostsMapper.toDomainModel
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsTable
import com.yappeer.data.posts.datasource.db.dao.PostDAO
import com.yappeer.data.posts.datasource.db.dao.PostLikesDislikesTable
import com.yappeer.data.posts.datasource.db.dao.PostMediaTable
import com.yappeer.data.posts.datasource.db.dao.PostTable
import com.yappeer.data.posts.datasource.db.dao.PostTagTable
import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserCommunitySubsTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import com.yappeer.domain.communities.model.Community
import com.yappeer.domain.posts.datasource.PostDataSource
import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.posts.model.PostsResult
import com.yappeer.domain.posts.model.value
import com.yappeer.domain.subscriptions.model.Tag
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

                        val communities = findCommunities(postId)
                        val tags = findTags(postId)
                        val mediaUrls = findMediaUrls(postId)
                        postDAO.toDomainModel(communities = communities, tags = tags, mediaUrls = mediaUrls)
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

    override fun homePosts(page: Int, pageSize: Int): PostsResult? {
        return try {
            transaction {
                val query = PostTable
                    .selectAll()
                    .orderBy(PostTable.likes, SortOrder.DESC)

                val totalPosts = query.count()

                val posts = query
                    .limit(pageSize).offset(start = (page - 1).toLong() * pageSize)
                    .map { resultRow ->
                        val postId = resultRow[PostTable.id].value
                        val postDAO = PostDAO[postId]

                        val communities = findCommunities(postId)
                        val tags = findTags(postId)
                        val mediaUrls = findMediaUrls(postId)

                        postDAO.toDomainModel(communities = communities, tags = tags, mediaUrls = mediaUrls)
                    }
                PostsResult(
                    posts = posts,
                    totalCount = totalPosts,
                    pagesCount = (totalPosts + pageSize - 1) / pageSize,
                    currentPage = page,
                )
            }
        } catch (e: ExposedSQLException) {
            logger.error("Error fetching home posts", e)
            null
        }
    }

    override fun createPost(
        title: String,
        content: String,
        tags: List<String>,
        communityIds: List<UUID>,
        mediaUrls: List<String>,
        createdBy: UUID,
    ): Post? {
        return try {
            transaction {
                // Create the post in a single operation
                val newPost = PostDAO.new {
                    this.title = title
                    this.content = content
                    this.createdBy = EntityID(createdBy, UserTable)
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.likes = 0
                    this.dislikes = 0
                    this.shares = 0
                }

                // Process tags
                val domainTags = processTags(tags, newPost.id)

                // Process communities
                val domainCommunities = processCommunities(communityIds, newPost.id)

                // Process media URLs
                val savedMediaUrls = processMediaUrls(mediaUrls, newPost.id)

                newPost.toDomainModel(
                    communities = domainCommunities,
                    tags = domainTags,
                    mediaUrls = savedMediaUrls,
                )
            }
        } catch (e: ExposedSQLException) {
            logger.error("Error creating post", e)
            null
        }
    }

    // make sql increment: update posts set likes = likes + 1
    @Suppress("CyclomaticComplexMethod")
    override fun updateLikeStats(postId: UUID, userId: UUID, status: LikeStatus): Boolean {
        return transaction {
            try {
                val existingLike = PostLikesDislikesTable
                    .selectAll().where {
                        (PostLikesDislikesTable.postId eq postId) and
                            (PostLikesDislikesTable.userId eq userId)
                    }
                    .firstOrNull()

                if (existingLike != null) {
                    PostLikesDislikesTable.update(
                        { (PostLikesDislikesTable.postId eq postId) and (PostLikesDislikesTable.userId eq userId) },
                    ) {
                        it[PostLikesDislikesTable.likeStatus] = when (existingLike[likeStatus].toLikeStatus()) {
                            LikeStatus.Like -> when (status) {
                                LikeStatus.Neutral -> 0
                                LikeStatus.Like -> 0
                                LikeStatus.Dislike -> -1
                            }

                            LikeStatus.Neutral -> when (status) {
                                LikeStatus.Neutral -> 0
                                LikeStatus.Like -> 1
                                LikeStatus.Dislike -> -1
                            }

                            LikeStatus.Dislike -> when (status) {
                                LikeStatus.Neutral -> 0
                                LikeStatus.Like -> 1
                                LikeStatus.Dislike -> 0
                            }
                        }
                    }
                } else {
                    PostLikesDislikesTable.insert {
                        it[PostLikesDislikesTable.postId] = postId
                        it[PostLikesDislikesTable.userId] = userId
                        it[PostLikesDislikesTable.likeStatus] = status.value
                        it[PostLikesDislikesTable.createdAt] = Clock.System.now().toJavaInstant()
                    }
                }

                updatePostLikeDislikeCount(postId)
                true
            } catch (e: ExposedSQLException) {
                logger.error("Error changing post like status.", e)
                false
            }
        }
    }

    private fun processMediaUrls(mediaUrls: List<String>, postId: EntityID<UUID>): List<String> {
        if (mediaUrls.isEmpty()) {
            return emptyList()
        }

        val timestamp = Clock.System.now().toJavaInstant()

        // Insert all media URLs in a batch
        PostMediaTable.batchInsert(mediaUrls) { mediaUrl ->
            this[PostMediaTable.postId] = postId
            this[PostMediaTable.mediaUrl] = mediaUrl
            this[PostMediaTable.createdAt] = timestamp
        }

        return mediaUrls
    }

    private fun processTags(tags: List<String>, postId: EntityID<UUID>): List<Tag> {
        // Early return if no tags to process
        if (tags.isEmpty()) {
            return emptyList()
        }

        // Fetch or create all tags in a single batch operation where possible
        val existingTags = TagDAO.find { TagTable.name inList tags }.toList()
        val existingTagNames = existingTags.map { it.name }

        // Create only the new tags that don't exist yet
        val newTagEntities = (tags - existingTagNames.toSet()).map { newTagName ->
            TagDAO.new { name = newTagName }
        }

        // Combine existing and new tags
        val allTagEntities = existingTags + newTagEntities

        // Create tag-post associations in a single batch
        if (allTagEntities.isNotEmpty()) {
            PostTagTable.batchInsert(allTagEntities) { tag ->
                this[PostTagTable.postId] = postId
                this[PostTagTable.tagId] = tag.id
            }
        }

        // Get all tag IDs for follower count query
        val tagIds = allTagEntities.map { it.id.value }

        // Use join to get follower counts for all tags at once
        val tagFollowerCounts = UserTagSubsTable
            .select(UserTagSubsTable.tagId, UserTagSubsTable.userId.count())
            .where { UserTagSubsTable.tagId inList tagIds }
            .groupBy(UserTagSubsTable.tagId)
            .associate {
                it[UserTagSubsTable.tagId].value to it[UserTagSubsTable.userId.count()]
            }

        // Map tags to domain models with proper follower counts
        return allTagEntities.map { tagEntity ->
            // Get the follower count from the map, default to 0 if not found
            val followersCount = tagFollowerCounts[tagEntity.id.value] ?: 0
            tagEntity.toDomainModel(followersCount)
        }
    }

    private fun processCommunities(communityIds: List<UUID>, postId: EntityID<UUID>): List<Community> {
        // Early return if no communities to process
        if (communityIds.isEmpty()) {
            return emptyList()
        }

        // Fetch communities and validate they exist
        val communities = communityIds.mapNotNull { communityId ->
            try {
                // Check if community exists
                val communityEntity = CommunitiesDAO.findById(communityId)

                // If community exists, create the post-community association
                if (communityEntity != null) {
                    CommunityPostsTable.insert {
                        it[CommunityPostsTable.postId] = postId
                        it[CommunityPostsTable.communityId] = communityEntity.id
                    }

                    // Get follower count for this community
                    val followerCount = UserCommunitySubsTable
                        .select(UserCommunitySubsTable.userId.count())
                        .where { UserCommunitySubsTable.communityId eq communityId }
                        .first()[UserCommunitySubsTable.userId.count()]

                    // Return domain model
                    communityEntity.toDomainModel(followerCount)
                } else {
                    logger.warn("Community with ID $communityId does not exist, skipping")
                    null
                }
            } catch (e: ExposedSQLException) {
                logger.error("Error processing community with ID $communityId", e)
                null
            }
        }

        return communities
    }

    private fun findMediaUrls(postId: UUID): List<String> = PostMediaTable
        .select(PostMediaTable.mediaUrl)
        .where { PostMediaTable.postId eq postId }
        .map { it[PostMediaTable.mediaUrl] }

    private fun Int.toLikeStatus(): LikeStatus {
        return when (this) {
            1 -> LikeStatus.Like
            -1 -> LikeStatus.Dislike
            else -> LikeStatus.Neutral
        }
    }

    private fun updatePostLikeDislikeCount(
        postId: UUID,
    ) {
        transaction {
            PostTable.update({ PostTable.id eq postId }) {
                it[likes] = PostLikesDislikesTable.selectAll().where {
                    (PostLikesDislikesTable.postId eq postId) and
                        (PostLikesDislikesTable.likeStatus eq LikeStatus.Like.value)
                }.count().toInt()
                it[dislikes] = PostLikesDislikesTable.selectAll().where {
                    (PostLikesDislikesTable.postId eq postId) and
                        (PostLikesDislikesTable.likeStatus eq LikeStatus.Dislike.value)
                }.count().toInt()
            }
        }
    }

    private fun findCommunities(postId: UUID) = CommunityPostsTable
        .innerJoin(CommunitiesTable, { communityId }, { id })
        .leftJoin(UserCommunitySubsTable, { CommunitiesTable.id }, { communityId })
        .select(
            CommunitiesTable.columns + UserCommunitySubsTable.userId.count(),
        ).where { CommunityPostsTable.postId eq postId }
        .groupBy(
            CommunitiesTable.id,
            CommunitiesTable.name,
            CommunitiesTable.description,
            CommunitiesTable.creatorId,
            CommunitiesTable.createdAt,
            CommunitiesTable.updatedAt,
            CommunitiesTable.isPrivate,
            CommunitiesTable.iconUrl,
        )
        .map { communityRow ->
            val communityId = communityRow[CommunitiesTable.id]
            val followerCount = communityRow[UserCommunitySubsTable.userId.count()]
            CommunitiesDAO[communityId].toDomainModel(followerCount)
        }

    private fun findTags(postId: UUID) = (PostTagTable innerJoin TagTable)
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
}
