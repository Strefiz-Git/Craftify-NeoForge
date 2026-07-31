package tech.thatgravyboat.craftify.screens

import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.craftify.platform.McClient
import tech.thatgravyboat.craftify.platform.drawSprite
import tech.thatgravyboat.craftify.services.addons.ServerShareAddon
import kotlin.math.min

private fun rl(path: String) = ResourceLocation.fromNamespaceAndPath("olympus", path)
private val MODAL = rl("modal/modal")
private val MODAL_HEADER = rl("modal/modal_header")
private val MODAL_INSET = rl("modal/modal_inset")
private val SCROLLBAR = rl("lists/scroll/bar")
private val SCROLLBAR_THUMB = rl("lists/scroll/thumb")

private const val PADDING = 5
private const val WIDTH = 140
private const val HEIGHT = 200

class ServersScreen : Screen(CommonComponents.EMPTY) {

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(graphics, mouseX, mouseY, partialTicks)

        graphics.drawSprite(
            MODAL,
            (this.width - WIDTH) / 2 - PADDING, (this.height - HEIGHT) / 2 - PADDING,
            WIDTH + PADDING * 2, HEIGHT + PADDING * 2
        )
        graphics.drawSprite(
            MODAL_HEADER,
            (this.width - WIDTH) / 2 - PADDING, (this.height - HEIGHT) / 2 - PADDING,
            WIDTH + PADDING * 2, 20
        )
    }

    override fun init() {

        val x = (this.width - WIDTH) / 2
        val y = (this.height - HEIGHT) / 2

        Layouts.column()
            .withGap(PADDING * 2)
            .withPosition(x, y)
            .withChild(
                Layouts.row()
                    .withChild(
                        Widgets.text(Component.literal("Shared Servers"))
                            .withShadow()
                            .withColor(MinecraftColors.WHITE)
                            .withLeftAlignment()
                            .withSize(WIDTH - 24, 10)
                    )
                    .withChild(
                        Widgets.button()
                            .withTexture(UIConstants.MODAL_SAVE)
                            .withCallback {
                                ServerShareAddon.servers += McClient.self.currentServer?.ip ?: ""
                                this.rebuildWidgets()
                            }
                            .withTooltip(Component.literal("Save current server to list"))
                            .withSize(12, 12)
                    )
                    .withChild(
                        Widgets.button()
                            .withTexture(UIConstants.MODAL_CLOSE)
                            .withCallback { this.onClose() }
                            .withTooltip(UIConstants.BACK)
                            .withSize(12, 12)
                    )
            )
            .withChild(Widgets.list {
                it.withSize(WIDTH, HEIGHT - 20)
                it.withScrollableY(TriState.TRUE)
                it.withTexture(MODAL_INSET)
                it.withScrollbarYRenderer { graphics, ctx, _ ->
                    val widget = ctx.getWidget() as LayoutWidget<*>
                    val scrollHeight: Int = (ctx.height.toFloat() * (ctx.height.toFloat() / widget.contentHeight.toFloat())).toInt() + (widget.viewHeight - ctx.height)
                    val scrollY = ((widget.yScroll + widget.overscrollY).toFloat() / widget.contentHeight.toFloat() * ctx.height.toFloat()).toInt()
                    graphics.drawSprite(SCROLLBAR, ctx.x + 2, ctx.y, ctx.width - 4, ctx.height)
                    graphics.drawSprite(SCROLLBAR_THUMB, ctx.x, ctx.y + scrollY, ctx.width, min(ctx.height, scrollHeight))
                }
                it.withContentMargin(1)

                it.withContents { contents ->
                    contents.withGap(1)

                    ServerShareAddon.servers.forEach { server ->
                        contents.withChild(Widgets.frame { frame ->
                            frame.withSize(WIDTH - 12, 20)
                            frame.withTexture(UIConstants.LIST_ENTRY.enabled())
                            frame.withEqualSpacing(Orientation.HORIZONTAL)
                            frame.withContents { contents ->
                                contents.addChild(Widgets
                                    .text(Component.literal(server))
                                    .withAlignment(0.1f)
                                    .withColor(MinecraftColors.WHITE)
                                    .withSize((WIDTH * 0.6f).toInt(), 20)
                                )
                                contents.addChild(Widgets.button { btn ->
                                    btn.withTexture(null)
                                    btn.withSize(20, 20)
                                    btn                                    .withTooltip(UIConstants.DELETE)
                                    btn.withCallback {
                                        ServerShareAddon.servers -= server
                                        this.rebuildWidgets()
                                    }
                                    btn.withRenderer(WidgetRenderers.layered(
                                        WidgetRenderers.withColors(
                                            WidgetRenderers.solid(),
                                            MinecraftColors.WHITE.withAlpha(0),
                                            MinecraftColors.WHITE.withAlpha(0),
                                            MinecraftColors.DARK_GRAY.withAlpha(0xFF)
                                        ),
                                        WidgetRenderers.icon<Button>(UIIcons.TRASH)
                                            .withColor(MinecraftColors.WHITE.withAlpha(0xFF)),
                                    ).withPadding(4))
                                })
                            }
                        })
                    }
                }
            })
            .build(this::addRenderableWidget)
    }
}
