package com.minecraftmessenger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class FirestoreClient {

private static final String PROJECT_ID =
"minecraftmessenger-a9f97";

private static final String BASE_URL =
"https://firestore.googleapis.com/v1/projects/"
+ PROJECT_ID
+ "/databases/(default)/documents";

private static final HttpClient HTTP = HttpClient.newHttpClient();

private FirestoreClient() {
}

public static String createUserProfile(
FirebaseClient.AuthResult account,
String username
) throws IOException, InterruptedException {

JsonObject fields = new JsonObject();

JsonObject emailField = new JsonObject();
emailField.addProperty("stringValue", account.email());
fields.add("email", emailField);

JsonObject usernameField = new JsonObject();
usernameField.addProperty("stringValue", username);
fields.add("username", usernameField);

JsonObject createdAtField = new JsonObject();
createdAtField.addProperty(
"timestampValue",
java.time.Instant.now().toString()
);
fields.add("createdAt", createdAtField);

JsonObject document = new JsonObject();
document.add("fields", fields);

String url = BASE_URL + "/users/" + account.userId();

HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(url))
.header("Authorization", "Bearer " + account.idToken())
.header("Content-Type", "application/json")
.PATCH(HttpRequest.BodyPublishers.ofString(
document.toString(),
StandardCharsets.UTF_8
))
.build();

HttpResponse<String> response = HTTP.send(
request,
HttpResponse.BodyHandlers.ofString(
StandardCharsets.UTF_8
)
);

if (response.statusCode() < 200
|| response.statusCode() >= 300) {
throw new IOException(
"Не удалось сохранить профиль. HTTP "
+ response.statusCode()
+ ": "
+ response.body()
);
}

return response.body();
}

public static String getUserProfile(
FirebaseClient.AuthResult account
) throws IOException, InterruptedException {

HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(
BASE_URL + "/users/" + account.userId()
))
.header("Authorization", "Bearer " + account.idToken())
.GET()
.build();

HttpResponse<String> response = HTTP.send(
request,
HttpResponse.BodyHandlers.ofString(
StandardCharsets.UTF_8
)
);

if (response.statusCode() == 404) {
return null;
}

if (response.statusCode() < 200
|| response.statusCode() >= 300) {
throw new IOException(
"Не удалось получить профиль. HTTP "
+ response.statusCode()
+ ": "
+ response.body()
);
}

return response.body();
}
}
