package org.delcom.laundry.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.AuthRequest
import org.delcom.laundry.data.DataResponse
import org.delcom.laundry.data.UserResponse
import org.delcom.laundry.helpers.ServiceHelper
import org.delcom.laundry.helpers.ValidatorHelper
import org.delcom.laundry.helpers.hashPassword
import org.delcom.laundry.helpers.verifyPassword
import org.delcom.laundry.repositories.IUserRepository
import java.io.File
import java.util.UUID

class UserService(
    private val userRepository: IUserRepository,
) {

    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        call.respond(
            DataResponse(
                "success",
                "Berhasil mengambil data profil",
                UserResponse(
                    id       = user.id,
                    name     = user.name,
                    username = user.username,
                    photo    = user.photo,
                    about    = user.about,
                )
            )
        )
    }

    suspend fun putMe(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepository)
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name",     "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
        if (existUser != null && existUser.id != user.id) {
            throw AppException(409, "Username sudah digunakan!")
        }

        userRepository.update(
            user.id,
            user.copy(name = request.name, username = request.username, about = request.name)
        )

        call.respond(DataResponse("success", "Berhasil memperbarui profil", null))
    }

    suspend fun putMyPassword(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepository)
        val request = call.receive<Map<String, String>>()

        val oldPassword = request["oldPassword"] ?: throw AppException(400, "Password lama tidak boleh kosong")
        val newPassword = request["newPassword"] ?: throw AppException(400, "Password baru tidak boleh kosong")

        if (newPassword.length < 6) throw AppException(400, "Password baru minimal 6 karakter")
        if (!verifyPassword(oldPassword, user.password)) throw AppException(400, "Password lama tidak sesuai")

        userRepository.update(user.id, user.copy(password = hashPassword(newPassword)))
        call.respond(DataResponse("success", "Berhasil memperbarui password", null))
    }

    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepository)
        var newPhotoPath: String? = null

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext      = part.originalFileName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/users/$fileName"
                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        newPhotoPath = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (newPhotoPath == null) throw AppException(400, "Foto tidak ditemukan")

        // Delete old photo
        user.photo?.let { oldPath ->
            val oldFile = File(oldPath)
            if (oldFile.exists()) oldFile.delete()
        }

        userRepository.updatePhoto(user.id, newPhotoPath!!)
        call.respond(DataResponse("success", "Berhasil memperbarui foto profil", null))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "ID user tidak valid")
        val user   = userRepository.getById(userId) ?: return call.respond(HttpStatusCode.NotFound)

        if (user.photo == null) throw AppException(404, "User belum memiliki foto")

        val file = File(user.photo!!)
        if (!file.exists()) throw AppException(404, "Foto tidak tersedia")

        call.respondFile(file)
    }
}
