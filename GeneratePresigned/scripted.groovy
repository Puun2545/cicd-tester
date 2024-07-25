pipeline {
    agent { label 'master' }

    parameters {
        choice(name: 'BUCKET_NAME', choices: ['puun-test-bucket1', 'puun-test-bucket2'], description: 'Select the S3 bucket')
        string(name: 'OBJECT_ID', defaultValue: 'files', description: 'Enter the object key in the S3 bucket')
        choice(name: 'TTL', choices: ['30', '60'], description: 'Select the Time to Live (TTL) for the presigned URL in minutes')
        string(name: 'EMAIL', defaultValue: '', description: 'Enter the email address to send the presigned URL')
    }

    environment {
        AWS_CREDENTIALS_ID = 'aws-iam' // ใช้ ID ของ AWS Credentials ที่เพิ่มใน Jenkins
        AWS_REGION = 'ap-southeast-1' // กำหนด region ที่ต้องการใช้
    }

    stages {
        stage('Generate Presigned URL') {
            steps {
                script {
                    withCredentials([aws(credentialsId: "${AWS_CREDENTIALS_ID}")]) {
                        def ttlSeconds = params.TTL.toInteger() * 60
                        def presignedUrl = sh(
                            script: "aws s3 presign s3://${params.BUCKET_NAME}/uploads/${params.OBJECT_ID} --expires-in ${ttlSeconds} --region ${env.AWS_REGION}  --method PUT",
                            returnStdout: true
                        ).trim()

                        echo "Presigned URL: ${presignedUrl}"

                        // Store presigned URL in a variable for later use
                        currentBuild.description = "Presigned URL: ${presignedUrl}"
                        env.PRESIGNED_URL = presignedUrl
                    }
                }
            }
        }

        stage('Send Email') {
            steps {
                emailext (
                    subject: "Your Presigned URL",
                    body: """
                        Hello,

                        Here is your presigned URL for the S3 bucket: ${params.BUCKET_NAME}

                        Presigned URL: ${env.PRESIGNED_URL}

                        This URL will expire in ${params.TTL} minutes.

                        Regards,
                        Jenkins
                    """,
                    to: "${params.EMAIL}",
                    attachLog: true
                )
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
    }
}
