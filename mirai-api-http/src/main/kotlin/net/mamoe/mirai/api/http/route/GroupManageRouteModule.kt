/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.route

import io.ktor.application.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.data.StateCode
import net.mamoe.mirai.api.http.data.common.DTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

/**
 * 群管理路由
 */
fun Application.groupManageModule() {
    routing {

        /**
         * 禁言所有人（需要相关权限）
         */
        miraiVerify<MuteDTO>("/muteAll") {
            it.session.bot.getGroupOrFail(it.target).settings.isMuteAll = true
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 取消禁言所有人（需要相关权限）
         */
        miraiVerify<MuteDTO>("/unmuteAll") {
            it.session.bot.getGroupOrFail(it.target).settings.isMuteAll = false
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 禁言指定群成员（需要相关权限）
         */
        miraiVerify<MuteDTO>("/mute") {
            it.session.bot.getGroupOrFail(it.target).getOrFail(it.memberId).mute(it.time)
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 取消禁言指定群成员（需要相关权限）
         */
        miraiVerify<MuteDTO>("/unmute") {
            it.session.bot.getGroupOrFail(it.target).getOrFail(it.memberId).unmute()
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 移出群聊（需要相关权限）
         */
        miraiVerify<KickDTO>("/kick") {
            it.session.bot.getGroupOrFail(it.target).getOrFail(it.memberId).kick(it.msg)
            call.respondStateCode(StateCode.Success)
        }

        /**
         * Bot退出群聊（Bot不能为群主）
         */
        miraiVerify<QuitDTO>("/quit") {
            val success = it.session.bot.getGroupOrFail(it.target).quit()
            call.respondStateCode(
                if (success) StateCode.Success
                else StateCode.PermissionDenied
            )
        }

        /**
         * 获取群设置（需要相关权限）
         */
        miraiGet("/groupConfig") {
            val group = it.bot.getGroupOrFail(paramOrNull("target"))
            call.respondDTO(GroupDetailDTO(group))
        }

        /**
         * 修改群设置（需要相关权限）
         */
        miraiVerify<GroupConfigDTO>("/groupConfig") { dto ->
            val group = dto.session.bot.getGroupOrFail(dto.target)
            with(dto.config) {
                name?.let { group.name = it }
                announcement?.let { group.settings.entranceAnnouncement = it }
                // confessTalk?.let { group.settings.isConfessTalkEnabled = it }
                allowMemberInvite?.let { group.settings.isAllowMemberInvite = it }
                // TODO: 待core接口实现设置可改
//                autoApprove?.let { group.autoApprove = it }
//                anonymousChat?.let { group.anonymousChat = it }
            }
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 群员信息管理（需要相关权限）
         */
        miraiGet("/memberInfo") {
            val member = it.bot.getGroupOrFail(paramOrNull("target")).getOrFail(paramOrNull("memberId"))
            call.respondDTO(MemberDetailDTO(member))
        }

        miraiVerify<MemberInfoDTO>("/memberInfo") { dto ->
            val member = dto.session.bot.getGroupOrFail(dto.target).getOrFail(dto.memberId)
            with(dto.info) {
                name?.let { member.nameCard = it }
                specialTitle?.let { member.specialTitle = it }
            }
            call.respondStateCode(StateCode.Success)
        }

    }
}


@Serializable
private data class MuteDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long = 0,
    val time: Int = 0
) : VerifyDTO()

@Serializable
private data class KickDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long,
    val msg: String = ""
) : VerifyDTO()

@Serializable
private data class QuitDTO(
    override val sessionKey: String,
    val target: Long
) : VerifyDTO()

@Serializable
private data class GroupConfigDTO(
    override val sessionKey: String,
    val target: Long,
    val config: GroupDetailDTO
) : VerifyDTO()

@Serializable
private data class GroupDetailDTO(
    val name: String? = null,
    val announcement: String? = null,
    val confessTalk: Boolean? = null,
    val allowMemberInvite: Boolean? = null,
    val autoApprove: Boolean? = null,
    val anonymousChat: Boolean? = null
) : DTO {
    constructor(group: Group) : this(
        group.name,
        group.settings.entranceAnnouncement,
        false,
        group.settings.isAllowMemberInvite,
        group.settings.isAutoApproveEnabled,
        group.settings.isAnonymousChatEnabled
    )
}

@Serializable
private data class MemberInfoDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long,
    val info: MemberDetailDTO
) : VerifyDTO()

@Serializable
private data class MemberDetailDTO(
    val name: String? = null,
    val nick: String? = null,
    val specialTitle: String? = null
) : DTO {
    constructor(member: Member) : this(member.nameCard, member.nick, member.specialTitle)
}
