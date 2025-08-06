package com.example.partymaker.data.api;

import com.example.partymaker.data.model.ChatMessageGpt;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API client for interacting with OpenAI's GPT chat completion endpoint. Supports sending single
 * messages and message history.
 */
public class OpenAiApi {
  /** The base URL for the OpenAI API. */
  private static final String API_URL = "https://api.openai.com/v1/chat/completions";

  /** The API key for authentication. */
  private final String apiKey;

  /** OkHttp client for HTTP requests. */
  private final OkHttpClient client = new OkHttpClient();

  /**
   * Constructs the API client with the given API key.
   *
   * @param apiKey the OpenAI API key
   */
  public OpenAiApi(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Sends a single user message to the OpenAI API and returns the assistant's reply.
   *
   * @param userMessage the user's message
   * @return the assistant's reply
   * @throws Exception if the request fails
   */
  public String sendMessage(String userMessage) throws Exception {
    JSONObject message = new JSONObject();
    message.put("role", "user");
    message.put("content", userMessage);

    JSONArray messages = new JSONArray();
    messages.put(message);

    JSONObject body = new JSONObject();
    body.put("model", "gpt-3.5-turbo");
    body.put("messages", messages);

    RequestBody requestBody =
        RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body.toString());

    Request request =
        new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build();

    try (Response response = client.newCall(request).execute()) {
        String responseBody = response.body().string();
      JSONObject json = new JSONObject(responseBody);
      return json.getJSONArray("choices")
          .getJSONObject(0)
          .getJSONObject("message")
          .getString("content");
    }
  }

  /**
   * Sends a list of chat messages (history) to the OpenAI API and returns the assistant's reply.
   *
   * @param history the list of chat messages
   * @return the assistant's reply
   * @throws Exception if the request fails
   */
  public String sendMessageWithHistory(List<ChatMessageGpt> history) throws Exception {
    JSONArray messages = new JSONArray();
    for (ChatMessageGpt msg : history) {
      JSONObject message = new JSONObject();
      message.put("role", msg.role);
      message.put("content", msg.content);
      messages.put(message);
    }
    JSONObject body = new JSONObject();
    body.put("model", "gpt-3.5-turbo");
    body.put("messages", messages);

    RequestBody requestBody =
        RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body.toString());

    Request request =
        new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build();

    try (Response response = client.newCall(request).execute()) {
        String responseBody = response.body().string();
      JSONObject json = new JSONObject(responseBody);
      return json.getJSONArray("choices")
          .getJSONObject(0)
          .getJSONObject("message")
          .getString("content");
    }
  }
}
