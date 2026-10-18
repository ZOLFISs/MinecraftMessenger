package com.minecraftmessenger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class FirebaseClient {

private static final String API_KEY =
"AIzaSyCV0kj11MhisDvVzmSrWRJJkwlv5Nv44k";

private static final String AUTH_BASE =
"https://identitytoolkit.googleapis.com/v1/accounts:";

private static final HttpClient HTTP = HttpClient.newHttpClient();

private FirebaseClient() {
}

public static AuthResult register(String email, String password)
throws IOException, InterruptedException {
return authenticate("signUp", email, password);
}

public static AuthResult login(String email, String password)
throws IOException, InterruptedException {
return authenticate("signInWithPassword", email, password);
}

private static AuthResult authenticate(
String operation,
String email,
String password
) throws IOException, InterruptedException {

JsonObject body = new JsonObject();
body.addProperty("email", email);
body.addProperty("password", password);
body.addProperty("returnSecureToken", true);

HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(
AUTH_BASE + operation + "?key=" + API_KEY
))
.header("Content-Type", "application/json")
.POST(HttpRequest.BodyPublishers.ofString(
body.toString(),
StandardCharsets.UTF_8
))
.build();

HttpResponse<String> response = HTTP.send(
request,
HttpResponse.BodyHandlers.ofString(
StandardCharsets.UTF_8
)
);

JsonObject json = JsonParser.parseString(
response.body()
).getAsJsonObject();

if (response.statusCode() < 200
|| response.statusCode() >= 300) {
String message = "Firebase authentication failed";

if (json.has("error")
&& json.getAsJsonObject("error").has("message")) {
message = json.getAsJsonObject("error")
.get("message").getAsString();
}

throw new IOException(message);
}

return new AuthResult(
json.get("localId").getAsString(),
json.get("email").getAsString(),
json.get("idToken").getAsString(),
json.get("refreshToken").getAsString()
);
}

public record AuthResult(
String userId,
String email,
String idToken,
String refreshToken
) {
}
}
