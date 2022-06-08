package net.modmanagermc.modmanager.mixin;

import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remove modget's button from ModsScreen
 */
@Mixin(ModMenuTexturedButtonWidget.class)
public abstract class ModMenuTexturedButtonWidgetMixin extends ButtonWidget {

    @Shadow
    @Final
    private Identifier texture;

    public ModMenuTexturedButtonWidgetMixin(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (texture.equals(new Identifier("modget", "textures/gui/install_button.png"))) {
            ci.cancel();
        }
    }

    @Override
    public void onPress() {
        if (texture.equals(new Identifier("modget", "textures/gui/install_button.png"))) {
            return;
        }
        super.onPress();
    }
}
