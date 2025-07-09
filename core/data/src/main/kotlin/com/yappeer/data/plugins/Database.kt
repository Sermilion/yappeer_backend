package com.yappeer.data.plugins

import com.yappeer.data.communities.db.dao.CommunitiesTable
import com.yappeer.data.onboarding.datasource.db.dao.UserTable
import com.yappeer.data.posts.datasource.db.dao.CommunityPostsTable
import com.yappeer.data.posts.datasource.db.dao.PostLikesDislikesTable
import com.yappeer.data.posts.datasource.db.dao.PostMediaTable
import com.yappeer.data.posts.datasource.db.dao.PostTable
import com.yappeer.data.posts.datasource.db.dao.PostTagTable
import com.yappeer.data.subscriptions.datasource.db.dao.TagTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserCommunitySubsTable
import com.yappeer.data.subscriptions.datasource.db.dao.UserTagSubsTable
import io.ktor.server.application.Application
import org.h2.util.SortedProperties.loadProperties
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val properties = loadProperties("local.properties")

    val username = properties.getProperty("postgres.username")
    val password = properties.getProperty("postgres.password")
    val url = properties.getProperty("postgres.url")

    Database.connect(
        url = url,
        user = username,
        password = password,
    )

    transaction {
        SchemaUtils.create(
            UserTable,
            PostTable,
            CommunitiesTable,
            TagTable,
            UserTagSubsTable,
            PostTagTable,
            CommunityPostsTable,
            UserCommunitySubsTable,
            PostLikesDislikesTable,
            PostMediaTable,
            inBatch = true
        )
    }
}
