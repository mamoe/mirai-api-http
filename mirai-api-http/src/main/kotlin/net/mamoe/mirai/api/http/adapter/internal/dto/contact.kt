package net.mamoe.mirai.api.http.adapter.internal.dto

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.*

@Serializable
abstract class ContactDTO : DTO {
    abstract val id: Long
}

@Serializable
data class QQDTO(
    override val id: Long,
    val nickname: String,
    val remark: String
) : ContactDTO() {
    constructor(qq: Friend) : this(qq.id, qq.nick, qq.remark)
    constructor(qq: Stranger) : this(qq.id, qq.nick, qq.remark)
}


@Serializable
data class MemberDTO(
    override val id: Long,
    val memberName: String,
    val permission: MemberPermission,
    val group: GroupDTO
) : ContactDTO() {
    constructor(member: Member) : this(
        member.id, member.nameCardOrNick, member.permission,
        GroupDTO(member.group)
    )
}

@Serializable
data class GroupDTO(
    override val id: Long,
    val name: String,
    val permission: MemberPermission
) : ContactDTO() {
    constructor(group: Group) : this(group.id, group.name, group.botPermission)
}