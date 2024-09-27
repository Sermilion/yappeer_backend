package com.sermilion.data.onboarding.datasource

import com.sermilion.data.onboarding.db.model.result.SQLResult
import com.sermilion.data.onboarding.db.SqlErrorCodes
import com.sermilion.data.onboarding.db.model.UserCredentialsDAO
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PSQLException

class OilaUserCredentialsDataSource : UserCredentialsDataSource {

    override fun createUser(
        username: String,
        email: String,
        salt: String,
        hashedPassword: String,
    ): SQLResult {
        return try {
            transaction {
                UserCredentialsDAO.new {
                    this.username = username
                    this.passwordHash = hashedPassword
                    this.salt = salt
                    this.createdAt = Clock.System.now().toString()
                    this.email = email
                    this.lastLogin = "0"
                }
            }
            SQLResult.Success
        } catch (e: ExposedSQLException) {
            when (val cause = e.cause?.cause) {
                is PSQLException -> {
                    when (cause.sqlState) {
                        SqlErrorCodes.UNIQUE_CONSTRAINT_VIOLATION -> SQLResult.ConstraintViolation
                        else -> SQLResult.UnknownError
                    }
                }

                else -> SQLResult.UnknownError
            }
        } catch (e: Exception) {
            SQLResult.UnknownError
        }
    }
}
