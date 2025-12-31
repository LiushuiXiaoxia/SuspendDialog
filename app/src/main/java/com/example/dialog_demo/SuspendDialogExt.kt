package com.example.dialog_demo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

// --- 1. 确认框请求 ---
data class ConfirmReq(
    val title: String,
    val message: String,
    override val autoDismiss: Boolean = true
) : DialogReq<Unit> {

    @Composable
    override fun Render(onResult: (DialogResp<Unit>) -> Unit) {
        // 内部状态：如果是手动模式，点击后显示 Loading
        var isProcessing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isProcessing) onResult(DialogResp.Cancel) },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = {
                        if (!autoDismiss) isProcessing = true
                        onResult(DialogResp.Success(Unit, object : DialogHandle {
                            override fun dismiss() {}
                        }))
                    }
                ) {
                    if (isProcessing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("确定")
                }
            },
            dismissButton = {
                TextButton(enabled = !isProcessing, onClick = { onResult(DialogResp.Cancel) }) {
                    Text("取消")
                }
            }
        )
    }
}

// --- 2. 输入框请求 ---
data class InputReq(val title: String, val hint: String = "", override val autoDismiss: Boolean = true) : DialogReq<String> {
    @Composable
    override fun Render(onResult: (DialogResp<String>) -> Unit) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { onResult(DialogResp.Cancel) },
            title = { Text(title) },
            text = { TextField(value = text, onValueChange = { text = it }, placeholder = { Text(hint) }) },
            confirmButton = {
                Button(onClick = {
                    onResult(DialogResp.Success(text, object : DialogHandle {
                        override fun dismiss() {}
                    }))
                }) {
                    Text("提交")
                }
            },
            dismissButton = {
                TextButton(onClick = { onResult(DialogResp.Cancel) }) { Text("取消") }
            }
        )
    }
}

// --- 3. 加载框请求 (随任务结束自动消失) ---
class LoadingReq<T>(val msg: String, val block: suspend () -> T) : DialogReq<T> {
    @Composable
    override fun Render(onResult: (DialogResp<T>) -> Unit) {
        LaunchedEffect(Unit) {
            val result = block()
            onResult(DialogResp.Success(result, object : DialogHandle {
                override fun dismiss() {}
            }))
        }
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                    Spacer(Modifier.width(20.dp))
                    Text(msg)
                }
            }
        }
    }
}