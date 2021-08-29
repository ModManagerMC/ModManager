package xyz.deathsgun.modmanager.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.Version;

import java.util.Map;
import java.util.Objects;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    private boolean hasRun = false;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (hasRun) {
            return;
        }
        hasRun = true;
        Map<String, Version> updates = ModManager.modManager.getUpdate().getUpdates();
        if (updates.isEmpty()) {
            return;
        }
        Objects.requireNonNull(client).getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT,
                new TranslatableText("modmanager.toast.update.title"),
                new TranslatableText("modmanager.toast.update.description", updates.size())));
    }

}
