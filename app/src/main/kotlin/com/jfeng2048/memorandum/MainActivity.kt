package com.jfeng2048.memorandum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jfeng2048.memorandum.core.designsystem.theme.MemorandumTheme
import com.jfeng2048.memorandum.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * 唯一 Activity：只负责挂主题与 NavHost，其余页面全部是 Compose。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 白底界面：状态栏、导航栏都用深色图标 + 透明底
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        setContent {
            MemorandumTheme {
                AppNavHost()
            }
        }
    }
}
