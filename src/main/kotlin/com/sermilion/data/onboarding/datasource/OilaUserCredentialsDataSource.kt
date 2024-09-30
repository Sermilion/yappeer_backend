package com.sermilion.data.onboarding.datasource

import com.sermilion.data.onboarding.db.SqlErrorCodes
import com.sermilion.data.onboarding.db.model.UserCredentialsDAO
import com.sermilion.data.onboarding.db.model.UserCredentialsTable
import com.sermilion.data.onboarding.db.model.result.SqlRegistrationResult
import com.sermilion.data.onboarding.model.registration.UserResultDataModel
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.model.value.Password
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
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
                    this.createdAt = Clock.System.now().toString()
                    this.email = email
                    this.lastLogin = Instant.DISTANT_PAST.toString()
                }

                val user = UserResultDataModel(
                    id = result.id.toString(),
                    username = result.username,
                    email = result.email,
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
            transaction {
                UserCredentialsTable.selectAll().where {
                    (UserCredentialsTable.username eq username)
                }.firstOrNull()?.let {
                    UserCredentialsDAO.wrapRow(it)
                }
            }?.passwordHash
        } catch (e: ExposedSQLException) {
            logger.info("Exception while finding password for username: `$username`", e)
            null
        }
        return result?.let { Password(it) }
    }
}
