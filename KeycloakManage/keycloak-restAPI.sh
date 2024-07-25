# Ref = https://www.keycloak.org/docs-api/latest/rest-api/index.html#_groups
# Ref = https://www.keycloak.org/docs-api/latest/rest-api/index.html#_users
# Ref = https://www.keycloak.org/docs-api/latest/rest-api/index.html#_clients

# Get Access Token


curl -X POST -k -s "http://<keycloak-server>/realms/master/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=admin" \
     -d "password=admin123" \
     -d "grant_type=client_credentials" \
     -d "client_id=admin-cli" | jq -r '.access_token' \
     -d "client_secret=WEVBqX0rDPfxbwYsCKp8FdUUTICbG7Xq"


#  ----------------------------------------

# Create Groups
curl -X POST "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/groups" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ## Access Token ##" \
     -d '{ 
           "name": "testgroup1"
         }'
#  ----------------------------------------

# Create a new user
curl -X POST "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/users" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ## Access Token ##" \
    -d '{
            "username": "user1",
            "enabled": true,
            "credentials": [{
                "type": "password",
                "value": "password",
                "temporary": false
            }]
        }'


# Get userID
USER_ID=$(curl -X GET "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/users" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer <access-token>" | jq -r '.[] | select(.username=="user1") | .id')

# Get groupID 
GROUP_ID=$(curl -X GET "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/groups" \
                 -H "Content-Type: application/json" \
                 -H "Authorization: Bearer <access-token>" | jq -r '.[] | select(.name=="testgroup1") | .id')


# Add User to Group
curl -X PUT "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/users/$USER_ID/groups/$GROUP_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ## Access Token ##" 

#  ----------------------------------------

# Change password of user1
curl -X PUT "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/users/$USER_ID/reset-password" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ## Access Token ##" \
        -d '{
            "type": "password",
            "value": "new-password",
            "temporary": false
        }'

#  ----------------------------------------

# Remove user1 Form testgroup1
curl -X DELETE "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/users/$USER_ID/groups/$GROUP_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ## Access Token ##"


#  ----------------------------------------

# Delete Group
curl -X DELETE "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/groups/$GROUP_ID" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ## Access Token ##"

#  ----------------------------------------

# Create a new client
curl -X POST "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/clients" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <access-token>" \
     -d '{
           "clientId": "## new-client-id ##",
           "enabled": true,
           "protocol": "openid-connect",
           "redirectUris": ["http://example.com/*"],
           "clientAuthenticatorType": "client-secret",
           "secret": "## new-client-secret ##"
         }'


#  ----------------------------------------

# Get Role ID

ROLE_ID=$(curl -X GET "http://<keycloak-server>/admin/realms/techx-ssp-exector-cicd-tools/roles" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer <access token>" | jq -r '.[] | select(.name=="## role-name ##") | .id')


curl --location --request POST 'http://{keycloak-server}/auth/admin/realms/{realm-name}/groups/{group-id}/role-mappings/realm' \
--header 'Authorization: Bearer {access-token}' \
--header 'Content-Type: application/json' \
--data-raw '[
  {
    "id": "{role-id}",
    "name": "{role-name}"
  }
]'

#  ----------------------------------------
