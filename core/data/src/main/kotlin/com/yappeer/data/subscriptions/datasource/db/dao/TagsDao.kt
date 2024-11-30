package com.yappeer.data.subscriptions.datasource.db.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

class TagDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagDAO>(TagTable)
    var name by TagTable.name
}

object TagTable : UUIDTable("tag") {
    val name = varchar("name", 50).uniqueIndex()
}
