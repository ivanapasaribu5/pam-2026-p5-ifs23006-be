package org.delcom.laundry.module

import org.delcom.laundry.repositories.*
import org.delcom.laundry.services.AuthService
import org.delcom.laundry.services.OrderService
import org.delcom.laundry.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {

    // User Repository
    single<IUserRepository> { UserRepository() }

    // User Service
    single { UserService(get()) }

    // RefreshToken Repository
    single<IRefreshTokenRepository> { RefreshTokenRepository() }

    // Auth Service
    single { AuthService(jwtSecret, get(), get()) }

    // Order Repository
    single<IOrderRepository> { OrderRepository() }

    // Order Service
    single { OrderService(get(), get()) }
}
