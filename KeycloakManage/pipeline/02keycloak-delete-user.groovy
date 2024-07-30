pipeline {
    agent any

    parameter {
        string(name: 'USER_NAME', defaultValue: 'user1', description: 'User name to delete')
    }

    environment {
        KEYCLOAK_SERVER = "172.17.0.3:8080"
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
                    def response = sh(script: """
                        curl -X POST -k -s \
                        -H "Content-Type: application/x-www-form-urlencoded" \
                        "${KEYCLOAK_SERVER}/realms/master/protocol/openid-connect/token" \
                        --data "username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}&client_id=${CLIENT_ID}&grant_type=client_credentials&client_secret=${CLIENT_SECRET}" | jq -r '.access_token'
                    """, returnStdout: true).trim()
                    env.ACCESS_TOKEN = response
                    echo "Access Token: ${env.ACCESS_TOKEN}"
                }
            }
        }

        // GOOD TO BE LOOP | GET USER NAME BY LIST
        stage('Get User ID') {
            steps {
                script {
                    def response = sh(script: """
                        curl -X GET -k -s \
                        -H "Content-Type: application/json" \
                        -H "Authorization: Bearer ${env.ACCESS_TOKEN}" \
                        "${KEYCLOAK_SERVER}/admin/realms/${REALM}/users"
                    """, returnStdout: true).trim()

                    def users = new groovy.json.JsonSlurper().parseText(response)
                    env.USER_ID = users.find { it.username == "${params.USER_NAME}" }.id

                    if (env.USER_ID == null) {
                        error("User not found")
                    }
                }
            }
        }

        // GOOD TO BE LOOP | GET USER ID BY LIST
        stage('Delete User') {
            steps {
                script {
                    sh(script: """
                        curl -X DELETE -k -s \
                        -H "Content-Type: application/json" \
                        -H "Authorization: Bearer ${env.ACCESS_TOKEN}" \
                        "${KEYCLOAK_SERVER}/admin/realms/${REALM}/users/${USER_ID}"
                    """)
                }
            }
        } 
    }
}