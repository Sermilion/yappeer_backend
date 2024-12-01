package com.yappeer.data.subscriptions.datasource.db.dao

import com.yappeer.data.communities.db.dao.CommunitiesTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

class UserCommunitySubsDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserCommunitySubsDAO>(UserCommunitySubsTable)

    var userId by UserCommunitySubsTable.userId
    var communityId by UserCommunitySubsTable.communityId
}

object UserCommunitySubsTable : IdTable<Long>("user_community_subs") {
    override val id = long("id").autoIncrement().entityId()
    val userId = reference("user_id", UserTable.id)
    val communityId = reference("community_id", CommunitiesTable.id)

    override val primaryKey = PrimaryKey(userId, communityId)
}
