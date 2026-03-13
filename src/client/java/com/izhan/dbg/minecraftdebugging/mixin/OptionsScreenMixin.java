package com.izhan.dbg.minecraftdebugging.mixin;

import com.izhan.dbg.minecraftdebugging.MinecraftDebugging;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void minecraftdebugging$addButton(CallbackInfo ci) {
        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Minecraft Debugging"),
                b -> MinecraftDebugging.open(this)
            ).dimensions(width / 2 - 100, height / 2 + 105, 200, 20).build()
        );
    }

}
