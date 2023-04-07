package com.example.courseproject.domain.cache.room


import com.example.courseproject.App
import com.example.courseproject.domain.api.IDataSource
import com.example.courseproject.domain.cache.IGithubUsersCache
import com.example.courseproject.entity.GithubUser
import com.example.courseproject.entity.room.Database
import com.example.courseproject.entity.room.RoomGithubUser
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RoomGithubUsersCache : IGithubUsersCache {
    @Inject
    lateinit var database: Database
    init{
        App.instance.appComponent.inject(this)
    }

    override fun newData(
        api: IDataSource
    ): Single<List<GithubUser>> {
        return api.getUsers()
            .flatMap { users ->
                Single.fromCallable {
                    val roomUsers = users.map { user ->
                        RoomGithubUser(
                            user.id,
                            user.login, user.avatar_url ?: "",
                            user.repos_url ?: ""
                        )
                    }

                    database.userDao.insert(roomUsers)
                    Thread { RoomGithubPictureCache().newData(users) }.start()
                    users
                }
            }
    }

    override fun fromDataBaseData(): Single<List<GithubUser>> {

        return Single.fromCallable {
            var out = database.userDao.getAll().map { roomUser ->
                GithubUser(
                    roomUser.id, roomUser.login, roomUser.avatar_url,
                    roomUser.repos_url
                )
            }
            out.forEach {
                val roomUser = it.login.let { database.userDao.findByLogin(it) }
                it.avatar_url = database.pictureDao.findForUser(roomUser.id).local_path
            }
            out
        }
    }

}