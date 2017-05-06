import boto3

# aws face compare function template
client = boto3.client('rekognition')

response = client.compare_faces(
    SimilarityThreshold=90,
    SourceImage={
        'S3Object': {
            'Bucket': 'mybucket',
            'Name': 'mysourceimage',
        },
    },
    TargetImage={
        'S3Object': {
            'Bucket': 'mybucket',
            'Name': 'mytargetimage',
        },
    },
)

print(response)