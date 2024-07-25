pipeline {
    agent any
    
    environment {
        AWS_REGION = 'ap-southeast-1' // Replace with your AWS region
        S3_BUCKET = 'puun-test-bucket1' // Replace with your S3 bucket name
        S3_OBJECT_KEY = 'files' // Replace with your S3 object key
        PRESIGNED_URL_EXPIRATION = 3600 // URL expiration time in seconds (e.g., 3600 seconds = 1 hour)
    }

    stages {
        stage('Generate Upload Presigned URL') {
            steps {
                withAWS(region: "${AWS_REGION}", credentials: 'aws-iam') {
                    script {
                        def presignedUrl = generatePresignedUploadUrl("${S3_BUCKET}", "${S3_OBJECT_KEY}")

                        echo "Upload Presigned URL: ${presignedUrl}"
                        
                        // ส่งอีเมลพร้อม presigned URL
                        emailext (
                            subject: "Upload Presigned URL",
                            body: "Here is the upload presigned URL: ${presignedUrl}",
                            to: 'recipient@example.com' // Replace with the recipient's email address
                        )
                    }
                }
            }
        }
    }
}

// Method to generate presigned URL for upload
def generatePresignedUploadUrl(bucket, key) {
    def awsCredentials = new com.amazonaws.auth.AWSStaticCredentialsProvider(
        new com.amazonaws.auth.BasicAWSCredentials(
            env.AWS_ACCESS_KEY_ID, env.AWS_SECRET_ACCESS_KEY
        )
    )

    def s3Client = com.amazonaws.services.s3.AmazonS3ClientBuilder.standard()
        .withCredentials(awsCredentials)
        .withRegion(env.AWS_REGION)
        .build()

    def calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, env.PRESIGNED_URL_EXPIRATION.toInteger())
    def expiration = calendar.getTime()

    def generatePresignedUrlRequest = new com.amazonaws.services.s3.model.GeneratePresignedUrlRequest(bucket, key)
        .withMethod(com.amazonaws.HttpMethod.PUT)
        .withExpiration(expiration)

    return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString()
}
