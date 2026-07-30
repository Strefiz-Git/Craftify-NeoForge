package tech.thatgravyboat.craftify.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.craftify.ui.Player;

@Mixin(Hud.class)
public class HudMixin {

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderNormal(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        var mc = Minecraft.getInstance();

        if (mc.screen != null) return;

        var window = mc.getWindow();
        var hasScreen = mc.screen != null;

        graphics.pose().pushPose();
        Player.INSTANCE.onRender(
                graphics,
                hasScreen ? (int) mc.mouseHandler.getScaledXPos(window) : -1,
                hasScreen ? (int) mc.mouseHandler.getScaledYPos(window) : -1,
                deltaTracker.getGameTimeDeltaPartialTick(false)
        );
        graphics.pose().popPose();
    }
}
