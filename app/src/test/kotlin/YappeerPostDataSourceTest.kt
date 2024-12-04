import com.yappeer.data.communities.db.dao.CommunitiesDAO
import com.yappeer.data.communities.db.dao.CommunitiesTable
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.posts.datasource.YappeerPostDataSource
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsDAO
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsTable
import com.yappeer.data.posts.datasource.db.dao.PostDAO
import com.yappeer.data.posts.datasource.db.dao.PostTable
import com.yappeer.data.posts.datasource.db.dao.PostTagDAO
import com.yappeer.data.posts.datasource.db.dao.PostTagTable
import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserCommunitySubsTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import com.yappeer.domain.posts.model.PostsResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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

        val result = dataSource.createPost(title, content, tags, user.id.value)

        assertTrue(result)

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
        val result = dataSource.createPost(title, content, tags, user.id.value)
        assertTrue(result)

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
        PostTagDAO.new {
            this.postId = post.id
            this.tagId = tag.id
        }
    }
}
