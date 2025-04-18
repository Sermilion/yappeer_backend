package com.yappeer.domain.posts.model

enum class LikeStatus {
    Like,
    Neutral,
    Dislike,
}

val LikeStatus.value: Int
    get() = when (this) {
        LikeStatus.Like -> 1
        LikeStatus.Dislike -> -1
        LikeStatus.Neutral -> 0
    }
