package com.minecraftmessenger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MinecraftMessengerClient implements ClientModInitializer {

    private static KeyBinding openMessengerKey;

    @Override
    public void onInitializeClient() {
        openMessengerKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.minecraftmessenger.open",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        KeyBinding.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMessengerKey.wasPressed()) {
                openMessenger(client);
            }
        });
    }

    private static void openMessenger(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }

        Screen currentScreen = client.currentScreen;

        if (currentScreen instanceof MessengerScreen) {
            client.setScreen(null);
            return;
        }

        client.setScreen(new MessengerScreen(currentScreen));
    }
}
