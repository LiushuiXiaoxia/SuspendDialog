package com.example.dialog_demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.dialog_demo.ui.theme.DialogdemoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DialogdemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // 1. 创建页面级的管理器
    val dialogManager = rememberSuspendDialogManager()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Hello $name!",
            )
            Button(onClick = {
                scope.launch {
                    val result = dialogManager.execute(ConfirmReq("标题", "内容", false))
                    if (result is DialogResp.Success) {
                        Toast.makeText(context, "result: ${result.data}", Toast.LENGTH_SHORT).show()
                        result.handle.dismiss()
                    } else {
                        Toast.makeText(context, "result: $result", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Show Confirm")
            }

            Button(onClick = {
                scope.launch {
                    val result = dialogManager.execute(InputReq("请输入标题", "请输入标题"))
                    Toast.makeText(context, "result: $result", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Show Input")
            }

            Button(onClick = {
                scope.launch {
                    val result = dialogManager.execute(LoadingReq("加载中...") {
                        delay(3000)
                        "加载完成"
                    })
                    Toast.makeText(context, "result: $result", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Show Loading")
            }

            Button(onClick = {
                scope.launch {
                    val input = dialogManager.execute(InputReq("请输入标题", "请输入标题", false))
                    if (input is DialogResp.Success) {
                        val result = dialogManager.execute(LoadingReq("加载中...") {
                            delay(3000)
                            "加载完成: ${input.data}"
                        })
                        input.handle.dismiss()
                        Toast.makeText(context, "input: $result", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "input: $input", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Show Multi Loading")
            }
        }
    }
    SuspendDialogHost(dialogManager)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DialogdemoTheme {
        Greeting("Android")
    }
}