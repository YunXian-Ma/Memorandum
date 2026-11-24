package com.jfeng2048.memorandum

import android.app.Application
import android.util.Log
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Hilt 的注入入口。
 * 首次启动时在 IO 线程写入演示数据，不阻塞首帧。
 */
@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var personRepository: PersonRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        appScope.launch {
            runCatching { personRepository.seedIfNeeded() }
                .onFailure { Log.e(TAG, "写入演示数据失败", it) }
        }
    }

    /**
     * 全局异常只做记录，然后交还给系统默认处理，避免影响崩溃上报。
     */
    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "未捕获异常，线程=${thread.name}", throwable)
            previous?.uncaughtException(thread, throwable)
        }
    }

    private companion object {
        const val TAG = "Memorandum"
    }
}
