pipeline {
    agent any

    parameter {
        string(name: 'GROUP_NAME', defaultValue: 'testgroup1', description: 'Group name to create')
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

        stage('Check Existing Group') {
            steps {
                script {
                    def response = sh(script: """
                        curl -X GET -k -s \
                        -H "Content-Type: application/json" \
                        -H "Authorization: Bearer ${env.ACCESS_TOKEN}" \
                        "${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups"
                    """, returnStdout: true).trim()

                    def groups = new groovy.json.JsonSlurper().parseText(response)
                    def group = groups.find { it.name == "${params.GROUP_NAME}" }
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
                        sh(script: """
                            curl -X POST -k -s \
                            -H "Content-Type: application/json" \
                            -H "Authorization: Bearer ${env.ACCESS_TOKEN}" \
                            -d '{
                                "name": "${params.GROUP_NAME}",
                                "attributes": {}
                            }' \
                            "${KEYCLOAK_SERVER}/admin/realms/${REALM}/groups"
                        """)
                    }
                }
            }
        }
        // if else condition to check if group exists && Check what the group scope [ Realms-Roles, Clients-Roles, Composite-Roles]
        // stages assigned roles to group
    }
}
