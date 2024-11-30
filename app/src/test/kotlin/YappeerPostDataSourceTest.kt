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
import com.yappeer.data.posts.datasource.db.dao.UserPostsDAO
import com.yappeer.data.posts.datasource.db.dao.UserPostsTable
import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import com.yappeer.domain.posts.model.PostsResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class YappeerPostDataSourceTest {
    private lateinit var database: Database
    private lateinit var yappeerCommunitiesDataSource: YappeerPostDataSource

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
                UserPostsTable,
            )
        }
        yappeerCommunitiesDataSource = YappeerPostDataSource()
    }

    @AfterEach
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(
                UserPostsTable,
                CommunityPostsTable,
                PostTagTable,
                UserTagSubsTable,
                TagTable,
                CommunitiesTable,
                PostTable,
                UserTable,
            )
        }
    }

    @Test
    fun `selfCommunityPosts returns correct posts with communities and tags`() {
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
            createUserPost(user.id.value, post1.id.value)
            val post2 = createPost("title2", "content2", user, listOf(community1), listOf(tag1))
            createUserPost(user.id.value, post2.id.value)

            // When
            val result =
                yappeerCommunitiesDataSource.userPosts(userId = user.id.value, pageSize = 10, page = 1)

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

    private fun createUserPost(userId: UUID, postId: UUID) {
        transaction {
            UserPostsDAO.new {
                this.userId = EntityID(userId, UserTable)
                this.postId = EntityID(postId, PostTable)
            }
        }
    }

    private fun createCommunityPost(community: CommunitiesDAO, post: PostDAO) = transaction(database) {
        CommunityPostsDAO.new {
            this.communityId = community.id
            this.postId = post.id
        }
    }

    private fun createCommunityPost(community: CommunitiesDAO, creator: UserDAO) = transaction(database) {
        UserPostsDAO.new {
            this.userId = EntityID(creator.id.value, UserTable)
            this.postId = EntityID(community.id.value, CommunitiesTable) // Using Community ID as Post ID
        }
    }

    private fun createPostTag(post: PostDAO, tag: TagDAO) = transaction(database) {
        PostTagDAO.new {
            this.postId = post.id
            this.tagId = tag.id
        }
    }
}
