package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Todo

@Serializable
data class TodoListData(
    val todos: List<Todo>,
    val totalTodos: Int,
    val completedTodos: Int,
    val incompleteTodos: Int,
    // Alias untuk kompatibilitas nama field di frontend
    val totalTodo: Int,
    val todoSelesai: Int,
    val todoBelumSelesai: Int,
)