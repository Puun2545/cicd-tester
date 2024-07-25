# https://medium.com/@imsanthiyag/introduction-to-keycloak-admin-api-44beb9011f7d
# https://www.keycloak.org/docs-api/25.0.1/rest-api/index.html
# https://www.keycloak.org/docs-api/25.0.1/rest-api/index.html#RealmRepresentation

# Get OIDC Token on master realm
Setting admin-cli client to enable
- Client authentication = on
- Direct access grants (checked)
- Service accounts roles (checked)
  At Service account roles Tab
  Assign Realm Role to this service account : create-realm

KEYCLOAK_URL="https://keycloak-internal.xplatformnp.aella.tech"
REALM_ID="newrealm"
ADMIN_CLIENT_TOKEN=$(curl -X POST -k -s -H 'Content-Type: application/x-www-form-urlencoded' '${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token' --data 'username=keycloak_admin&password=P@ssw0rd123!&client_id=admin-cli&grant_type=client_credentials&client_secret=WEVBqX0rDPfxbwYsCKp8FdUUTICbG7Xq' | jq -r '.access_token')


# Get Realm Configuration
${KEYCLOAK_URL}/realms/techx-std-executor-cicd-tools/.well-known/openid-configuration

# Create and Delete Realm
curl -X POST "${KEYCLOAK_URL}/admin/realms" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${ADMIN_CLIENT_TOKEN}" \
     -d '{
            "realm": "newrealm",
            "displayName": "New Realm",
            "enabled": true,
            "sslRequired": "external",
            "registrationAllowed": false,
            "loginWithEmailAllowed": true,
            "duplicateEmailsAllowed": false,
            "resetPasswordAllowed": false,
            "editUsernameAllowed": false,
            "bruteForceProtected": true
        }'

curl -X DELETE "${KEYCLOAK_URL}/admin/realms/newrealm" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${ADMIN_CLIENT_TOKEN}"


# Create and Delete Client
curl -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_ID}/clients" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${ADMIN_CLIENT_TOKEN}" \
     -d '{
           "clientId": "testid",
           "enabled": true,
           "protocol": "openid-connect",
           "redirectUris": ["http://example.com/*"],
           "clientAuthenticatorType": "client-secret",
           "secret": "l65q02RZ0744At5HsEIaZku9TAgcywA4"
         }'



Create Group
curl -X POST "${KEYCLOAK_URL}/admin/realms/techx-std-exector-cicd-tools/groups" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer xxxx" \
     -d '{ 
           "name": "JenkinsXPL_PlatformAdmin"
         }'
 