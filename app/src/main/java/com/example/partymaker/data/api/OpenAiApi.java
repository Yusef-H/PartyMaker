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
  
  // API Constants
  private static final String API_URL = "https://api.openai.com/v1/chat/completions";
  private static final String GPT_MODEL = "gpt-3.5-turbo";
  private static final String CONTENT_TYPE = "application/json";
  private static final String CONTENT_TYPE_CHARSET = "application/json; charset=utf-8";
  
  // JSON Field Names
  private static final String FIELD_ROLE = "role";
  private static final String FIELD_CONTENT = "content";
  private static final String FIELD_MODEL = "model";
  private static final String FIELD_MESSAGES = "messages";
  private static final String FIELD_CHOICES = "choices";
  private static final String FIELD_MESSAGE = "message";
  
  // Role Constants
  private static final String ROLE_USER = "user";
  
  // Header Constants
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String BEARER_PREFIX = "Bearer ";

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
    if (userMessage == null || userMessage.trim().isEmpty()) {
      throw new IllegalArgumentException("User message cannot be null or empty");
    }
    
    JSONObject message = createUserMessage(userMessage);
    JSONArray messages = new JSONArray();
    messages.put(message);
    
    return sendMessagesToApi(messages);
  }
  
  /**
   * Creates a user message JSON object
   * 
   * @param userMessage The user's message
   * @return JSON object representing the user message
   * @throws Exception if JSON creation fails
   */
  private JSONObject createUserMessage(String userMessage) throws Exception {
    JSONObject message = new JSONObject();
    message.put(FIELD_ROLE, ROLE_USER);
    message.put(FIELD_CONTENT, userMessage);
    return message;
  }

  /**
   * Sends a list of chat messages (history) to the OpenAI API and returns the assistant's reply.
   *
   * @param history the list of chat messages
   * @return the assistant's reply
   * @throws Exception if the request fails
   */
  public String sendMessageWithHistory(List<ChatMessageGpt> history) throws Exception {
    if (history == null || history.isEmpty()) {
      throw new IllegalArgumentException("Message history cannot be null or empty");
    }
    
    JSONArray messages = convertHistoryToJsonArray(history);
    return sendMessagesToApi(messages);
  }
  
  /**
   * Converts chat message history to JSON array
   * 
   * @param history List of chat messages
   * @return JSON array of messages
   * @throws Exception if JSON creation fails
   */
  private JSONArray convertHistoryToJsonArray(List<ChatMessageGpt> history) throws Exception {
    JSONArray messages = new JSONArray();
    for (ChatMessageGpt msg : history) {
      if (msg != null && msg.role != null && msg.content != null) {
        JSONObject message = new JSONObject();
        message.put(FIELD_ROLE, msg.role);
        message.put(FIELD_CONTENT, msg.content);
        messages.put(message);
      }
    }
    return messages;
  }
  
  /**
   * Sends messages to OpenAI API and returns response
   * 
   * @param messages JSON array of messages
   * @return API response content
   * @throws Exception if request fails
   */
  private String sendMessagesToApi(JSONArray messages) throws Exception {
    JSONObject body = createRequestBody(messages);
    Request request = buildApiRequest(body);
    
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new Exception("API request failed with code: " + response.code());
      }
      
      return parseApiResponse(response);
    }
  }
  
  /**
   * Creates request body for API call
   * 
   * @param messages JSON array of messages
   * @return Request body JSON object
   * @throws Exception if JSON creation fails
   */
  private JSONObject createRequestBody(JSONArray messages) throws Exception {
    JSONObject body = new JSONObject();
    body.put(FIELD_MODEL, GPT_MODEL);
    body.put(FIELD_MESSAGES, messages);
    return body;
  }
  
  /**
   * Builds HTTP request for API call
   * 
   * @param body Request body
   * @return HTTP request
   */
  private Request buildApiRequest(JSONObject body) {
    RequestBody requestBody = RequestBody.create(
        MediaType.parse(CONTENT_TYPE_CHARSET), body.toString());
    
    return new Request.Builder()
        .url(API_URL)
        .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + apiKey)
        .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
        .post(requestBody)
        .build();
  }
  
  /**
   * Parses API response to extract content
   * 
   * @param response HTTP response
   * @return Response content
   * @throws Exception if parsing fails
   */
  private String parseApiResponse(Response response) throws Exception {
    String responseBody = response.body().string();
    JSONObject json = new JSONObject(responseBody);
    return json.getJSONArray(FIELD_CHOICES)
        .getJSONObject(0)
        .getJSONObject(FIELD_MESSAGE)
        .getString(FIELD_CONTENT);
  }
}
