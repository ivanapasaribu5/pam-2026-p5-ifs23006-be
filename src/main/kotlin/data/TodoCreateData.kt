package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Todo

@Serializable
data class TodoCreateData(
    val todoId: String,
    val todo: Todo
)
