# PartyMaker Server

A minimal Spring Boot server that acts as a middleware between the PartyMaker app and Firebase.

## Setup

1. Generate a Firebase Admin SDK service account key:
    - Go to the Firebase Console: https://console.firebase.google.com/
    - Select your project: `partymaker-9c966`
    - Go to Project Settings > Service accounts
    - Click "Generate new private key"
    - Save the JSON file
    - Copy the contents to `src/main/resources/firebase-service-account.json`

## Running the server

```bash
./gradlew bootRun
```

The server will start on port 8080.

## API Endpoints

### Get Data

```
GET /api/firebase/{path}
```

### Get Data as List

```
GET /api/firebase/list/{path}
```

### Save Data

```
POST /api/firebase/{path}
Body: JSON data
```

### Update Data

```
PUT /api/firebase/{path}
Body: JSON updates
```

### Delete Data

```
DELETE /api/firebase/{path}
```

## Mobile App Integration

Update your mobile app to use these API endpoints instead of directly accessing Firebase. For
example:

```java
// Instead of directly accessing Firebase
DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path");

// Use the server API
String serverUrl = "http://your-server-url:8080";
// For GET requests
URL url = new URL(serverUrl + "/api/firebase/path");
// For POST requests
URL url = new URL(serverUrl + "/api/firebase/path");
// Then use HttpURLConnection or your preferred HTTP client
``` 