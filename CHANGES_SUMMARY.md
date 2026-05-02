# ✅ 403 Forbidden - Emergency Error Handling Implementation

## Summary of Changes

Your backend has been enhanced with comprehensive error handling to diagnose and troubleshoot 403 Forbidden errors. Here's what was added:

---

## 📦 New Files Created

### 1. **JwtAccessDeniedHandler.java**
   - **Location:** `src/main/java/com/tola/sentinelvault/identity/infrastructure/security/`
   - **Purpose:** Handles all 403 Forbidden errors
   - **Features:**
     - Logs user info, request path, and reason
     - Returns structured JSON error response
     - Includes timestamp for correlation

### 2. **JwtAuthenticationEntryPoint.java**
   - **Location:** `src/main/java/com/tola/sentinelvault/identity/infrastructure/security/`
   - **Purpose:** Handles all 401 Unauthorized errors
   - **Features:**
     - Logs authentication failures
     - Returns structured JSON error response
     - Prevents default Spring Security redirect

### 3. **GlobalExceptionHandler.java**
   - **Location:** `src/main/java/com/tola/sentinelvault/shared/infrastructure/rest/`
   - **Purpose:** Catches all exception types and converts to HTTP responses
   - **Handles:**
     - Domain exceptions → 409 Conflict
     - Validation errors → 400 Bad Request
     - Access denied → 403 Forbidden
     - General exceptions → 500 Internal Server Error

### 4. **DEBUG_403_GUIDE.md**
   - **Location:** `sentinelvault/` (root)
   - **Purpose:** Complete debugging guide with examples
   - **Contains:**
     - 403 causes & solutions
     - Test scripts
     - Log examples
     - Response format examples

---

## 🔧 Modified Files

### 1. **SecurityConfig.java**
   **Changes:**
   - Added imports for new handlers
   - Injected `JwtAuthenticationEntryPoint` and `JwtAccessDeniedHandler`
   - Added exception handling configuration:
     ```java
     .exceptionHandling(ex -> ex
         .authenticationEntryPoint(authenticationEntryPoint)
         .accessDeniedHandler(accessDeniedHandler)
     )
     ```
   - Additional comment about 403 handling

### 2. **JwtFilter.java**
   **Changes:**
   - Enhanced logging at each step
   - Added null-check for role claim (prevents NPE)
   - Added email extraction from token
   - Wrapped token parsing in try-catch
   - More descriptive debug messages

### 3. **application.yml**
   **Changes:**
   - Updated logging configuration to show security events
   - Corrected package names for logging levels
   - Added specific DEBUG level for access decisions

---

## 🚀 How It Works

### Error Response Flow

```
HTTP Request
    ↓
JwtFilter (validates token)
    ↓
    ├─ No token/invalid? → JwtAuthenticationEntryPoint (401)
    └─ Valid token? → Continue
        ↓
    SecurityContext (authentication set)
        ↓
    Endpoint authorization check
        ↓
        ├─ User lacks role? → JwtAccessDeniedHandler (403)
        └─ User has access? → Handle request
            ↓
        Try-catch block
            ↓
            ├─ DomainException? → GlobalExceptionHandler (409)
            ├─ ValidationException? → GlobalExceptionHandler (400)
            └─ Unexpected error? → GlobalExceptionHandler (500)
```

---

## 📝 Example: Adding Role-Based Protection

To add role-based access control to an endpoint:

```java
@RestController
@RequestMapping("/api/secrets")
@RequiredArgsConstructor
public class SecretController {
    
    private final SecretUseCase secretUseCase;
    
    // Only ADMIN and higher can create secrets
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SecretResponse> createSecret(
            @Valid @RequestBody CreateSecretRequest request) {
        return ResponseEntity.ok(secretUseCase.execute(request));
    }
    
    // Members can only read their own secrets
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping
    public ResponseEntity<List<SecretResponse>> listSecrets() {
        return ResponseEntity.ok(secretUseCase.list());
    }
    
    // VIEWER role can only read
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'VIEWER')")
    @GetMapping("/{secretId}")
    public ResponseEntity<SecretResponse> getSecret(
            @PathVariable UUID secretId) {
        return ResponseEntity.ok(secretUseCase.getById(secretId));
    }
}
```

---

## 🔍 Quick Troubleshooting

| Symptom | Cause | Solution |
|---------|-------|----------|
| Always getting 403 | Missing role in JWT | Check LoginUseCase.java - verify `.claim("role", user.getRole().name())` |
| 401 instead of 403 | No token provided | Add Authorization header: `Bearer <token>` |
| Specific endpoint 403 | @PreAuthorize mismatch | Verify role in @PreAuthorize matches user's actual role |
| Login fails | Invalid password/email | Check password requirements in PasswordPolicyService |
| Token expires quickly | Expiration too low | Increase `jwt.expiration-ms` in application.yml |

---

## 🧪 Testing the Changes

### 1. Start the application
```bash
./mvnw spring-boot:run
```

### 2. Register a user (if needed)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "role": "MEMBER"
  }'
```

### 3. Login to get token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 4. Save the token and test an endpoint
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# This will work (authenticated)
curl -X GET http://localhost:8080/api/secrets \
  -H "Authorization: Bearer $TOKEN"

# This will return 401 (no token)
curl -X GET http://localhost:8080/api/secrets

# This will return 403 if endpoint requires ADMIN role
curl -X GET http://localhost:8080/api/admin/settings \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Log Examples

Check your application logs for:

**Successful authentication:**
```
DEBUG [main] JwtFilter - Authenticated user abc123def456 with role MEMBER for request: /api/secrets
```

**403 Forbidden:**
```
WARN  [main] JwtAccessDeniedHandler - 403 Forbidden - Access denied for request /api/admin/users by user: abc123def456. Reason: Access Denied
```

**401 Unauthorized:**
```
WARN  [main] JwtAuthenticationEntryPoint - 401 Unauthorized - Authentication failed for request /api/secrets: Unauthorized
```

**Missing role in token:**
```
WARN  [main] JwtFilter - JWT token missing 'role' claim for user: abc123def456 (test@example.com)
```

---

## ⚠️ Important Notes

1. **JWT Secret:** Keep `jwt.secret` consistent across all deployments
2. **Role Matching:** Use exact role names from `Role.java` enum
3. **Logging Levels:** Use DEBUG/TRACE in development, INFO in production
4. **Security Headers:** Consider adding CORS and HSTS headers for production
5. **Token Expiration:** Adjust `jwt.expiration-ms` based on your security needs

---

## 📖 Full Documentation

See **DEBUG_403_GUIDE.md** for complete debugging instructions with test scripts.

---

## ✨ What's Next?

1. Implement more endpoints with appropriate role checks
2. Add integration tests for permission scenarios
3. Set up monitoring/alerting for 403 errors in production
4. Consider implementing rate limiting
5. Add audit logging for security events

---

**All changes are backward compatible and non-breaking!** ✅

