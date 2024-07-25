pipeline {
    agent any

    environment {
        KEYCLOAK_SERVER = "localhost:8800"
        REALM = "test-realms"
        ADMIN_USERNAME = "admin"
        ADMIN_PASSWORD = "admin123"
        CLIENT_ID = "admin-cli"
        CLIENT_SECRET = "bXieO6lmGx4QO5zyLwoDZNL5KHIcAt80"
    }

    stages{
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

        stage('Get User ID') {
            steps {
                script {
                    def response = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users",
                        httpMode: 'GET',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    def users = new groovy.json.JsonSlurper().parseText(response.content)
                    env.USER_ID = users.find { it.username == 'user1' }.id
                }
            }
        }

        stage('Get Group ID') {
            steps {
                script {
                    def response = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups",
                        httpMode: 'GET',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    def groups = new groovy.json.JsonSlurper().parseText(response.content)
                    env.GROUP_ID = groups.find { it.name == 'testgroup1' }.id
                }
            }
        }

        stage('Add User to Group') {
            steps {
                script {
                    def response = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/users/${USER_ID}/groups/${GROUP_ID}",
                        httpMode: 'PUT',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    echo "User added to group: ${response.status}"
                }
            }
        }


    }
}