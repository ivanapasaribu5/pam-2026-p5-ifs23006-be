package org.delcom.laundry.module

import org.delcom.laundry.repositories.IOrderRepository
import org.delcom.laundry.repositories.OrderRepository
import org.delcom.laundry.services.OrderService
import org.koin.dsl.module

fun appModule() = module {

    // Order Repository
    single<IOrderRepository> {
        OrderRepository()
    }

    // Order Service
    single {
        OrderService(get())
    }
}
