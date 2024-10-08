package com.sermilion.data.onboarding.datasource

import com.sermilion.data.exposed.ExposedQueryUtil.selectRow
import com.sermilion.data.onboarding.datasource.db.SqlErrorCodes
import com.sermilion.domain.onboarding.datasorce.UserCredentialsDataSource
import com.sermilion.domain.onboarding.model.User
import com.sermilion.domain.onboarding.model.result.SqlRegistrationResult
import com.sermilion.domain.onboarding.model.value.Password
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory

class OilaUserCredentialsDataSource : UserCredentialsDataSource {

    private val logger = LoggerFactory.getLogger(OilaUserCredentialsDataSource::class.java)

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
                    this.avatar = null
                }

                val user = User(
                    id = result.id.value,
                    username = result.username,
                    email = result.email,
                    avatar = result.avatar,
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
            selectRow { UserCredentialsTable.username eq username }?.passwordHash
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding password for username: `$username`", e)
            null
        }
        return result?.let { Password(it) }
    }

    override fun findUser(username: String): User? {
        val result = try {
            selectRow { UserCredentialsTable.username eq username }
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding user for username: `$username`", e)
            null
        }
        return result?.let {
            User(
                id = result.id.value,
                username = result.username,
                email = result.email,
                avatar = result.avatar,
            )
        }
    }
}
