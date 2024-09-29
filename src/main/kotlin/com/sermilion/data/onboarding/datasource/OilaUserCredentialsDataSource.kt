package com.sermilion.data.onboarding.datasource

import com.sermilion.data.onboarding.db.SqlErrorCodes
import com.sermilion.data.onboarding.db.model.UserCredentialsDAO
import com.sermilion.data.onboarding.db.model.result.SqlResult
import com.sermilion.data.onboarding.model1.registration.UserResultDataModel
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
    ): SqlResult {
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

                SqlResult.Success(user)
            }
        } catch (e: ExposedSQLException) {
            when (val cause = e.cause?.cause) {
                is PSQLException -> {
                    when (cause.sqlState) {
                        SqlErrorCodes.UNIQUE_CONSTRAINT_VIOLATION -> SqlResult.ConstraintViolation
                        else -> SqlResult.UnknownError
                    }
                }

                else -> SqlResult.UnknownError
            }
        } catch (e: IllegalStateException) {
            logger.error("Unknown while creating user in database", e)
            SqlResult.UnknownError
        }
    }
}
