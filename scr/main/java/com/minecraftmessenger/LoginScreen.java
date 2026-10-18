package com.minecraftmessenger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class LoginScreen extends Screen {

private final Screen parent;

private TextFieldWidget emailField;
private TextFieldWidget passwordField;
private TextFieldWidget usernameField;

private String status = "Войди или создай аккаунт";
private boolean busy = false;
private boolean registering = false;

public LoginScreen(Screen parent) {
super(Text.literal("Minecraft Messenger — Account"));
this.parent = parent;
}

@Override
protected void init() {
int fieldWidth = Math.min(300, width - 40);
int left = (width - fieldWidth) / 2;
int top = height / 2 - 105;

emailField = new TextFieldWidget(
textRenderer, left, top, fieldWidth, 22,
Text.literal("Email")
);
emailField.setPlaceholder(Text.literal("Электронная почта"));
emailField.setMaxLength(254);
addDrawableChild(emailField);

passwordField = new TextFieldWidget(
textRenderer, left, top + 32, fieldWidth, 22,
Text.literal("Password")
);
passwordField.setPlaceholder(Text.literal("Пароль"));
passwordField.setMaxLength(128);
passwordField.setRenderTextProvider(
(text, firstCharacterIndex) ->
net.minecraft.text.OrderedText.styledForwardsVisitedString(
"*".repeat(text.length()),
net.minecraft.text.Style.EMPTY
)
);
addDrawableChild(passwordField);

usernameField = new TextFieldWidget(
textRenderer, left, top + 64, fieldWidth, 22,
Text.literal("Username")
);
usernameField.setPlaceholder(Text.literal("Имя пользователя"));
usernameField.setMaxLength(24);
addDrawableChild(usernameField);

addDrawableChild(ButtonWidget.builder(
Text.literal("Войти"),
button -> authenticate(false)
).dimensions(left, top + 100, (fieldWidth - 8) / 2, 24).build());

addDrawableChild(ButtonWidget.builder(
Text.literal("Регистрация"),
button -> authenticate(true)
).dimensions(
left + (fieldWidth + 8) / 2,
top + 100,
(fieldWidth - 8) / 2,
24
).build());

addDrawableChild(ButtonWidget.builder(
Text.literal("Назад"),
button -> close()
).dimensions(left, top + 132, fieldWidth, 22).build());
}

private void authenticate(boolean createAccount) {
if (busy) {
return;
}

String email = emailField.getText().trim();
String password = passwordField.getText();
String username = usernameField.getText().trim();

if (email.isEmpty() || password.isEmpty()) {
status = "Введи email и пароль";
return;
}

if (createAccount && username.isBlank()) {
status = "Для регистрации укажи имя";
return;
}

if (createAccount && username.length() < 3) {
status = "Имя должно содержать минимум 3 символа";
return;
}

busy = true;
registering = createAccount;
status = createAccount
? "Создание аккаунта..."
: "Выполняется вход...";

CompletableFuture.supplyAsync(() -> {
try {
FirebaseClient.AuthResult account = createAccount
? FirebaseClient.register(email, password)
: FirebaseClient.login(email, password);

if (createAccount) {
FirestoreClient.createUserProfile(account, username);
}

return new Result(account, null);
} catch (Exception exception) {
return new Result(null, exception.getMessage());
}
}).thenAccept(result -> MinecraftClient.getInstance().execute(() -> {
busy = false;

if (result.error() != null) {
status = readableError(result.error());
return;
}

status = "Вход выполнен!";
MinecraftClient.getInstance().setScreen(
new MessengerScreen(parent, result.account())
);
}));
}

private String readableError(String error) {
if (error == null) {
return "Неизвестная ошибка";
}

if (error.contains("EMAIL_EXISTS")) {
return "Этот email уже зарегистрирован";
}
if (error.contains("INVALID_LOGIN_CREDENTIALS")
|| error.contains("INVALID_PASSWORD")
|| error.contains("EMAIL_NOT_FOUND")) {
return "Неверный email или пароль";
}
if (error.contains("WEAK_PASSWORD")) {
return "Пароль слишком слабый";
}
if (error.contains("OPERATION_NOT_ALLOWED")) {
return "Включи Email/Password в Firebase";
}
if (error.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
return "Слишком много попыток. Попробуй позже";
}
if (error.contains("403") || error.contains("PERMISSION_DENIED")) {
return "Firestore запрещает доступ. Проверь правила";
}

return "Ошибка: " + error;
}

@Override
public void render(
DrawContext context,
int mouseX,
int mouseY,
float delta
) {
renderBackground(context, mouseX, mouseY, delta);

int fieldWidth = Math.min(300, width - 40);
int left = (width - fieldWidth) / 2;
int top = height / 2 - 145;

context.fill(
left - 16, top,
left + fieldWidth + 16, top + 205,
0xF0161721
);

context.fill(
left - 16, top,
left + fieldWidth + 16, top + 3,
0xFF9B59FF
);

context.drawCenteredTextWithShadow(
textRenderer,
"MINECRAFT MESSENGER",
width / 2,
top + 14,
0xFFE0C7FF
);

context.drawCenteredTextWithShadow(
textRenderer,
registering ? "Создание аккаунта" : "Вход в аккаунт",
width / 2,
top + 34,
0xFFFFFFFF
);

context.drawCenteredTextWithShadow(
textRenderer,
status,
width / 2,
top + 180,
0xFFBFB4D8
);

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

private record Result(
FirebaseClient.AuthResult account,
String error
) {
}
}
