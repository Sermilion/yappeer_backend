import com.yappeer.data.communities.db.dao.CommunitiesDAO
import com.yappeer.data.communities.db.dao.CommunitiesTable
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.posts.datasource.YappeerPostDataSource
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsDAO
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
import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.posts.model.PostsResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class YappeerPostDataSourceTest {
    private lateinit var database: Database
    private lateinit var dataSource: YappeerPostDataSource

    @BeforeEach
    fun setUp() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(database) {
            SchemaUtils.create(
                UserTable,
                PostTable,
                CommunitiesTable,
                TagTable,
                UserTagSubsTable,
                PostTagTable,
                CommunityPostsTable,
                UserCommunitySubsTable,
                PostLikesDislikesTable,
                PostMediaTable,
            )
        }
        dataSource = YappeerPostDataSource()
    }

    @AfterEach
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(
                CommunityPostsTable,
                CommunitiesTable,
                PostTable,
                PostTagTable,
                TagTable,
                UserTable,
                UserTagSubsTable,
                UserCommunitySubsTable,
                PostLikesDislikesTable,
                PostMediaTable,
            )
        }
    }

    @Test
    fun `user posts returned with communities and tags`() {
        runBlocking {
            // Given
            val user = createUser("testuser", "test@test.com", "password")

            val community1 = createCommunity("community1", user)
            val community2 = createCommunity("community2", user)

            val tag1 = createTag("tag1")
            val tag2 = createTag("tag2")

            val post1 = createPost(
                title = "title1",
                content = "content1",
                creator = user,
                communities = listOf(community1, community2),
                tags = listOf(tag1, tag2),
            )
            val post2 = createPost("title2", "content2", user, listOf(community1), listOf(tag1))

            // When
            val result =
                dataSource.userPosts(userId = user.id.value, pageSize = 10, page = 1)

            // Then
            result.shouldBeInstanceOf<PostsResult>()
            result.posts.size shouldBe 2
            result.totalCount shouldBe 2
            result.pagesCount shouldBe 1
            result.currentPage shouldBe 1

            val resultPost1 = result.posts.find { it.id == post1.id.value }
            resultPost1?.title shouldBe "title1"
            resultPost1?.communities?.size shouldBe 2
            resultPost1?.tags?.size shouldBe 2

            val resultPost2 = result.posts.find { it.id == post2.id.value }
            resultPost2?.title shouldBe "title2"
            resultPost2?.communities?.size shouldBe 1
            resultPost2?.tags?.size shouldBe 1
        }
    }

    @Test
    fun `createPost creates a new post and associates tags`() {
        val title = "Test Post Title"
        val content = "Test Post Content"
        val tags = listOf("tag1", "tag2", "tag3")

        val user = createUser("testuser", "test@test.com", "password")

        val result = dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = emptyList(),
            mediaUrls = emptyList(),
            createdBy = user.id.value,
        )

        // Verify the post was created and returned
        result.shouldBeInstanceOf<Post>()
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(user.id.value, result.createdBy)
        assertEquals(3, result.tags.size)
        assertEquals(0, result.mediaUrls.size)

        // Verify data in the database
        transaction(database) {
            val createdPost = PostDAO.all().firstOrNull()
            assertEquals(title, createdPost?.title)
            assertEquals(content, createdPost?.content)
            assertEquals(user.id.value, createdPost?.createdBy?.value)

            val associatedTags = PostTagTable.innerJoin(TagTable)
                .selectAll().where { PostTagTable.postId eq createdPost?.id?.value }
                .map { TagDAO[it[PostTagTable.tagId]].name }

            assertEquals(tags.size, associatedTags.size)
            tags.forEach { tag -> assertTrue(associatedTags.contains(tag)) }
        }
    }

    @Test
    fun `createPost handles existing tags correctly`() {
        val title = "Test Post with Existing Tag"
        val content = "Content for the post"
        val existingTagName = "existingTag"

        val user = createUser("testuser", "test@test.com", "password")

        transaction {
            TagDAO.new { name = existingTagName }
        }

        val tags = listOf(existingTagName, "newTag")
        val result = dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = emptyList(),
            mediaUrls = emptyList(),
            createdBy = user.id.value,
        )

        // Verify the post was created and returned
        result.shouldBeInstanceOf<com.yappeer.domain.posts.model.Post>()
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(user.id.value, result.createdBy)
        assertEquals(2, result.tags.size)

        // Verify tags are correct
        val resultTagNames = result.tags.map { it.name }
        assertTrue(resultTagNames.contains(existingTagName))
        assertTrue(resultTagNames.contains("newTag"))

        transaction {
            val numTags = TagDAO.all().count()
            assertEquals(2, numTags)

            val createdPost = PostDAO.all().firstOrNull()
            val associatedTags = PostTagTable.innerJoin(TagTable)
                .selectAll().where { PostTagTable.postId eq createdPost?.id?.value }
                .map { TagDAO[it[PostTagTable.tagId]].name }
            assertEquals(tags.size, associatedTags.size) // Check association
            tags.forEach { tag -> assertTrue(associatedTags.contains(tag)) }
        }
    }

    @Test
    fun `createPost with communities creates correct associations`() {
        val title = "Test Post with Communities"
        val content = "Content for the community post"
        val tags = listOf("tag1")

        val user = createUser("testuser", "test@test.com", "password")
        val community1 = createCommunity("community1", user)
        val community2 = createCommunity("community2", user)

        val communityIds = listOf(community1.id.value, community2.id.value)

        val result = dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = communityIds,
            mediaUrls = emptyList(),
            createdBy = user.id.value,
        )

        // Verify the post was created with correct communities
        result.shouldBeInstanceOf<com.yappeer.domain.posts.model.Post>()
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(user.id.value, result.createdBy)
        assertEquals(2, result.communities.size)

        // Verify community associations in database
        transaction {
            val createdPost = PostDAO.all().firstOrNull()
            val postId = createdPost?.id?.value!!

            val associatedCommunities = CommunityPostsTable
                .selectAll().where { CommunityPostsTable.postId eq postId }
                .map { it[CommunityPostsTable.communityId].value }

            assertEquals(2, associatedCommunities.size)
            communityIds.forEach { communityId ->
                assertTrue(associatedCommunities.contains(communityId))
            }
        }
    }

    @Test
    fun `createPost with media URLs stores them correctly`() {
        val title = "Test Post with Media"
        val content = "Content with media attachments"
        val tags = listOf("image", "media")

        val user = createUser("testuser", "test@test.com", "password")
        val mediaUrls = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/video.mp4",
        )

        val result = dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = emptyList(),
            mediaUrls = mediaUrls,
            createdBy = user.id.value,
        )

        // Verify the post was created with media URLs
        result.shouldBeInstanceOf<com.yappeer.domain.posts.model.Post>()
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(3, result.mediaUrls.size)
        result.mediaUrls.shouldContainAll(mediaUrls)

        // Verify media URLs in database
        transaction {
            val createdPost = PostDAO.all().firstOrNull()
            val postId = createdPost?.id?.value!!

            val storedMediaUrls = PostMediaTable
                .selectAll().where { PostMediaTable.postId eq postId }
                .map { it[PostMediaTable.mediaUrl] }

            assertEquals(3, storedMediaUrls.size)
            mediaUrls.forEach { url ->
                assertTrue(storedMediaUrls.contains(url))
            }
        }
    }

    @Test
    fun `homePosts returns paginated posts sorted by likes`() {
        runBlocking {
            // Given
            val user = createUser("testuser", "test@test.com", "password")
            val now = Clock.System.now()

            // Create posts with different like counts and creation times to test sorting and pagination
            createPost("Post 1", "Content 1", user, likes = 5, createdAt = now.minus(duration = 2.hours))
            createPost("Post 2", "Content 2", user, likes = 2, createdAt = now.minus(duration = 1.hours))
            createPost("Post 3", "Content 3", user, likes = 10, createdAt = now)

            // When - Page 1
            var result = dataSource.homePosts(page = 1, pageSize = 2)

            // Then - Page 1
            result.shouldBeInstanceOf<PostsResult>()
            result.posts.size shouldBe 2
            result.posts[0].title shouldBe "Post 3"
            result.posts[1].title shouldBe "Post 1"

            // When - Page 2
            result = dataSource.homePosts(page = 2, pageSize = 2)

            // Then - Page 2
            result.shouldBeInstanceOf<PostsResult>()
            result.posts.size shouldBe 1
            result.posts[0].title shouldBe "Post 2"
        }
    }

    @Test
    fun `updateLikeStats should increment likes`() {
        val user = createUser("testuser", "test@test.com", "password")
        val now = Clock.System.now()

        createPost("Post 1", "Content 1", user, likes = 0, createdAt = now.minus(duration = 2.hours))

        val postId = transaction { PostDAO.all().first().id.value }
        val status = LikeStatus.Like
        val result = dataSource.updateLikeStats(postId, user.id.value, status)

        result shouldBe true

        val updatedPost = transaction { PostDAO.findById(postId)!! }
        updatedPost.likes shouldBe 1
        updatedPost.dislikes shouldBe 0
    }

    @Test
    fun `updateLikeStats should increment dislikes`() {
        val user = createUser("testuser", "test@test.com", "password")
        val now = Clock.System.now()

        createPost("Post 1", "Content 1", user, likes = 0, createdAt = now.minus(duration = 2.hours))

        val postId = transaction { PostDAO.all().first().id.value }
        val result = dataSource.updateLikeStats(postId, user.id.value, LikeStatus.Dislike)

        result shouldBe true

        val updatedPost = transaction { PostDAO.findById(postId)!! }
        updatedPost.likes shouldBe 0
        updatedPost.dislikes shouldBe 1
    }

    @Test
    fun `updateLikeStats with LikeStatus Neutral from Like should decrease the likes counter`() {
        val user = createUser("testuser", "test@test.com", "password")
        val now = Clock.System.now()

        createPost("Post 1", "Content 1", user, likes = 0, createdAt = now.minus(duration = 2.hours))
        val postId = transaction { PostDAO.all().first().id.value }

        // Set status to Like
        val result1 = dataSource.updateLikeStats(postId, user.id.value, LikeStatus.Like)
        result1 shouldBe true
        val updatedPost1 = transaction { PostDAO.findById(postId)!! }
        updatedPost1.likes shouldBe 1

        // Set Status to Neutral from like
        val result2 = dataSource.updateLikeStats(postId, user.id.value, LikeStatus.Neutral)
        result2 shouldBe true
        val updatedPost = transaction { PostDAO.findById(postId)!! }
        updatedPost.likes shouldBe 0
        updatedPost.dislikes shouldBe 0
    }

    @Test
    fun `createPost returns post with accurate tag follower counts`() {
        // Create users
        val postCreator = createUser("postuser", "post@test.com", "password")
        val follower1 = createUser("follower1", "follower1@test.com", "password")
        val follower2 = createUser("follower2", "follower2@test.com", "password")

        // Create tags with followers
        val tag1 = createTag("popular-tag")
        val tag2 = createTag("new-tag") // No followers

        // Set up followers for tag1
        transaction {
            UserTagSubsTable.insert {
                it[userId] = follower1.id.value
                it[tagId] = tag1.id.value
            }

            UserTagSubsTable.insert {
                it[userId] = follower2.id.value
                it[tagId] = tag1.id.value
            }
        }

        // Create post with both tags
        val title = "Follower Count Test Post"
        val content = "This post tests tag follower counts"
        val tags = listOf("popular-tag", "new-tag")

        val result = dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = emptyList(),
            mediaUrls = emptyList(),
            createdBy = postCreator.id.value,
        )

        // Verify the post and tag details
        result.shouldBeInstanceOf<com.yappeer.domain.posts.model.Post>()
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(2, result.tags.size)

        // Check the follower counts on tags
        val popularTag = result.tags.find { it.name == "popular-tag" }
        val newTag = result.tags.find { it.name == "new-tag" }

        assertEquals(2, popularTag?.followers)
        assertEquals(0, newTag?.followers)
    }

    @Test
    fun `posts returned by userPosts include media URLs`() {
        runBlocking {
            // Given
            val user = createUser("testuser", "test@test.com", "password")

            // Create post with media URLs
            val postId = transaction {
                val post = PostDAO.new {
                    this.title = "Post with Media"
                    this.content = "Content with media attachments"
                    this.createdBy = user.id
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.likes = 0
                    this.dislikes = 0
                    this.shares = 0
                }

                // Add media URLs to the post
                val mediaUrls = listOf("https://example.com/image1.jpg", "https://example.com/video.mp4")
                mediaUrls.forEach { url ->
                    PostMediaTable.insert {
                        it[PostMediaTable.postId] = post.id
                        it[PostMediaTable.mediaUrl] = url
                        it[PostMediaTable.createdAt] = Clock.System.now().toJavaInstant()
                    }
                }

                post.id.value
            }

            // When
            val result = dataSource.userPosts(userId = user.id.value, pageSize = 10, page = 1)

            // Then
            result.shouldBeInstanceOf<PostsResult>()
            result.posts.size shouldBe 1
            val resultPost = result.posts.first()
            assertEquals(postId, resultPost.id)
            assertEquals(2, resultPost.mediaUrls.size)
            assertTrue(resultPost.mediaUrls.contains("https://example.com/image1.jpg"))
            assertTrue(resultPost.mediaUrls.contains("https://example.com/video.mp4"))
        }
    }

    private fun createUser(username: String, email: String, passwordHash: String): UserDAO =
        transaction(database) {
            UserDAO.new {
                this.username = username
                this.email = email
                this.passwordHash = passwordHash
                this.createdAt = Clock.System.now().toJavaInstant()
            }
        }

    private fun createCommunity(name: String, creator: UserDAO): CommunitiesDAO = transaction(database) {
        CommunitiesDAO.new {
            this.name = name
            this.creatorId = creator.id
            this.createdAt = Clock.System.now().toJavaInstant()
            this.isPrivate = false
        }
    }

    private fun createTag(name: String): TagDAO = transaction(database) {
        TagDAO.new { this.name = name }
    }

    private fun createPost(
        title: String,
        content: String,
        creator: UserDAO,
        communities: List<CommunitiesDAO>,
        tags: List<TagDAO>,
    ): PostDAO = transaction(database) {
        PostDAO.new {
            this.title = title
            this.content = content
            this.createdBy = creator.id
            this.createdAt = Clock.System.now().toJavaInstant()
            this.likes = 0
            this.dislikes = 0
            this.shares = 0
        }.also { post ->
            communities.forEach { community -> createCommunityPost(community, post) }
            tags.forEach { tag -> createPostTag(post, tag) }
        }
    }

    private fun createPost(
        title: String,
        content: String,
        creator: UserDAO,
        likes: Int,
        createdAt: Instant,
    ): PostDAO = transaction(database) {
        PostDAO.new {
            this.title = title
            this.content = content
            this.createdBy = creator.id
            this.createdAt = createdAt.toJavaInstant()
            this.likes = likes
            this.dislikes = 0
            this.shares = 0
        }
    }

    private fun createCommunityPost(community: CommunitiesDAO, post: PostDAO) = transaction(database) {
        CommunityPostsDAO.new {
            this.communityId = community.id
            this.postId = post.id
        }
    }

    private fun createPostTag(post: PostDAO, tag: TagDAO) = transaction(database) {
        PostTagTable.insert {
            it[postId] = post.id
            it[tagId] = tag.id
        }
    }
}
