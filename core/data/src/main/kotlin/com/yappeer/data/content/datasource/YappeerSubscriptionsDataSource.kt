package com.yappeer.data.content.datasource

import com.yappeer.data.content.datasource.db.dao.TagDAO
import com.yappeer.data.content.datasource.db.dao.TagTable
import com.yappeer.data.content.datasource.db.dao.UserTagSubsTable
import com.yappeer.data.content.datasource.db.dao.UserUserSubsTable
import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.onboarding.mapper.TagDaoMapper.toDomainModel
import com.yappeer.data.onboarding.mapper.UserDaoMapper.toDomainModel
import com.yappeer.domain.content.datasource.SubscriptionsDataSource
import com.yappeer.domain.content.model.FollowersResult
import com.yappeer.domain.content.model.TagResult
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.UUID

class YappeerSubscriptionsDataSource : SubscriptionsDataSource {
    private val logger = LoggerFactory.getLogger(YappeerSubscriptionsDataSource::class.java.name)

    override fun findFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResult {
        return try {
            transaction {
                val totalFollowers =
                    UserUserSubsTable.selectAll().where { UserUserSubsTable.subId eq userId }.count()

                val followers = (UserTable.innerJoin(UserUserSubsTable, { UserTable.id }, { UserUserSubsTable.userId }))
                    .selectAll()
                    .where { UserUserSubsTable.subId eq userId }
                    .limit(pageSize).offset((page - 1) * pageSize.toLong())
                    .map { UserDAO.wrapRow(it).toDomainModel() }

                val pagesCount = if (totalFollowers % pageSize == 0L) {
                    totalFollowers / pageSize
                } else {
                    totalFollowers / pageSize + 1
                }
                FollowersResult.Data(
                    users = followers,
                    totalUserCount = totalFollowers,
                    pagesCount = pagesCount,
                    currentPage = page,
                )
            }
        } catch (e: ExposedSQLException) {
            val message = "Error fetching followers."
            logger.error(message, e)
            FollowersResult.Error
        }
    }

    override fun findFollowing(userId: UUID, page: Int, pageSize: Int): FollowersResult {
        return try {
            transaction {
                val totalFollowing =
                    UserUserSubsTable.selectAll().where { UserUserSubsTable.userId eq userId }.count()

                val followings = (UserTable.innerJoin(UserUserSubsTable, { UserTable.id }, { subId }))
                    .selectAll().where { UserUserSubsTable.userId eq userId }
                    .andWhere { UserTable.id eq UserUserSubsTable.subId }
                    .limit(pageSize).offset((page - 1) * pageSize.toLong())
                    .map { UserDAO.wrapRow(it).toDomainModel() }

                val pagesCount = if (totalFollowing % pageSize == 0L) {
                    totalFollowing / pageSize
                } else {
                    totalFollowing / pageSize + 1
                }
                FollowersResult.Data(
                    users = followings,
                    totalUserCount = totalFollowing,
                    pagesCount = pagesCount,
                    currentPage = page,
                )
            }
        } catch (e: ExposedSQLException) {
            val message = "Error fetching following."
            logger.error(message, e)
            FollowersResult.Error
        }
    }

    override fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagResult {
        return try {
            transaction {
                val totalTags = (
                    TagTable.alias("TAG")
                        .innerJoin(UserTagSubsTable, { TagTable.id }, { tagId })
                    )
                    .selectAll().where { UserTagSubsTable.userId eq userId }
                    .count()

                val dbResult = (TagTable.innerJoin(UserTagSubsTable, { TagTable.id }, { tagId }))
                    .select(TagTable.id, TagTable.name, UserTagSubsTable.userId.count())
                    .limit(pageSize).offset((page - 1) * pageSize.toLong())
                    .groupBy(TagTable.id, TagTable.name)

                val pagesCount = if (totalTags % pageSize == 0L) {
                    totalTags / pageSize
                } else {
                    totalTags / pageSize + 1
                }

                val result = dbResult.map {
                    val followerCount = it[UserTagSubsTable.userId.count()]
                    TagDAO.wrapRow(it).toDomainModel(followerCount)
                }
                TagResult.Data(result, pagesCount, page)
            }
        } catch (e: ExposedSQLException) {
            val message = "Error fetching tags."
            logger.error(message, e)
            TagResult.Error
        }
    }
}
