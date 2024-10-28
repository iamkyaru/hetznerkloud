package tech.sco.hetznerkloud.response

import kotlinx.serialization.Serializable
import tech.sco.hetznerkloud.model.Meta
import tech.sco.hetznerkloud.model.Server

@Serializable
data class ServerList(
    val meta: Meta,
    val servers: List<Server>,
)
