package net.modmanagermc.modmanager.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.modmanagermc.modmanager.gui.ModManagerListScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ModsScreen.class)
public class ModsScreenMixin extends Screen {

    private static final Identifier MODMANAGER_BUTTON_LOCATION = new Identifier("modmanager", "textures/gui/install_button.png");
    private static final TranslatableText MODMANAGER_BUTTON_OPTIONS = new TranslatableText("modmanager.button.open");
    @Shadow
    private int paneWidth;


    protected ModsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        int searchBoxWidth = paneWidth - 32 - 22;

        this.addDrawableChild(new ModMenuTexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2 + 22, 22, 20, 20,
                0, 0, MODMANAGER_BUTTON_LOCATION, 32, 64,
                button -> Objects.requireNonNull(client).setScreen(new ModManagerListScreen(this)), MODMANAGER_BUTTON_OPTIONS,
                (buttonWidget, matrices, mouseX, mouseY) -> {
                    ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
                    if (button.isJustHovered()) {
                        this.renderTooltip(matrices, MODMANAGER_BUTTON_OPTIONS, mouseX, mouseY);
                    } else if (button.isFocusedButNotHovered()) {
                        this.renderTooltip(matrices, MODMANAGER_BUTTON_OPTIONS, button.x, button.y);
                    }
                }));
    }

}
