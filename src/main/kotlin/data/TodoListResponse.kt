package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class TodoListResponse(
    val status: String,
    val message: String,
    val data: TodoListData
)
