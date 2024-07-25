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

        stage('Check Existing Group') {
            steps {
                script {
                    def response = httpRequest(
                        url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups",
                        httpMode: 'GET',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]]
                    )
                    def groups = new groovy.json.JsonSlurper().parseText(response.content)
                    def group = groups.find { it.name == 'testgroup1' }
                    if (group) {
                        env.GROUP_ID = group.id
                    }
                }
            }
        }

        stage('Create Group') {
            steps {
                script {
                    if (env.GROUP_ID) {
                        echo "Group already exists"
                    } else {
                        httpRequest(
                            url: "http://${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups",
                            httpMode: 'POST',
                            contentType: 'APPLICATION_JSON',
                            customHeaders: [[name: 'Authorization', value: "Bearer ${env.ACCESS_TOKEN}"]],
                            requestBody: '{"name": "testgroup1"}'
                        )
                    }
                }
            }
        }
        // if else condition to check if group exists && Check what the group scope [ Realms-Roles, Clients-Roles, Composite-Roles]
        // stages assigned roles to group
    }
}
