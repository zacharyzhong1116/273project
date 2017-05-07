from __future__ import print_function
from decimal import Decimal
# from org.face.match.match_face import match_face
# from boto3.dynamodb.conditions import Key, Attr
import base64
# import match_face

import boto3
import json

print('Loading function')

def respond(err, res=None):
    return {
        'statusCode': '400' if err else '200',
        'body': err.message if err else json.dumps(res),
        'headers': {
            'Content-Type': 'application/json',
        },
    }


def compareTest(content2, content1):
    print("content2 = " + content2)
    print("content1 = " + content1)
    if content1 == content2:
        return 0
    else:
        return 1

def lambda_handler(event, context):
    '''Demonstrates a simple HTTP endpoint using API Gateway. You have full
    access to the request and response payload, including headers and
    status code.

    To scan a DynamoDB table, make a GET request with the TableName as a
    query string parameter. To put, update, or delete an item, make a POST,
    PUT, or DELETE request respectively, passing in the payload to the
    DynamoDB API as a JSON body.
    '''
    print("Received event: " + json.dumps(event, indent=2))

    operations = {
        'DELETE': lambda dynamo, x: dynamo.delete_item(**x),
        'GET': lambda dynamo, x: dynamo.scan(**x),
        'POST': lambda dynamo, x: dynamo.put_item(**x),
        'PUT': lambda dynamo, x: dynamo.update_item(**x),
    }

    operation = event['httpMethod']

    if operation in operations:
        if operation == 'POST':
            payload = event['body']

            # How many pictures the customer want
            returnNumber = payload['Count']
            diction = {}
            exist = False

            # The content of the first picture
            content = payload['Content']

            # Only for testing. encode the content before comparing.
            content1 = base64.b64encode(content)
            imageName1 = payload['ImageName']

            # Get all the objects from bucket in S3
            s3 = boto3.resource('s3')
            bucketName = event['bucketName']
            bucket = s3.Bucket(bucketName)
            for obj in bucket.objects.all():
                # Get all the content in these objects and compare with the input picture
                imageName2 = obj.key
                # Is content2 encoding with base64? We need to test.
                content2 = obj.get()['Body'].read()
                print("Comparing " + imageName2 + " with " + imageName1)
                # similarity = match_face(content2, content1)
                # Decimal('1') isn't json serizable, so I use this method to change similarity to str and json.dumps it.
                similarity = compareTest(content2, content1)
                similarity_json = json.dumps(str(Decimal(similarity)))
                print("similarity = " + str(similarity))
                # similarity = 0
                diction[imageName2] = similarity_json
                if similarity == 0:
                    exist = True
                print("exist = " + str(exist))

            # Batch write all the result in the dictionary to DynamoDB table "Result"
            dynamoResult = boto3.resource('dynamodb').Table(event['tableName'])
            with dynamoResult.batch_writer() as batch:
                for imageName, similarity_json in diction.iteritems():
                    batch.put_item(
                        Item={
                            'ImageName': imageName,
                            'Similarity': similarity_json
                        }
                    )

            # Quary only required number of items with ascend similarity.
            response = dynamoResult.scan(
                Limit=returnNumber
            )
            items = response['Items']
            print(items[0])
            result = {"Exist": exist, "Response": items[0]}
            # return the response.
            return respond(None, result)
        elif operation == "DELETE":
            # get the Count(*) from table "Result".
            dynamoResult = boto3.resource('dynamodb').Table(event['tableName'])
            response = dynamoResult.scan()
            items = response['Items']
            responseList = []

            # Compare with all the pictures in the database.
            for item in items:
                imageName2 = item['ImageName']
                print("Deleting " + imageName2 + " from table: " + event['tableName'])
                response = dynamoResult.delete_item(
                    Key={
                        'ImageName': imageName2
                    }
                )
                response = {"Delete": imageName2, "Status": "Success"}
                responseList.append(response)

            return respond(None, responseList);
        else:
            return respond(ValueError('Unsupported PUT and GET "{}"'.format(operation)));
    else:
        return respond(ValueError('Unsupported method "{}"'.format(operation)))