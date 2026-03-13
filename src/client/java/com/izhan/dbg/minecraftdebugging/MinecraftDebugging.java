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
                .dimensions(width / 2 + 5, height - 25, 100, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> LOGS.clear())
                .dimensions(width / 2 - 105, height - 25, 100, 20).build());
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

            int leftColX = 10;
            int rightColX = width - 150;

            context.drawCenteredTextWithShadow(textRenderer, "Log Viewer", width / 2, 10, 0xffffff);
            context.drawText(textRenderer, "FPS: " + client.getCurrentFps(), leftColX, 10, 0xffffff, true);
            
            context.drawText(textRenderer, "Logs:", leftColX, 30, 0xffffff, true);
            context.enableScissor(0, 40, width / 2 - 10, height - 30);
            int logY = 45 - scroll;
            for (String log : LOGS) {
                context.drawText(textRenderer, log, leftColX, logY, 0xffffff, true);
                logY += 10;
            }
            context.disableScissor();

            if (client.world != null) {
                context.drawText(textRenderer, "Entities:", rightColX, 30, 0xffff00, true);
                context.enableScissor(rightColX - 5, 40, width, height / 2);
                int entityY = 45;
                for (Entity e : client.world.getEntities()) {
                    context.drawText(textRenderer, e.getName().getString(), rightColX, entityY, 0xffffff, true);
                    entityY += 10;
                }
                context.disableScissor();

                if (client.crosshairTarget instanceof BlockHitResult hit) {
                    BlockPos pos = hit.getBlockPos();
                    BlockState state = client.world.getBlockState(pos);
                    int blockY = height / 2 + 10;
                    context.drawText(textRenderer, "Inspector:", rightColX, blockY, 0x00ffff, true);
                    context.drawText(textRenderer, state.getBlock().getName().getString(), rightColX, blockY + 12, 0xffffff, true);
                    context.drawText(textRenderer, pos.toShortString(), rightColX, blockY + 22, 0xaaaaaa, true);
                }
            }
        }
    }
}
