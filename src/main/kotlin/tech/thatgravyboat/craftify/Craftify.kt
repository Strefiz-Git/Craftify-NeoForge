package tech.thatgravyboat.craftify

import com.mojang.blaze3d.platform.InputConstants
import gg.essential.universal.UChat
import gg.essential.universal.UKeyboard
import gg.essential.universal.UScreen
import gg.essential.vigilance.gui.SettingsGui
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadHandler
import tech.thatgravyboat.craftify.config.Config
import tech.thatgravyboat.craftify.platform.McClient
import tech.thatgravyboat.craftify.platform.McKeybind
import tech.thatgravyboat.craftify.platform.ServerboundSongPacket
import tech.thatgravyboat.craftify.services.ServiceHelper.close
import tech.thatgravyboat.craftify.services.ServiceHelper.setup
import tech.thatgravyboat.craftify.services.ServiceTypes
import tech.thatgravyboat.craftify.services.ads.AdManager
import tech.thatgravyboat.craftify.services.config.ServiceConfig
import tech.thatgravyboat.craftify.services.update.Updater
import tech.thatgravyboat.craftify.ui.Player
import tech.thatgravyboat.craftify.utils.Utils
import tech.thatgravyboat.jukebox.api.service.BaseService

@Mod("craftify")
@EventBusSubscriber(modid = "craftify", bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object Craftify {

    private val keyBindCategory = "key.categories.craftify"
    val skipForward = McKeybind("Skip Forward", keyBindCategory, InputConstants.Type.KEYSYM, UKeyboard.KEY_NONE)
    val skipPrevious = McKeybind("Skip Previous", keyBindCategory, InputConstants.Type.KEYSYM, UKeyboard.KEY_NONE)
    val togglePlaying = McKeybind("Toggle Playing", keyBindCategory, InputConstants.Type.KEYSYM, UKeyboard.KEY_NONE)
    val hidePlayer = McKeybind("Toggle Craftify HUD", keyBindCategory, InputConstants.Type.KEYSYM, UKeyboard.KEY_NONE)

    var service: BaseService? = null
        private set

    private var initalized: Boolean = false

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        AdManager.load()
        ServiceConfig.load()
    }

    @SubscribeEvent
    fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        event.register(skipForward.mapping)
        event.register(skipPrevious.mapping)
        event.register(togglePlaying.mapping)
        event.register(hidePlayer.mapping)
    }

    @SubscribeEvent
    fun onRegisterPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("craftify").versioned("1.0.0")
        registrar.playBidirectional(
            ServerboundSongPacket.TYPE, ServerboundSongPacket.CODEC,
            IPayloadHandler { _, _ -> }
        )
    }

    @EventBusSubscriber(modid = "craftify", value = [Dist.CLIENT])
    object ClientEvents {
        @SubscribeEvent
        fun onClientTick(event: ClientTickEvent.Post) {
            onTick()
        }
    }

    @EventBusSubscriber(modid = "craftify", value = [Dist.CLIENT])
    object CommandEvents {
        @SubscribeEvent
        fun onRegisterCommands(event: RegisterClientCommandsEvent) {
            Command.register(event.dispatcher)
        }
    }

    private fun onTick() {
        if (Config.firstTime && McClient.player != null) {
            Config.firstTime = false
            Config.markDirty()
            Config.writeData()

            val message = """
                §7-------[§aCraftify§7]-------
                §6This is your first time loading the mod.
                §6To setup the mod run §9/craftify§6 and go to the Login category.
                §6If you would like to support the creator you can
                §6sub to §2ThatGravyBoat§6 on §fGitHub§6, link in the
                §6config.
                §7----------------------
            """

            UChat.chat(message.trimIndent())
        }

        if (!this.initalized) {
            this.initalized = true
            reloadService()
        }

        if (Updater.hasUpdate() && McClient.player != null) Updater.showMessage()
        if (skipForward.isPressed) Utils.async { service?.move(true) }
        if (skipPrevious.isPressed) Utils.async { service?.move(false) }
        if (togglePlaying.isPressed) Utils.async { service?.setPaused(Player.state?.isPlaying == true) }
        if (hidePlayer.isPressed) Player.toggleHiding()
    }

    fun onScreenChanged(screen: Screen?) {
        if (screen == null && UScreen.currentScreen is SettingsGui) {
            Config.markDirty()

            if (Config.musicService == "disabled") {
                service?.stop()
                service?.close()
                service = null
            } else {
                reloadService()
            }
        }
    }

    fun reloadService() {
        val type = ServiceTypes.fromId(Config.musicService) ?: return

        service?.stop()
        service?.close()
        service = type.createOrReuse(service)
        service?.start()
        service?.setup()
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath("craftify", path)
}
