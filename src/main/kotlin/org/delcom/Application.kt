package org.delcom

import io.ktor.server.application.Application
import org.delcom.laundry.laundryModule

/**
 * Ktor loads the module function by fully qualified name from `application.yaml`.
 * The implementation lives in `org.delcom.laundry`, so we delegate to it to keep
 * config stable (`org.delcom.ApplicationKt.module`).
 */
fun Application.module() {
    laundryModule()
}
