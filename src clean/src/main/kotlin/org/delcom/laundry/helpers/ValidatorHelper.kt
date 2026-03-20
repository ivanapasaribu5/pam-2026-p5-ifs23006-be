package org.delcom.laundry.helpers

import org.delcom.laundry.data.AppException

class ValidatorHelper(private val data: Map<String, Any?>) {

    private val errors = mutableListOf<String>()

    fun addError(field: String, error: String) {
        errors.add("$field: $error")
    }

    fun required(field: String, message: String? = null) {
        val value = data[field]
        if (value == null || (value is String && value.isBlank())) {
            addError(field, message ?: "$field is required")
        }
    }

    fun minLength(field: String, min: Int, message: String? = null) {
        val value = data[field]
        if (value is String && value.length < min) {
            addError(field, message ?: "$field must be at least $min characters")
        }
    }

    fun maxLength(field: String, max: Int, message: String? = null) {
        val value = data[field]
        if (value is String && value.length > max) {
            addError(field, message ?: "$field must be at most $max characters")
        }
    }

    fun validate() {
        if (errors.isNotEmpty()) {
            throw AppException(400, errors.joinToString("|"))
        }
    }
}
