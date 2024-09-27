package com.sermilion.data.onboarding.db.model

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

class UserCredentialsDAO(id: EntityID<UUID>) : UUIDEntity(id) {

    companion object : UUIDEntityClass<UserCredentialsDAO>(UserCredentialsTable)

    var username by UserCredentialsTable.username
    var email by UserCredentialsTable.email
    var passwordHash by UserCredentialsTable.password_hash
    var salt by UserCredentialsTable.salt
    var createdAt by UserCredentialsTable.createdAt
    var lastLogin by UserCredentialsTable.lastLogin
}

object UserCredentialsTable : UUIDTable("user_credentials") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 50).uniqueIndex()
    val password_hash = text("password_hash")
    val salt = text("salt")
    val createdAt = text("created_at")
    val lastLogin = text("last_login")
}


//object TaskTable : IntIdTable("task") {
//    val name = varchar("name", 50)
//    val description = varchar("description", 50)
//    val priority = varchar("priority", 50)
//}
//
//class TaskDAO(id: EntityID<Int>) : IntEntity(id) {
//    companion object : IntEntityClass<TaskDAO>(TaskTable)
//
//    var name by TaskTable.name
//    var description by TaskTable.description
//    var priority by TaskTable.priority
//}
//
//suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
//    newSuspendedTransaction(Dispatchers.IO, statement = block)
//
//fun daoToModel(dao: TaskDAO) = Task(
//    dao.name,
//    dao.description,
//    Priority.valueOf(dao.priority)
//)