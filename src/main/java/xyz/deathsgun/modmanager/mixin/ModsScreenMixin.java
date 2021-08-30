package xyz.deathsgun.modmanager.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.deathsgun.modmanager.gui.ModsOverviewScreen;

@Mixin(ModsScreen.class)
public class ModsScreenMixin extends Screen {

    private static final Identifier MODMANAGER_BUTTON_LOCATION = new Identifier("modmanager", "textures/gui/install_button.png");
    @Shadow
    private int paneWidth;

    protected ModsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        int searchBoxWidth = this.paneWidth - 32 - 22;
        this.addDrawableChild(new ModMenuTexturedButtonWidget(this.paneWidth / 2 + searchBoxWidth / 2 + 14,
                22, 20, 20, 0, 0, MODMANAGER_BUTTON_LOCATION, 32, 64, button -> {
            MinecraftClient.getInstance().setScreen(new ModsOverviewScreen(this));
        }, new TranslatableText("modmanager.button.open")));
    }
}
