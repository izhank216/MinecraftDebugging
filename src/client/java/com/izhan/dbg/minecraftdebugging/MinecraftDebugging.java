package com.izhan.dbg.minecraftdebugging;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class MinecraftDebugging implements ClientModInitializer {
public static final List<String> LOGS = new ArrayList<>();

@Override
public void onInitializeClient() {
    LOGS.add("MinecraftDebugging started");
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
        if (client.player != null) {
            LOGS.add("Tick " + System.currentTimeMillis());
            if (LOGS.size() > 300) LOGS.remove(0);
        }
    });
}

public static void open(Screen parent) {
    MinecraftClient.getInstance().setScreen(new DebugOptionsScreen(parent));
}

public static class DebugOptionsScreen extends Screen {
    private final Screen parent;
    private int scroll;

    protected DebugOptionsScreen(Screen parent) {
        super(Text.literal("Minecraft Debugging"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent))
            .dimensions(width - 110, height - 30, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear Logs"), b -> LOGS.clear())
            .dimensions(10, height - 30, 100, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll -= (int) (vertical * 10);
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, "FPS: " + client.getCurrentFps(), 10, 10, 0xffffff, true);
        context.drawText(textRenderer, "Logs:", 10, 30, 0xffffff, true);

        context.enableScissor(5, 45, width / 2 - 20, height - 40);
        int logY = 50 - scroll;
        for (String log : LOGS) {
            context.drawText(textRenderer, log, 10, logY, 0xffffff, true);
            logY += 10;
        }
        context.disableScissor();

        if (client.world != null) {
            int rightColX = width - 150;
            context.drawText(textRenderer, "Entities:", rightColX, 40, 0xffff00, true);
            
            context.enableScissor(rightColX - 5, 55, width - 5, height / 2);
            int entityY = 60;
            for (Entity e : client.world.getEntities()) {
                context.drawText(textRenderer, e.getName().getString(), rightColX, entityY, 0xffffff, true);
                entityY += 10;
            }
            context.disableScissor();

            if (client.crosshairTarget instanceof BlockHitResult hit) {
                BlockPos pos = hit.getBlockPos();
                BlockState state = client.world.getBlockState(pos);
                int blockY = height / 2 + 20;
                context.drawText(textRenderer, "Block Inspector:", rightColX, blockY, 0x00ffff, true);
                context.drawText(textRenderer, "Block: " + state.getBlock().getName().getString(), rightColX, blockY + 15, 0xffffff, true);
                context.drawText(textRenderer, "Pos: " + pos.toShortString(), rightColX, blockY + 25, 0xffffff, true);
            }
        }

        context.drawCenteredTextWithShadow(textRenderer, "Log Viewer", width / 2, 15, 0xffffff);
    }
}
}
