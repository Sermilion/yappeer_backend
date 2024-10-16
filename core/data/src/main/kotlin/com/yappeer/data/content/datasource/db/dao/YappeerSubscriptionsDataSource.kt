package com.yappeer.data.content.datasource.db.dao

import com.yappeer.data.onboarding.datasource.db.dao.TagDAO
import com.yappeer.data.onboarding.datasource.db.dao.TagTable
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTagSubsTable
import com.yappeer.data.onboarding.datasource.db.dao.UserUserSubsTable
import com.yappeer.data.onboarding.mapper.TagDaoMapper.toDomainModel
import com.yappeer.data.onboarding.mapper.UserDaoMapper.toDomainModel
import com.yappeer.domain.content.datasource.SubscriptionsDataSource
import com.yappeer.domain.content.model.Tag
import com.yappeer.domain.onboarding.model.User
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class YappeerSubscriptionsDataSource : SubscriptionsDataSource {

    override fun findFollowers(userId: UUID, page: Int, pageSize: Int): List<User> = transaction {
        (UserTable.innerJoin(UserUserSubsTable, { UserTable.id }, { UserUserSubsTable.userId }))
            .selectAll()
            .where { UserUserSubsTable.subId eq userId }
            .limit(pageSize).offset((page - 1) * pageSize.toLong())
            .map { UserDAO.wrapRow(it).toDomainModel() }
    }

    override fun findFollowing(userId: UUID, page: Int, pageSize: Int): List<User> {
        return transaction {
            (UserTable.innerJoin(UserUserSubsTable, { UserTable.id }, { subId }))
                .selectAll().where { UserUserSubsTable.userId eq userId }
                .andWhere { UserTable.id eq UserUserSubsTable.subId }
                .limit(pageSize).offset((page - 1) * pageSize.toLong())
                .map { UserDAO.wrapRow(it).toDomainModel() }
        }
    }

    override fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): List<Tag> {
        return transaction {
            (TagTable.leftJoin(UserTagSubsTable, { TagTable.id }, { tagId }))
                .select(TagTable.id, TagTable.name, UserTagSubsTable.userId.count())
                .limit(pageSize).offset((page - 1) * pageSize.toLong())
                .groupBy(TagTable.id, TagTable.name)
                .map {
                    val followerCount = it[UserTagSubsTable.userId.count()]
                    TagDAO.wrapRow(it).toDomainModel(followerCount)
                }
        }
    }
}
