/*
 * Copyright 2020-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.api.http.route

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.HttpApiPluginBase
import net.mamoe.mirai.api.http.data.StateCode
import net.mamoe.mirai.api.http.data.common.DTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import net.mamoe.mirai.api.http.generateSessionKey
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.sendTo

/**
 * 群文件管理路由
 */

fun Application.fileRouteModule() {

    routing {

        /**
         * 修改群文件/目录名字
         */
        miraiVerify<FileRenameDTO>("/groupFileRename") { dto ->
            val file =
                dto.session.bot.getGroupOrFail(dto.target).filesRoot.resolveById(dto.id) ?: error("文件ID ${dto.id} 不存在")
            val success = file.renameTo(dto.rename)
            call.respondStateCode(
                if (success) StateCode.Success
                else StateCode.PermissionDenied
            )
        }

        /**
         * 移动群文件位置
         */

        miraiVerify<FilePathMoveDTO>("/groupFileMove") { dto ->
            val file =
                dto.session.bot.getGroupOrFail(dto.target).filesRoot.resolveById(dto.id) ?: error("文件ID ${dto.id} 不存在")
            val dir = dto.session.bot.getGroupOrFail(dto.target).filesRoot.resolve(dto.movePath)
            if (!dir.exists() || dir.isFile())
                if (!dir.mkdir()) {
                    call.respondStateCode(StateCode.PermissionDenied)
                    return@miraiVerify
                }
            val success = file.moveTo(dto.movePath)
            call.respondStateCode(
                if (success) StateCode.Success
                else StateCode.PermissionDenied
            )
        }

        /**
         * 删除群文件/目录
         */

        miraiVerify<FileDeleteDTO>("/groupFileDelete") { dto ->
            val file =
                dto.session.bot.getGroupOrFail(dto.target).filesRoot.resolveById(dto.id)
                    ?: error("文件/目录ID ${dto.id} 不存在")
            val success = file.delete()
            call.respondStateCode(
                if (success) StateCode.Success
                else StateCode.PermissionDenied
            )
        }

        /**
         * 转发群文件
         */

//        TODO() //等待以后支持
//          miraiVerify<FileCopyToDTO>("/FileCopyTO") {
//         error("暂未支持，等待core更新")
//            val file = when (dto.fromType) {
//                "Group" ->
//                    dto.session.bot.getGroupOrFail(dto.from).filesRoot.resolveById(dto.id)
//                        ?: error("文件/目录ID ${dto.id} 不存在")
//                else -> error("暂未支持，等待core更新")
//            }
//            val success = when (dto.type) {
//                "Group" -> file.copyTo()....
//                else -> error("暂未支持，等待core更新")
//            }
//    }

        /**
         * 上传文件并且发送
         */

        miraiMultiPart("/uploadFileAndSend") { session, parts ->
            val type = parts.value("type")
            val target = parts.value("target").toLongOrNull() ?: error("target不能为空")
            val path = parts.value("path")
            val file = parts.file("file") ?: error("file不能为空")
            var messageChain: MessageChain
            val newFile = HttpApiPluginBase.saveFileAsync(
                file.originalFileName ?: generateSessionKey(), file.provider().readBytes()
            )
            when (type) {
                "Group" -> session.bot.getGroupOrFail(target).let { group ->
                    group.filesRoot.resolve(path).let { remoteFile ->
                        if (remoteFile.parent != null && !remoteFile.exists()) {
                            if (!remoteFile.mkdir())
                                call.respondStateCode(StateCode.PermissionDenied)
                            return@miraiMultiPart
                        } else messageChain =
                            newFile.await().sendTo(group, "/$path").source.originalMessage
                    }
                }
                else -> error("不支持类型 $type")
            }

            call.respondDTO(UploadFileRetDTO(id = messageChain.firstIsInstance<FileMessage>().id))
        }

    }

}

@Serializable
data class FileRenameDTO(
    override val sessionKey: String,
    val id: String,
    val target: Long,
    val rename: String
) : VerifyDTO()

@Serializable
data class FilePathMoveDTO(
    override val sessionKey: String,
    val id: String,
    val target: Long,
    val movePath: String
) : VerifyDTO()

@Serializable
data class FileDeleteDTO(
    override val sessionKey: String,
    val id: String,
    val target: Long
) : VerifyDTO()

@Serializable
@Suppress("unused")
private class UploadFileRetDTO(
    val code: Int = 0,
    val msg: String = "success",
    val id: String
) : DTO
