pipeline {
    agent any

    parameters {
        string(name: 'USERNAME', defaultValue: 'user1', description: 'Username for the new user')
        string(name: 'EMAIL', defaultValue: 'user1@example.com', description: 'Email address for the new user')
        string(name: 'FIRST_NAME', defaultValue: 'John', description: 'First name of the new user')
        string(name: 'LAST_NAME', defaultValue: 'Doe', description: 'Last name of the new user')
        string(name: 'PASSWORD', defaultValue: 'password', description: 'Password for the new user')
        string(name: 'PHONE', defaultValue: '', description: 'Phone number for the new user')
        string(name: 'DEPARTMENT', defaultValue: '', description: 'Department for the new user')
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

                     if (response == null || response.isEmpty()) {
                        error "Failed to retrieve access token"
                    }
                    env.ACCESS_TOKEN = response
                }
            }
        }

        stage('Create User') {
            steps {
                script {
                    def requestBody = """
                    {
                        "username": "${params.USERNAME}",
                        "email": "${params.EMAIL}",
                        "firstName": "${params.FIRST_NAME}",
                        "lastName": "${params.LAST_NAME}",
                        "enabled": true,
                        "credentials": [
                            {
                                "type": "password",
                                "value": "${params.PASSWORD}",
                                "temporary": false
                            }
                        ],
                        "attributes": {
                            "phone": "${params.PHONE}",
                            "department": "${params.DEPARTMENT}"
                        }
                    }
                    """
                    def response = sh(script: """
                        curl -X POST -k -s \
                        -H "Content-Type: application/json" \
                        -H "Authorization: Bearer ${env.ACCESS_TOKEN}" \
                        -d '${requestBody}' \
                        "${KEYCLOAK_SERVER}/admin/realms/${REALM}/users"
                    """)
                }
            }
        }
    }
}
