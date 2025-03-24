package tech.sco.hetznerkloud.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.sco.hetznerkloud.model.*

// TODO: check which values can be omitted and which ones can be sent as null
@Serializable
data class CreateServer internal constructor(
    val automount: Boolean = false,
    val datacenter: String? = null,
    val firewalls: List<Firewall> = emptyList(),
    val image: String,
    val labels: Labels? = null,
    val location: String? = null,
    val name: String,
    val networks: List<Int> = emptyList(),
    @SerialName("placement_group")
    val placementGroup: PlacementGroup.Id? = null,
    @SerialName("public_net")
    val publicNetwork: PublicNetwork,
    @SerialName("server_type")
    val serverType: String,
    @SerialName("ssh_keys")
    val sshKeys: List<String>,
    @SerialName("start_after_create")
    val startAfterCreate: Boolean = true,
    @SerialName("user_data")
    val userData: String,
    val volumes: List<Int> = emptyList(),
) : HttpBody {
    constructor(
        automount: Boolean = false,
        firewalls: List<Firewall> = emptyList(),
        image: String,
        labels: Labels? = null,
        name: String,
        networks: List<Int> = emptyList(),
        serverPosition: Position,
        placementGroup: PlacementGroup.Id? = null,
        publicNetwork: PublicNetwork,
        serverType: String,
        sshKeys: List<String>,
        startAfterCreate: Boolean = true,
        userData: String,
        volumes: List<Int> = emptyList(),
    ) : this(
        automount,
        datacenter = serverPosition.takeValueIf<Position.Datacenter>(),
        firewalls,
        image,
        labels,
        location = serverPosition.takeValueIf<Position.Location>(),
        name,
        networks,
        placementGroup,
        publicNetwork,
        serverType,
        sshKeys,
        startAfterCreate,
        userData,
        volumes
    )

    @Serializable
    data class Firewall(
        @SerialName("firewall")
        val id: Long,
    )

    sealed class Position(val value: String) {
        class Location(value: String) : Position(value)
        class Datacenter(value: String) : Position(value)

        internal inline fun <reified T> takeValueIf(): String? {
            return this.value.takeIf { this is T }
        }
    }

    @Serializable
    data class PublicNetwork(
        @SerialName("enable_ipv4")
        val enableIpv4: Boolean = true,
        @SerialName("enable_ipv6")
        val enableIpv6: Boolean = true,
        val ipv4: String? = null,
        val ipv6: String? = null,
    )
}

@Serializable
data class AddToPlacementGroup(
    @SerialName("placement_group")
    val id: PlacementGroup.Id,
) : HttpBody

@Serializable
data class AttachIsoByName(val iso: String) : HttpBody

@Serializable
data class AttachIsoById(val iso: Iso.Id) : HttpBody

@Serializable
data class RebuildFromImageByName(val image: String) : HttpBody

@Serializable
data class RebuildFromImageById(val image: Image.Id) : HttpBody

@Serializable
data class EnableRescueMode(
    @SerialName("ssh_keys")
    val sshKeys: List<SSHKey.Id>,
    val type: Type = Type.LINUX64,
) : HttpBody {

    @Serializable
    enum class Type {
        @SerialName("linux64")
        LINUX64,
    }
}

@Serializable
data class CreateImageFromServer(val description: String, val labels: Labels? = null, val type: Type) : HttpBody {

    @Serializable
    enum class Type {
        @SerialName("snapshot")
        SNAPSHOT,

        @SerialName("backup")
        BACKUP,
    }
}

@Serializable
data class ChangeServerType(
    @SerialName("server_type")
    val serverType: String,
    @SerialName("upgrade_disk")
    val upgradeDisk: Boolean,
) : HttpBody

@Serializable
data class ChangeServerProtections(val delete: Boolean, val rebuild: Boolean) : HttpBody

@Serializable
data class ChangeAliasIps(
    @SerialName("alias_ips")
    val aliasIps: List<String>,
    val network: Network.Id,
) : HttpBody
