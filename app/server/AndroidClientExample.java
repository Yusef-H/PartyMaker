package com.example.partymaker;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Example class showing how to interact with the PartyMaker server
 * instead of directly using Firebase in the Android app.
 */
public class AndroidClientExample {

    private static final String SERVER_URL = "http://your-server-url:8080";

    /**
     * Fetch data from the server
     */
    public void fetchData(String path, final DataCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SERVER_URL + "/api/firebase/" + params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        return response.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Map<String, Object> data = jsonToMap(jsonObject);
                        callback.onSuccess(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch data");
                }
            }
        }.execute(path);
    }

    /**
     * Fetch data as a list from the server
     */
    public void fetchDataAsList(String path, final ListDataCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SERVER_URL + "/api/firebase/list/" + params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        return response.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, Object> item = jsonToMap(jsonObject);
                            dataList.add(item);
                        }
                        callback.onSuccess(dataList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch data");
                }
            }
        }.execute(path);
    }

    /**
     * Save data to the server
     */
    public void saveData(String path, Map<String, Object> data, final SaveCallback callback) {
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SERVER_URL + "/api/firebase/" + params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    
                    JSONObject jsonObject = mapToJson((Map<String, Object>) params[1]);
                    String jsonString = jsonObject.toString();
                    
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = connection.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to save data");
                }
            }
        }.execute(path, data);
    }

    /**
     * Update data on the server
     */
    public void updateData(String path, Map<String, Object> updates, final SaveCallback callback) {
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SERVER_URL + "/api/firebase/" + params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    
                    JSONObject jsonObject = mapToJson((Map<String, Object>) params[1]);
                    String jsonString = jsonObject.toString();
                    
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = connection.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to update data");
                }
            }
        }.execute(path, updates);
    }

    /**
     * Delete data from the server
     */
    public void deleteData(String path, final SaveCallback callback) {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SERVER_URL + "/api/firebase/" + params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    
                    int responseCode = connection.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to delete data");
                }
            }
        }.execute(path);
    }

    // Helper methods to convert between JSON and Map
    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        if (json != JSONObject.NULL) {
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    value = jsonToMap((JSONObject) value);
                }
                map.put(key, value);
            }
        }
        return map;
    }

    private JSONObject mapToJson(Map<String, Object> map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = mapToJson((Map<String, Object>) value);
            }
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    // Callback interfaces
    public interface DataCallback {
        void onSuccess(Map<String, Object> data);
        void onError(String errorMessage);
    }

    public interface ListDataCallback {
        void onSuccess(List<Map<String, Object>> dataList);
        void onError(String errorMessage);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
} 