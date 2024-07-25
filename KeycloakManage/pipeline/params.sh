# Required params

# Jenkins Static Parameters
KEYCLOAK_URL [server-url]
REALM_ID [main-realm]
ADMIN_USERNAME [power-user]
ADMIN_PASSWORD [power-user]

CLIENT_ID [admin-cli]
CLIENT_SECRET

# Manage User
USER_NAME [user-name]
USER_PASSWORD [user-password]
USER_EMAIL [user-email]

USER_NEW_PASSWORD [new-password] [Change Password]

# Manage Group
GROUP_NAME [group-name] -> ประกอบด้วย
- SERVICE_NAME [service-name]
- TEAM_NAME / COMPANY_NAME [team-name]
- PROJECT_NAME [project-name]
- USER_ROLE [role-name] -> จะต้องไป MAP กับ Role ใน Keycloak ต่่อ

# Manage Role
ROLE_NAME [role-name]
ROLE_DESCRIPTION [role-description]


# Manage Client
CLIENT_NAME [client-name]
CLIENT_REDIRECT_URI [client-redirect-uri]
CLIENT_SECRET [client-secret]

