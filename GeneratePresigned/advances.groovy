properties ([ 
    parameters ([
        choice(name: 'BUCKET_NAME', choices: ['puun-test-bucket1', 'puun-test-bucket2'], description: 'Select the S3 bucket'),
        string(name: 'OBJECT_ID', defaultValue: 'files', description: 'Enter the object key in the S3 bucket'),
        choice(name: 'TTL', choices: ['15', '30', '60'], description: 'Select the Time to Live (TTL) for the presigned URL in minutes'),
        string(name: 'EMAIL', defaultValue: '', description: 'Enter the email address to send the presigned URL')
    ])
])

runpipeline ([
    BUCKET_NAME: params.BUCKET_NAME,
    OBJECT_ID: params.OBJECT_ID,
    TTL: params.TTL.toInteger() * 60 ,
    EMAIL: params.EMAIL,
    AWS_CREDENTIALS_ID: 'aws-iam',
    AWS_REGION: 'ap-southeast-1'
]) { context -> 
    generatePresignedUploadUrl(context)
    sendingEmail(context)
}

def runpipeline(Map args, Closure stages) {
    node('master') {
        ansiColor('xterm') {
            timestamps(){
                timeout(time: 50, unit: 'MINUTES'){
                    skipDefaultCheckout(true)
                    try {
                        stages(args)
                    } catch (Exception err) {
                        throw err
                    } finally {
                        echo 'DONE'
                    }
                }
            }
        }
    }
}

def generatePresignedUploadUrl(Map args) {
    stage('Generate Presigned URL') {
        withAWS(region: "${args.AWS_REGION}", credentials: "${args.AWS_CREDENTIALS_ID}") {
            script {
                def awsCredentials = new com.amazonaws.auth.AWSStaticCredentialsProvider(
                    new com.amazonaws.auth.BasicAWSCredentials(
                        env.AWS_ACCESS_KEY_ID, env.AWS_SECRET_ACCESS_KEY
                    )
                )

                def s3Client = com.amazonaws.services.s3.AmazonS3ClientBuilder.standard()
                    .withCredentials(awsCredentials)
                    .withRegion(args.AWS_REGION)
                    .build()

                def calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, args.TTL.toInteger())
                def expiration = calendar.getTime()

                def generatePresignedUrlRequest = new com.amazonaws.services.s3.model.GeneratePresignedUrlRequest(args.BUCKET_NAME, args.OBJECT_ID)
                    .withMethod(com.amazonaws.HttpMethod.PUT)
                    .withExpiration(expiration)

                def presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString()
                echo "Upload Presigned URL: ${presignedUrl}"
            }
        }
    }
}

def sendingEmail(Map args) {
    stage('Send Presigned URL to Emails') {
        echo "Sending email to ${args.EMAIL}"
    }
}


