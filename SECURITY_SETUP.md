# Security Setup Guide for PartyMaker

## Overview
This guide explains the security improvements made to the PartyMaker application and how to properly configure them.

## Security Improvements Implemented

### 1. API Keys and Credentials
- ✅ Removed all hardcoded API keys from source code
- ✅ Implemented secure storage using EncryptedSharedPreferences
- ✅ API keys now loaded from local.properties or environment variables

### 2. Network Security
- ✅ Disabled cleartext HTTP traffic
- ✅ Implemented network security configuration
- ✅ Added SSL certificate pinning support

### 3. Data Protection
- ✅ Implemented EncryptedSharedPreferences for sensitive data
- ✅ Enhanced session management with expiration
- ✅ Secure storage for user credentials

### 4. Authentication Security
- ✅ Enhanced password validation requirements:
  - Minimum 8 characters
  - Must contain uppercase, lowercase, numbers, and special characters
  - Prevents common passwords
  - Checks for sequential and repeated characters
- ✅ Reduced session duration from 30 to 7 days
- ✅ Automatic session expiration

### 5. Code Protection
- ✅ Enhanced ProGuard rules for obfuscation
- ✅ Removed logging in release builds
- ✅ Hidden sensitive method names

### 6. Permission Management
- ✅ Made dangerous permissions optional
- ✅ Implemented runtime permission handling
- ✅ Graceful degradation when permissions denied

## Setup Instructions

### 1. Configure API Keys

1. Copy `local.properties.template` to `local.properties`:
   ```bash
   cp local.properties.template local.properties
   ```

2. Edit `local.properties` and add your API keys:
   ```properties
   openai.api.key=your_actual_openai_api_key
   maps.api.key=your_actual_google_maps_api_key
   ```

3. **IMPORTANT**: Never commit `local.properties` to version control!

### 2. For CI/CD Environments

Set the following environment variables:
- `OPENAI_API_KEY`: Your OpenAI API key
- `MAPS_API_KEY`: Your Google Maps API key

### 3. Google Services Configuration

1. Download your `google-services.json` from Firebase Console
2. Place it in the `app/` directory
3. This file is already in `.gitignore`

### 4. SSL Certificate Pinning

To enable SSL pinning for your server:

1. Get your server's certificate SHA-256 fingerprint
2. Update `app/src/main/res/xml/network_security_config.xml`:
   ```xml
   <pin digest="SHA-256">your_certificate_sha256_here</pin>
   ```

### 5. Build Configuration

For release builds, ProGuard is automatically enabled with security-optimized rules.

## Security Best Practices

1. **Regular Updates**: Keep all dependencies updated
2. **API Key Rotation**: Rotate API keys periodically
3. **Monitor Usage**: Monitor API usage for unusual patterns
4. **Code Reviews**: Review security-critical code changes
5. **Testing**: Test permission handling on various devices

## Verification

Run the security scan to verify all security measures are in place:

```java
SecurityAgent agent = SecurityAgent.getInstance(context);
agent.performSecurityScan().thenAccept(report -> {
    // Check security score
    Log.d("Security", "Score: " + report.getOverallScore());
});
```

## Additional Security Considerations

1. **Backend Security**: Ensure your backend APIs have proper authentication
2. **Data Validation**: Always validate data on the server side
3. **HTTPS Only**: Never allow HTTP connections in production
4. **Regular Audits**: Perform regular security audits

## Support

For security-related questions or to report vulnerabilities, please contact the security team.