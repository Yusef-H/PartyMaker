# Templates Directory

Configuration templates for the PartyMaker project. This directory contains template files for sensitive configurations that should not be committed to Git.

## Directory Contents

### 1. application.properties.template
Template for Spring Boot server configuration file.

**Target location:** `app/server/src/main/resources/application.properties`

**Key settings:**
- Server port (8080)
- Firebase settings (Database URL, Storage Bucket)
- CORS configuration
- Security and JWT settings
- Logging and monitoring
- Performance and connections

### 2. firebase-service-account.json.template
Template for Firebase Admin SDK Service Account key.

**Target location:** `app/server/src/main/resources/firebase-service-account.json`

**Required content:**
- Project ID
- Private key
- Service account email
- Additional authentication details

### 3. google-services.json.template
Template for Firebase configuration file for Android application.

**Target location:** `app/google-services.json`

**Required content:**
- Project and application IDs
- API Keys
- Client IDs
- Firebase Services settings

### 4. local.properties.template
Template for local development environment settings.

**Target location:** `local.properties` (root directory)

**Required content:**
- Android SDK path
- Local environment settings
- Development resource paths

### 5. secrets.properties.template
Template for API keys and sensitive information.

**Target location:** `secrets.properties` (root directory)

**Required keys:**
- `GOOGLE_MAPS_API_KEY` - Google Maps API key
- `PLACES_API_KEY` - Google Places API key
- `OPENAI_API_KEY` - OpenAI API key for chatbot
- `FIREBASE_SERVER_KEY` - Firebase server key

## Usage Instructions

### Step 1: Copy Templates
```bash
# Copy all templates to their correct locations
cp templates/google-services.json.template app/google-services.json
cp templates/secrets.properties.template secrets.properties
cp templates/local.properties.template local.properties
cp templates/firebase-service-account.json.template app/server/src/main/resources/firebase-service-account.json
cp templates/application.properties.template app/server/src/main/resources/application.properties
```

### Step 2: Configure Firebase
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select or create a new project
3. Download `google-services.json` from project settings
4. Create a Service Account and download the JSON key

### Step 3: Obtain API Keys
1. **Google Maps & Places:**
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Enable Maps SDK and Places API
   - Create an API key

2. **OpenAI:**
   - Go to [OpenAI Platform](https://platform.openai.com)
   - Create a new API key

### Step 4: Update Files
Edit each file and replace placeholder values with your actual values:
- Replace `your_project_id` with your project ID
- Replace `your_*_api_key_here` with actual keys
- Update URLs and settings according to your environment

## Security Warnings

### Never Commit to Git
**Never commit the following files to Git:**
- `google-services.json`
- `secrets.properties`
- `local.properties`
- `firebase-service-account.json`
- `application.properties` (with real keys)

### Verify .gitignore
Ensure sensitive files are listed in `.gitignore`:
```gitignore
# Configuration files with sensitive data
google-services.json
secrets.properties
local.properties
firebase-service-account.json
application.properties
```

### Key Security
- Store keys in a secure location
- Use minimal permissions
- Rotate keys regularly
- Use environment variables in production

## Examples

### Example secrets.properties
```properties
GOOGLE_MAPS_API_KEY=AIzaSyB1234567890abcdefghijk
OPENAI_API_KEY=sk-proj-1234567890abcdefghijk
FIREBASE_SERVER_KEY=AAAAabcd1234:APA91bE...
```

### Example Firebase Database URL Configuration
```properties
firebase.database.url=https://partymaker-app-default-rtdb.firebaseio.com/
firebase.storage.bucket=partymaker-app.appspot.com
```

## Troubleshooting

### Issue: Application not connecting to Firebase
- Verify `google-services.json` is copied to the correct location
- Check that project ID matches across all files
- Ensure Firebase is properly initialized

### Issue: API keys not working
- Check keys are active in respective consoles
- Verify APIs are enabled
- Check key restrictions and permissions

### Issue: Spring Boot server errors
- Verify all settings in `application.properties` are correct
- Check Service Account JSON is valid
- Ensure port 8080 is available

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Google Maps Platform](https://developers.google.com/maps)
- [OpenAI API Reference](https://platform.openai.com/docs)
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)

## Support

If you encounter issues with configuration:
1. Refer to official documentation for each service
2. Contact the development team for assistance

---

**Note:** These templates are essential parts of the project. Keep them updated when adding new features or services.
