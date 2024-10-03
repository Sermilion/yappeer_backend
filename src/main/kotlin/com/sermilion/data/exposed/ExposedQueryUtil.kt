package com.sermilion.data.exposed

import com.sermilion.data.onboarding.db.model.UserCredentialsDAO
import com.sermilion.data.onboarding.db.model.UserCredentialsTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedQueryUtil {
    fun selectRow(predicate: SqlExpressionBuilder.() -> Op<Boolean>): UserCredentialsDAO? {
        return transaction {
            UserCredentialsTable.selectAll().where {
                predicate()
            }.firstOrNull()?.let {
                UserCredentialsDAO.wrapRow(it)
            }
        }
    }
}
