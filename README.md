# Development Configuration Guide

##create `application-dev.yml`
**Location:** `src/main/resources/application-dev.yml`  
*(This file is ignored by Git - see `.gitignore`)*

### 1. Create the Config File
```bash
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml
```

### 2. Required Configurations
```yaml
# Database (MySQL)
spring.datasource:
  url: jdbc:mysql://localhost:3306/YOUR_DB_NAME?createDatabaseIfNotExist=true&serverTimezone=UTC
  username: YOUR_DB_USER
  password: YOUR_DB_PASSWORD  # Keep empty if none

# Email (Gmail Example)
spring.mail:
  username: your.email@gmail.com
  password: "your-app-password"  # Generate in Google Account > Security

# JWT Authentication
spring.security.jwt:
  secret-key: generate-with-base64  # Run: `openssl rand -base64 32`
  expiration: 86400000  # 24 hours in ms
```

## 🔐 Security Notes
- This file **MUST NOT** be committed to Git
- For production:
    - Use environment variables
    - Use proper secret management (Vault, K8s Secrets, etc.)
- Gmail users: Enable "Less secure apps" or use App Passwords



## 📂 File Structure
```
your-project/
└── src/main/resources/
    ├── application.yml           # Main config
    ├── application-dev.yml       # Local dev (IGNORED)
```
