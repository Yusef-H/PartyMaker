# API Documentation

## Overview

The PartyMaker API is a RESTful service built with Spring Boot that provides backend functionality for the PartyMaker mobile application. All database operations are performed through this API layer to ensure data consistency and security.

## Base URLs

- **Production**: `https://partymaker.onrender.com`
- **Development**: `http://localhost:8080`
- **Emulator**: `http://10.0.2.2:8080`

## Authentication

All API endpoints require Firebase Authentication. Include the Firebase ID token in the Authorization header:

```
Authorization: Bearer {firebase_id_token}
```

## API Endpoints

### 1. User Management

#### Get User Profile
```http
GET /api/users/{userId}
```

**Response:**
```json
{
  "uid": "string",
  "email": "string",
  "username": "string",
  "photoUrl": "string",
  "createdAt": "timestamp",
  "groups": ["groupId1", "groupId2"]
}
```

#### Update User Profile
```http
PUT /api/users/{userId}
Content-Type: application/json

{
  "username": "string",
  "photoUrl": "string"
}
```

#### Delete User
```http
DELETE /api/users/{userId}
```

### 2. Group Management

#### Create Group
```http
POST /api/groups
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "date": "ISO 8601 datetime",
  "location": "string",
  "price": "number",
  "isPrivate": "boolean",
  "maxMembers": "number",
  "imageUrl": "string"
}
```

**Response:**
```json
{
  "groupId": "string",
  "name": "string",
  "adminId": "string",
  "members": ["userId"],
  "createdAt": "timestamp"
}
```

#### Get Group Details
```http
GET /api/groups/{groupId}
```

#### Update Group
```http
PUT /api/groups/{groupId}
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "date": "ISO 8601 datetime",
  "location": "string",
  "price": "number"
}
```

#### Delete Group
```http
DELETE /api/groups/{groupId}
```

#### List User Groups
```http
GET /api/users/{userId}/groups
```

#### List Public Groups
```http
GET /api/groups/public
```

### 3. Group Membership

#### Join Group
```http
POST /api/groups/{groupId}/members/{userId}
```

#### Leave Group
```http
DELETE /api/groups/{groupId}/members/{userId}
```

#### Invite Member
```http
POST /api/groups/{groupId}/invitations
Content-Type: application/json

{
  "userId": "string",
  "invitedBy": "string"
}
```

#### Accept Invitation
```http
PUT /api/groups/{groupId}/invitations/{userId}/accept
```

#### Decline Invitation
```http
DELETE /api/groups/{groupId}/invitations/{userId}
```

#### List Group Members
```http
GET /api/groups/{groupId}/members
```

#### List Pending Invitations
```http
GET /api/groups/{groupId}/invitations
```

### 4. Chat Messages

#### Send Message
```http
POST /api/groups/{groupId}/messages
Content-Type: application/json

{
  "senderId": "string",
  "senderName": "string",
  "content": "string",
  "type": "text|image|file",
  "mediaUrl": "string (optional)"
}
```

#### Get Messages
```http
GET /api/groups/{groupId}/messages?limit=50&before={timestamp}
```

**Query Parameters:**
- `limit`: Number of messages to retrieve (default: 50, max: 100)
- `before`: Timestamp to get messages before (for pagination)

#### Delete Message
```http
DELETE /api/groups/{groupId}/messages/{messageId}
```

### 5. File Upload

#### Upload Image
```http
POST /api/upload/image
Content-Type: multipart/form-data

file: [binary data]
type: "profile|group|message"
```

**Response:**
```json
{
  "url": "string",
  "publicId": "string",
  "size": "number",
  "type": "string"
}
```

### 6. AI Integration

#### Chat with AI Assistant
```http
POST /api/ai/chat
Content-Type: application/json

{
  "message": "string",
  "context": "string (optional)",
  "userId": "string"
}
```

**Response:**
```json
{
  "response": "string",
  "tokens": "number",
  "model": "string"
}
```

## Error Responses

All error responses follow this format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {},
    "timestamp": "ISO 8601 datetime"
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|------------|-------------|
| `AUTH_REQUIRED` | 401 | Authentication required |
| `INVALID_TOKEN` | 401 | Invalid or expired auth token |
| `FORBIDDEN` | 403 | Access denied |
| `NOT_FOUND` | 404 | Resource not found |
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `RATE_LIMIT` | 429 | Too many requests |
| `SERVER_ERROR` | 500 | Internal server error |

## Rate Limiting

- **General endpoints**: 100 requests per minute
- **File uploads**: 10 requests per minute
- **AI endpoints**: 20 requests per minute

Rate limit headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1628851200
```

## Pagination

List endpoints support pagination using cursor-based pagination:

```http
GET /api/groups/public?cursor={lastItemId}&limit=20
```

Response includes pagination metadata:
```json
{
  "data": [...],
  "pagination": {
    "hasMore": true,
    "nextCursor": "string",
    "total": 100
  }
}
```

## Webhooks

### Event Types

- `group.created`
- `group.updated`
- `group.deleted`
- `member.joined`
- `member.left`
- `message.sent`

### Webhook Payload

```json
{
  "event": "group.created",
  "timestamp": "ISO 8601 datetime",
  "data": {
    // Event-specific data
  }
}
```

## SDK Examples

### Android (Java)
```java
FirebaseServerClient client = new FirebaseServerClient(context);
client.createGroup(groupData, new ApiCallback<Group>() {
    @Override
    public void onSuccess(Group group) {
        // Handle success
    }
    
    @Override
    public void onError(AppNetworkError error) {
        // Handle error
    }
});
```

### cURL
```bash
curl -X POST https://partymaker.onrender.com/api/groups \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Party","date":"2025-01-01T20:00:00Z"}'
```

## Testing

### Postman Collection
Download our [Postman Collection](./partymaker-api.postman_collection.json) for easy API testing.

### Test Credentials
For development/testing:
- Email: `test@partymaker.com`
- Password: `Test123!`

## API Versioning

The API uses URL versioning. Current version: `v1`

Future versions will be available at:
- `/api/v2/...`
- `/api/v3/...`

## Support

For API support, please contact:
- Email: api-support@partymaker.com
- GitHub Issues: [PartyMaker API Issues](https://github.com/partymaker/api/issues)

---

*API Version: 1.0.0 | Last Updated: August 2025*