package tech.thatgravyboat.craftify.platform

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

inline fun GuiGraphics.pushPop(runnable: () -> Unit) {
    this.pose().pushPose()
    runnable()
    this.pose().popPose()
}

fun GuiGraphics.scale(x: Number, y: Number) {
    this.pose().scale(x.toFloat(), y.toFloat(), 1.0f)
}

fun GuiGraphics.translate(x: Number, y: Number) {
    this.pose().translate(x.toDouble(), y.toDouble(), 0.0)
}

fun GuiGraphics.drawTexture(
    texture: ResourceLocation, x: Int, y: Int, width: Int, height: Int,
    u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f,
    color: Int = -1
) {
    this.blit(texture, x, y, width, height, u0, v0, width, height, width, height)
}

fun GuiGraphics.drawSprite(
    texture: ResourceLocation, x: Int, y: Int, width: Int, height: Int,
) {
    this.blitSprite(texture, x, y, width, height)
}

fun GuiGraphics.drawString(
    text: Component, x: Int, y: Int, color: Int = -1, dropShadow: Boolean = false
) {
    this.drawString(McClient.font, text, x, y, color, dropShadow)
}
