package com.nonio.android

import com.nonio.android.network.TestNetworkHelper.apiService
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ApiUnitTest {
    @Before
    fun setUp() {
    }

    @Test
    fun testPosts() {
        runBlocking {
            val posts = apiService.posts()
            posts
        }
    }

    @Test
    fun testUserNameCheck() {
        runBlocking {
            apiService.userNameCheck("testusername")
        }
    }

    @Test
    fun testUserInfo() {
        runBlocking {
            apiService.userInfo("jjcm")
        }
    }

    @Test
    fun testLogin() {
        runBlocking {
            val userName = ""
            val pwd = ""
            apiService.login(
                mapOf(
                    "username" to userName,
                    "password" to pwd,
                ).toRequestBody(),
            )
        }
    }

    @Test
    fun testCommentPost() {
        val post = "A-tiny-embroidered-squiddy-magnet"
        runBlocking {
            val model = apiService.postByUrl(post)
            val commentPost = apiService.commentPost(post)
            commentPost
        }
    }

    @Test
    fun testTagQuery() {
        runBlocking {
            val model = apiService.tagQuery("t")
            model
        }
    }
}
