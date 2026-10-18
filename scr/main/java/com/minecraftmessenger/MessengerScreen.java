package com.minecraftmessenger;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MessengerScreen extends Screen {

private final Screen parent;
private final FirebaseClient.AuthResult account;

private TextFieldWidget messageInput;
private final List<String> messages = new ArrayList<>();

private String selectedFriend = "Общий чат";
private String status = "Аккаунт авторизован";

private int panelLeft;
private int panelTop;
private int panelWidth;
private int panelHeight;

public MessengerScreen(Screen parent) {
this(parent, null);
}

public MessengerScreen(
Screen parent,
FirebaseClient.AuthResult account
) {
super(Text.literal("Minecraft Messenger"));
this.parent = parent;
this.account = account;
}

@Override
protected void init() {
panelWidth = Math.min(760, width - 32);
panelHeight = Math.min(440, height - 32);
panelLeft = (width - panelWidth) / 2;
panelTop = (height - panelHeight) / 2;

int sidebarWidth = Math.min(190, panelWidth / 3);
int contentLeft = panelLeft + sidebarWidth + 16;
int contentWidth = panelWidth - sidebarWidth - 32;

addDrawableChild(ButtonWidget.builder(
Text.literal("← Назад"),
button -> close()
).dimensions(
panelLeft + 10, panelTop + 10, 78, 20
).build());

addDrawableChild(ButtonWidget.builder(
Text.literal("Общий чат"),
button -> {
selectedFriend = "Общий чат";
messages.clear();
messages.add("Добро пожаловать!");
status = "Демонстрационный чат";
}
).dimensions(
panelLeft + 10, panelTop + 70,
sidebarWidth - 20, 24
).build());

messageInput = new TextFieldWidget(
textRenderer,
contentLeft,
panelTop + panelHeight - 48,
Math.max(60, contentWidth - 76),
24,
Text.literal("Сообщение")
);

messageInput.setMaxLength(1000);
messageInput.setPlaceholder(
Text.literal("Напиши сообщение...")
);
addDrawableChild(messageInput);

addDrawableChild(ButtonWidget.builder(
Text.literal("Отправить"),
button -> sendMessage()
).dimensions(
contentLeft + Math.max(60, contentWidth - 70),
panelTop + panelHeight - 48,
70, 24
).build());

messages.clear();
messages.add("Minecraft Messenger");
messages.add(account == null
? "Гостевой режим"
: "Вы вошли как " + account.email());
messages.add("Отправка сообщений через Firebase");
messages.add("будет добавлена следующим этапом.");
}

private void sendMessage() {
if (messageInput == null) {
return;
}

String text = messageInput.getText().trim();

if (text.isEmpty()) {
return;
}

messages.add("Вы: " + text);
messageInput.setText("");
status = "Сообщение сохранено только на этом экране";
}

@Override
public void render(
DrawContext context,
int mouseX,
int mouseY,
float delta
) {
renderBackground(context, mouseX, mouseY, delta);

context.fill(
panelLeft, panelTop,
panelLeft + panelWidth,
panelTop + panelHeight,
0xF014151D
);

context.fill(
panelLeft, panelTop,
panelLeft + panelWidth,
panelTop + 3,
0xFF9B59FF
);

int sidebarWidth = Math.min(190, panelWidth / 3);

context.fill(
panelLeft, panelTop + 36,
panelLeft + sidebarWidth,
panelTop + panelHeight,
0xFF1B1C27
);

context.drawText(
textRenderer,
"MINECRAFT MESSENGER",
panelLeft + 100,
panelTop + 16,
0xFFE0C7FF,
false
);

context.drawText(
textRenderer,
"ДРУЗЬЯ",
panelLeft + 12,
panelTop + 48,
0xFFB8A6D9,
false
);

int contentLeft = panelLeft + sidebarWidth + 16;

context.drawText(
textRenderer,
selectedFriend,
contentLeft,
panelTop + 48,
0xFFFFFFFF,
false
);

context.drawText(
textRenderer,
status,
contentLeft,
panelTop + 64,
0xFFB5B5C5,
false
);

int messageY = panelTop + 92;
int bottomLimit = panelTop + panelHeight - 62;

for (String message : messages) {
if (messageY > bottomLimit) {
break;
}

context.drawText(
textRenderer,
textRenderer.trimToWidth(
message,
Math.max(
40,
panelWidth - sidebarWidth - 40
)
),
contentLeft,
messageY,
0xFFE3E3EE,
false
);

messageY += 18;
}

super.render(context, mouseX, mouseY, delta);
}

@Override
public void close() {
if (client != null) {
client.setScreen(parent);
}
}

@Override
public boolean shouldPause() {
return false;
}
}
