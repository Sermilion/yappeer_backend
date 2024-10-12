package com.yappeer.data.onboarding.datasource

import com.yappeer.data.exposed.ExposedQueryUtil.selectUSerCredentialsRow
import com.yappeer.data.onboarding.datasource.db.dao.TagTable
import com.yappeer.data.onboarding.datasource.db.dao.UserCredentialsDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserCredentialsTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTagTable
import com.yappeer.data.onboarding.datasource.db.model.result.SqlErrorCodes
import com.yappeer.domain.onboarding.datasorce.UserCredentialsDataSource
import com.yappeer.domain.onboarding.model.Tag
import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory

class YappeerUserCredentialsDataSource : UserCredentialsDataSource {

    private val logger = LoggerFactory.getLogger(YappeerUserCredentialsDataSource::class.java)

    override fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult {
        return try {
            transaction {
                val result = UserCredentialsDAO.new {
                    this.username = username
                    this.passwordHash = hashedPassword
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.email = email
                    this.lastLogin = null
                }

                val user = User(
                    id = result.id.value,
                    username = result.username,
                    email = result.email,
                    avatar = null,
                    bio = null,
                    createdAt = result.createdAt.toKotlinInstant(),
                    lastLogin = null,
                    tags = emptyList(),
                )

                SqlRegistrationResult.Success(user)
            }
        } catch (e: ExposedSQLException) {
            when (val cause = e.cause?.cause) {
                is PSQLException -> {
                    when (cause.sqlState) {
                        SqlErrorCodes.UNIQUE_CONSTRAINT_VIOLATION -> SqlRegistrationResult.ConstraintViolation
                        else -> SqlRegistrationResult.UnknownError
                    }
                }

                else -> SqlRegistrationResult.UnknownError
            }
        }
    }

    override fun findPassword(username: String): Password? {
        val result = try {
            selectUSerCredentialsRow { UserCredentialsTable.username eq username }?.passwordHash
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding password for username: `$username`", e)
            null
        }
        return result?.let { Password(it) }
    }

    override fun findUser(username: Username): User? {
        return try {
            transaction {
                val userId = selectUSerCredentialsRow { UserCredentialsTable.username eq username.value }?.id?.value
                if (userId == null) {
                    return@transaction null
                }

                (UserCredentialsTable.innerJoin(UserTable, { UserCredentialsTable.id }, { UserTable.id }))
                    .selectAll().where { (UserCredentialsTable.id eq userId) and (UserTable.id eq userId) }
                    .firstOrNull()
                    ?.let { result ->
                        val user = UserCredentialsDAO.wrapRow(result)
                        val bio = result[UserTable.bio]
                        val avatar = result[UserTable.avatar]

                        val tags = UserTagTable.join(
                            TagTable,
                            JoinType.INNER,
                            additionalConstraint = { UserTagTable.tagId eq TagTable.id },
                        )
                            .selectAll().where { (UserTagTable.userId eq userId) }
                            .map { resultRow ->
                                Tag(
                                    id = resultRow[TagTable.id].value,
                                    name = resultRow[TagTable.name],
                                )
                            }

                        val mappedTags = tags.map { Tag(id = it.id, name = it.name) }

                        User(
                            id = user.id.value,
                            username = user.username,
                            email = user.email,
                            createdAt = user.createdAt.toKotlinInstant(),
                            lastLogin = user.lastLogin?.toKotlinInstant(),
                            bio = bio,
                            avatar = avatar,
                            tags = mappedTags,
                        )
                    }
            }
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding user for username: `$username`", e)
            null
        }
    }
}
