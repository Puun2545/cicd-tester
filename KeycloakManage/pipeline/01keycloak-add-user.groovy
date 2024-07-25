pipeline {
    agent any

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

        stage('Create User') {
            steps {
                script {
                    def requestBody = """
                    {
                        "username": "user1",
                        "enabled": true,
                        "credentials": [
                            {
                                "type": "password",
                                "value": "password",
                                "temporary": false
                            }
                        ]
                    }
                    """
                    sh(script: """
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
