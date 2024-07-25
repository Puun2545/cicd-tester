pipeline {
    agent any

    environment {
        KEYCLOAK_SERVER = "<keycloak-server>"
        REALM = "techx-ssp-exector-cicd-tools"
        ADMIN_USERNAME = "admin"
        ADMIN_PASSWORD = "admin123"
        CLIENT_ID = "admin-cli"
        CLIENT_SECRET = "WEVBqX0rDPfxbwYsCKp8FdUUTICbG7Xq"
        NEW_CLIENT_ID = "new-client-id"
        NEW_CLIENT_SECRET = "new-client-secret"
    }

    stages {
        stage('Get Access Token') {
            steps {
                script {
                    def response = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/realms/master/protocol/openid-connect/token",
                        httpMode: 'POST',
                        contentType: 'APPLICATION_FORM',
                        requestBody: "username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}&grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}"
                    )
                    def json = new groovy.json.JsonSlurper().parseText(response.content)
                    env.ACCESS_TOKEN = json.access_token
                }
            }
        }

        stage('Create Group') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]],
                    requestBody: '{"name": "testgroup1"}'
                )
            }
        }

        stage('Create User') {
            steps {
                // params : username, password,
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]],
                    requestBody: '{
                    "username": "user1", 
                    "enabled": true, 
                    "credentials": [{
                        "type": "password",
                        "value": "password",
                        "temporary": false}
                    ]}'
                )
            }
        }

        stage('Get UserID and GroupID') {
            steps {
                script {
                    def userResponse = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users",
                        httpMode: 'GET',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    def users = new groovy.json.JsonSlurper().parseText(userResponse.content)
                    env.USER_ID = users.find { it.username == 'user1' }.id

                    def groupResponse = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups",
                        httpMode: 'GET',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    def groups = new groovy.json.JsonSlurper().parseText(groupResponse.content)
                    env.GROUP_ID = groups.find { it.name == 'testgroup1' }.id
                }
            }
        }

        stage('Add User to Group') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users/${env.USER_ID}/groups/${env.GROUP_ID}",
                    httpMode: 'PUT',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                )
            }
        }

        stage('Change User Password') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users/${env.USER_ID}/reset-password",
                    httpMode: 'PUT',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]],
                    requestBody: '{"type": "password", "value": "new-password", "temporary": false}'
                )
            }
        }

        stage('Remove User from Group') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users/${env.USER_ID}/groups/${env.GROUP_ID}",
                    httpMode: 'DELETE',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                )
            }
        }

        stage('Delete Group') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups/${env.GROUP_ID}",
                    httpMode: 'DELETE',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                )
            }
        }

        stage('Create Client') {
            steps {
                httpRequest(
                    url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/clients",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]],
                    requestBody: '{"clientId": "${env.NEW_CLIENT_ID}", "enabled": true, "protocol": "openid-connect", "redirectUris": ["http://example.com/*"], "clientAuthenticatorType": "client-secret", "secret": "${env.NEW_CLIENT_SECRET}"}'
                )
            }
        }
    }
}
