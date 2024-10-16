package com.yappeer.data.onboarding.datasource

import com.yappeer.data.exposed.ExposedQueryUtil.selectUserRow
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.datasource.db.model.result.SqlErrorCodes
import com.yappeer.data.onboarding.mapper.UserDaoMapper.toDomainModel
import com.yappeer.domain.onboarding.datasorce.UserDataSource
import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import java.util.UUID

class YappeerUserDataSource : UserDataSource {

    private val logger = LoggerFactory.getLogger(YappeerUserDataSource::class.java)

    override fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult {
        return try {
            transaction {
                val user = UserDAO.new {
                    this.passwordHash = hashedPassword
                    this.createdAt = Clock.System.now().toJavaInstant()
                    this.email = email
                    this.lastLogin = null
                    this.username = username
                }.toDomainModel()

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
}
