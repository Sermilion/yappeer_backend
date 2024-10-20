package com.yappeer.data.exposed

import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedQueryUtil {
    fun selectUserRow(predicate: SqlExpressionBuilder.() -> Op<Boolean>): UserDAO? {
        return transaction {
            UserTable.selectAll().where {
                predicate()
            }.firstOrNull()?.let {
                UserDAO.wrapRow(it)
            }
        }
    }
}
