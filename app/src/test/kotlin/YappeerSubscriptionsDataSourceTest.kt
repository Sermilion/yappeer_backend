import com.yappeer.data.content.datasource.db.dao.YappeerSubscriptionsDataSource
import com.yappeer.data.onboarding.datasource.db.dao.TagDAO
import com.yappeer.data.onboarding.datasource.db.dao.TagTable
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTagSubsDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTagSubsTable
import com.yappeer.data.onboarding.datasource.db.dao.UserUserSubsDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserUserSubsTable
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class YappeerSubscriptionsDataSourceTest {

    private lateinit var database: Database
    private lateinit var yappeerSubscriptionsDataSource: YappeerSubscriptionsDataSource

    @BeforeEach
    fun setUp() {
        // Initialize an in-memory H2 database for testing
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        // Create tables using Exposed's SchemaUtils
        transaction(database) {
            SchemaUtils.create(
                UserTable,
                TagTable,
                UserUserSubsTable,
                UserTagSubsTable,
            )
        }

        yappeerSubscriptionsDataSource = YappeerSubscriptionsDataSource()
    }

    @AfterEach
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(
                UserUserSubsTable,
                UserTagSubsTable,
                TagTable,
                UserTable,
            )
        }
    }

    @Test
    fun findFollowing() = runBlocking {
        // given
        val list = prepareUserList()
        val subscriber = list[0]
        createUserUserSubscription(subscriber.id.value, list[1].id.value)
        createUserUserSubscription(subscriber.id.value, list[2].id.value)
        createUserUserSubscription(subscriber.id.value, list[3].id.value)
        createUserUserSubscription(subscriber.id.value, list[4].id.value)
        createUserUserSubscription(subscriber.id.value, list[5].id.value)
        createUserUserSubscription(subscriber.id.value, list[6].id.value)

        // when
        val followers = yappeerSubscriptionsDataSource.findFollowing(
            userId = subscriber.id.value,
            pageSize = 2,
            page = 2,
        )

        // then
        assertEquals(2, followers.size)
        assertEquals(list[3].id.value, followers[0].id)
        assertEquals(list[4].id.value, followers[1].id)
    }

    @Test
    fun findFollowers() = runBlocking {
        // given
        val list = prepareUserList()
        val subscribee = list[0]
        createUserUserSubscription(userId = list[3].id.value, subId = subscribee.id.value)
        createUserUserSubscription(userId = list[2].id.value, subId = subscribee.id.value)
        createUserUserSubscription(userId = list[3].id.value, subId = subscribee.id.value)
        createUserUserSubscription(userId = list[4].id.value, subId = subscribee.id.value)
        createUserUserSubscription(userId = list[5].id.value, subId = subscribee.id.value)
        createUserUserSubscription(userId = list[6].id.value, subId = subscribee.id.value)

        // when
        val followers = yappeerSubscriptionsDataSource.findFollowers(
            userId = subscribee.id.value,
            pageSize = 2,
            page = 2,
        )

        // then
        assertEquals(2, followers.size)
        assertEquals(list[3].id.value, followers[0].id)
        assertEquals(list[4].id.value, followers[1].id)
    }

    @Test
    fun findFollowedTagsWithCount() = runBlocking {
        // Given
        val testUser1 = createUser("testUser1", "test1@test.com", "passwordHash1")
        val testUser2 = createUser("testUser2", "test2@test.com", "passwordHash2")
        val testTag1 = createTag("tag1")
        val testTag2 = createTag("tag2")
        createUserTagSubscription(testUser1.id.value, testTag1.id.value)
        createUserTagSubscription(testUser1.id.value, testTag2.id.value)
        createUserTagSubscription(testUser2.id.value, testTag1.id.value)

        // When
        val followedTags = yappeerSubscriptionsDataSource.findFollowedTags(
            userId = testUser1.id.value,
            pageSize = 10,
            page = 1,
        )

        // Then
        assertEquals(2, followedTags.size)

        // Assert details of the first followed tag (testTag1)
        val followedTag1 = followedTags.find { it.id == testTag1.id.value }
        assertEquals(testTag1.name, followedTag1?.name)
        assertEquals(2, followedTag1?.followers) // Two users follow this tag

        // Assert details of the second followed tag (testTag2)
        val followedTag2 = followedTags.find { it.id == testTag2.id.value }
        assertEquals(testTag2.name, followedTag2?.name)
        assertEquals(1, followedTag2?.followers) // Only one user follows this tag
    }

    private fun createTag(tagName: String): TagDAO = transaction(database) {
        TagDAO.new {
            this.name = tagName
        }
    }

    private fun createUserTagSubscription(userId: UUID, tagId: UUID) = transaction(database) {
        UserTagSubsDAO.new {
            this.userId = EntityID(userId, UserTable)
            this.tagId = EntityID(tagId, TagTable)
        }
    }

    private fun createUser(
        username: String,
        email: String,
        passwordHash: String,
    ): UserDAO = transaction(database) {
        UserDAO.new {
            this.username = username
            this.email = email
            this.passwordHash = passwordHash
            this.createdAt = Clock.System.now().toJavaInstant()
        }
    }

    private fun createUserUserSubscription(userId: UUID, subId: UUID) = transaction(database) {
        UserUserSubsDAO.new {
            this.userId = EntityID(userId, UserTable)
            this.subId = EntityID(subId, UserTable)
        }
    }

    private fun prepareUserList(): List<UserDAO> {
        val testUser1 = createUser("testUser1", "test1@test.com", "passwordHash1")
        val testUser2 = createUser("testUser2", "test2@test.com", "passwordHash2")
        val testUser3 = createUser("testUser3", "test3@test.com", "passwordHash3")
        val testUser4 = createUser("testUser4", "test4@test.com", "passwordHash4")
        val testUser5 = createUser("testUser5", "test5@test.com", "passwordHash5")
        val testUser6 = createUser("testUser6", "test6@test.com", "passwordHash6")
        val testUser7 = createUser("testUser7", "test7@test.com", "passwordHash7")
        return listOf(
            testUser1,
            testUser2,
            testUser3,
            testUser4,
            testUser5,
            testUser6,
            testUser7,
        )
    }
}
