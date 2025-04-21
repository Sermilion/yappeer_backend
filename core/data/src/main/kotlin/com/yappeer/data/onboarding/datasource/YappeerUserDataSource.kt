package com.yappeer.data.onboarding.datasource

import com.yappeer.data.exposed.ExposedQueryUtil.selectUserRow
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.mapper.UserDaoMapper.toDomainModel
import com.yappeer.domain.onboarding.datasorce.UserDataSource
import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.UserWithPassword
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.util.UUID

class YappeerUserDataSource : UserDataSource {

    private val logger = LoggerFactory.getLogger(YappeerUserDataSource::class.java)

    override fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): RegistrationResult {
        val existingUserCount = UserTable.selectAll().where { UserTable.email eq email }.count()
        if (existingUserCount > 0) {
            return RegistrationResult.Error(RegistrationErrorType.EmailTaken)
        }

        val existingUsernameCount = UserTable.selectAll().where { UserTable.username eq username }.count()
        return if (existingUsernameCount > 0) {
            RegistrationResult.Error(RegistrationErrorType.UsernameTaken)
        } else {
            transaction {
                val user = UserDAO.new {
                    this.passwordHash = hashedPassword
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.email = email
                    this.lastLogin = null
                    this.username = username
                }.toDomainModel()

                RegistrationResult.Success(user)
            }
        }
    }

    override fun findPassword(email: Email): Password? {
        val result = try {
            selectUserRow { UserTable.email eq email.value }?.passwordHash
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding password for email: `$email`", e)
            null
        }
        return result?.let { Password(it) }
    }

    override fun findUser(userId: UUID): User? {
        return try {
            selectUserRow { UserTable.id eq userId }?.toDomainModel()
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding user for userId: `$userId`", e)
            null
        }
    }

    override fun findUser(email: Email): User? {
        return try {
            selectUserRow { UserTable.email eq email.value }?.toDomainModel()
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding user for email: `$email`", e)
            null
        }
    }

    override fun updateLastLogin(userId: UUID, instant: Instant) {
        transaction {
            UserTable.update({ UserTable.id eq userId }) {
                it[lastLogin] = instant.toJavaInstant()
            }
        }
    }

    override fun findUserWithPassword(email: Email): UserWithPassword? {
        return transaction {
            UserTable.selectAll().where { UserTable.email eq email.value }
                .map {
                    UserWithPassword(
                        id = it[UserTable.id].value,
                        email = it[UserTable.email],
                        username = it[UserTable.username],
                        password = it[UserTable.password_hash],
                    )
                }
                .singleOrNull()
        }
    }
}
