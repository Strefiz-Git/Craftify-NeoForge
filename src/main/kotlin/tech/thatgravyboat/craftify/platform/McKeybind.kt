package tech.thatgravyboat.craftify.platform

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

class McKeybind(
    name: String,
    category: String,
    type: InputConstants.Type,
    code: Int,
) {
    val mapping = KeyMapping(name, type, code, category)

    val isPressed: Boolean get() = mapping.isDown && !mapping.isUnbound
}
