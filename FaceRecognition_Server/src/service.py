from __future__ import print_function

from boto3.dynamodb.conditions import Key, Attr
from decimal import Decimal

import base64
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

def compareImages(content1, imageName2):
    client = boto3.client('rekognition')
    response = client.compare_faces(
        SimilarityThreshold=90,
        SourceImage={
            'Bytes': content1
        },
        TargetImage={
            'S3Object': {
                'Bucket': 'images273',
                'Name': imageName2,
            },
        },
    )
    print("compareImages response = "+ str(response))
    return response


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
            returnNumber = event['count']
            diction = {}
            exist = False

            # The content of the first picture
            content = payload['Content']
            # if content != "":
            #     content = base64.b64encode(content)
            # content1 = content
            imageName1 = payload['ImageName']

            # just for testing
            if content == "":
                f = open('test1.jpg', 'rb')
                with f as myfile:
                    content1 = myfile.read()
                f.close()
            else:
                try:
                    content1 = base64.b64decode(content)
                except Exception:
                    return respond(ValueError('Content can not be decoded by base64.'.format(operation)));

            # print ("content1 = " + str(content1))
            # Get all the objects from bucket in S3
            s3 = boto3.resource('s3')
            bucketName = event['bucketName']
            bucket = s3.Bucket(bucketName)
            for obj in bucket.objects.all():
                # Get all the content in these objects and compare with the input picture
                imageName2 = obj.key
                # Is content2 encoding with base64? We need to test.
                # content2 = obj.get()['Body'].read()
                print("Comparing " + imageName1 + " with " + imageName2)
                # Decimal('1') isn't json serizable, so I use this method to change similarity to str and json.dumps it.
                try:
                    result = compareImages(content1, imageName2)
                except Exception:
                    continue
                similarity = ""
                if len(result['FaceMatches']) != 0:
                    similarity = result['FaceMatches'][0]['Similarity']
                else:
                    continue
                # If we don't use similarity_json, Decimal('91') in the response['items'][0] can not be json.dumps()
                similarity_json = json.dumps(str(Decimal(similarity)))
                print("similarity = " + str(similarity))
                # similarity = 0
                diction[imageName2] = similarity
                if similarity > 90:
                    exist = True
                print("exist = " + str(exist))

            # Batch write all the result in the dictionary to DynamoDB table "Result"
            dynamoResult = boto3.resource('dynamodb').Table(event['tableName'])
            with dynamoResult.batch_writer() as batch:
                for imageName, similarity in diction.iteritems():
                    batch.put_item(
                        Item={
                            'ImageName': imageName,
                            'Similarity': int(similarity)
                        }
                    )

            # Quary only required number of items with ascend similarity.
            response = dynamoResult.scan(
                IndexName='ImageName-Similarity-index',
                Limit=returnNumber,
                FilterExpression=Attr('Similarity').gte(90)
            )
            items = response['Items']
            # convert Decimal('91') to 91 in response
            for item in items:
                item['Similarity'] = int(item['Similarity'])

            # print("response['Items'][0] = " + items[0])
            if len(items) != 0:
                print("items = " + str(items))
                result = {"Exist": exist, "Response": items}
                return respond(None, result)
            else:
                result = {"Exist": exist, "Response": "No matching images"}
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
                similarity2 = item['Similarity']
                print("Deleting " + imageName2 + " from table: " + event['tableName'])
                response = dynamoResult.delete_item(
                    Key={
                        'ImageName': imageName2,
                        'Similarity': similarity2
                    }
                )
                response = {"Delete": imageName2, "Status": "Success"}
                responseList.append(response)

            return respond(None, responseList);
        else:
            return respond(ValueError('Unsupported PUT and GET "{}"'.format(operation)));
    else:
        return respond(ValueError('Unsupported method "{}"'.format(operation)))