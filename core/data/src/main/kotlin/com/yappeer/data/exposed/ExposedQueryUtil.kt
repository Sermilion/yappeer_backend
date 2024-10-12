package com.yappeer.data.exposed

import com.yappeer.data.onboarding.datasource.db.dao.UserCredentialsDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserCredentialsTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedQueryUtil {
    fun selectUSerCredentialsRow(predicate: SqlExpressionBuilder.() -> Op<Boolean>): UserCredentialsDAO? {
        return transaction {
            UserCredentialsTable.selectAll().where {
                predicate()
            }.firstOrNull()?.let {
                UserCredentialsDAO.wrapRow(it)
            }
        }
    }
}
