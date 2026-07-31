package tech.thatgravyboat.craftify.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.craftify.ui.Player;

@Mixin(Gui.class)
public class HudMixin {

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderNormal(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        var mc = Minecraft.getInstance();

        if (mc.screen != null) return;

        graphics.pose().pushPose();
        Player.INSTANCE.onRender(
                graphics,
                -1,
                -1,
                deltaTracker.getGameTimeDeltaPartialTick(false)
        );
        graphics.pose().popPose();
    }
}
