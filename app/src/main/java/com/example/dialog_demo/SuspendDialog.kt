package com.example.dialog_demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CompletableDeferred

/** 控制句柄：用于手动关闭对话框 */
interface DialogHandle {
    fun dismiss()
}

/** 响应封装：携带数据和控制句柄 */
sealed interface DialogResp<out T> {
    data class Success<T>(val data: T, val handle: DialogHandle) : DialogResp<T>
    object Cancel : DialogResp<Nothing>
}

/** 请求基类：R 是预期的返回数据类型 */
@Stable
sealed interface DialogReq<R> {
    val autoDismiss: Boolean get() = true // 是否点击确定后自动隐藏

    @Composable
    fun Render(onResult: (DialogResp<R>) -> Unit)
}


class SuspendDialogManager {
    // 内部类：将请求和回调封装在一起
    data class DialogEntry<R>(
        val req: DialogReq<R>,
        val onResult: (DialogResp<R>) -> Unit
    )

    // 观察栈：支持多层叠加
    val requestStack = mutableStateListOf<DialogEntry<*>>()

    /** 执行请求并挂起 */
    suspend fun <R> execute(req: DialogReq<R>): DialogResp<R> {
        val deferred = CompletableDeferred<DialogResp<R>>()

        // 创建专属句柄
        val handle = object : DialogHandle {
            override fun dismiss() {
                requestStack.removeAll { it.req === req }
            }
        }

        // 包装回调逻辑
        val entry = DialogEntry(req) { resp ->
            when (resp) {
                is DialogResp.Cancel -> {
                    handle.dismiss()
                    deferred.complete(DialogResp.Cancel)
                }

                is DialogResp.Success -> {
                    if (req.autoDismiss) handle.dismiss()
                    // 传回成功结果和句柄，由调用方决定是否继续调用 handle.dismiss()
                    deferred.complete(DialogResp.Success(resp.data, handle))
                }
            }
        }

        requestStack.add(entry)

        return try {
            deferred.await()
        } finally {
            // 如果协程被取消且是自动关闭模式，清理 UI
            if (req.autoDismiss) handle.dismiss()
        }
    }
}


/**
 * 配合 remember 使用的辅助方法
 */
@Composable
fun rememberSuspendDialogManager() = remember { SuspendDialogManager() }

/** 宿主组件：渲染所有弹窗 */
@Composable
fun SuspendDialogHost(manager: SuspendDialogManager) {
    manager.requestStack.forEach { entry ->
        key(entry.req) {
            RenderEntry(entry)
        }
    }
}

@Composable
private fun <R> RenderEntry(entry: SuspendDialogManager.DialogEntry<R>) {
    entry.req.Render(onResult = entry.onResult)
}