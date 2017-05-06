from __future__ import print_function
from org.face.match.match_face import compareImages
# from boto3.dynamodb.conditions import Key, Attr

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
    # if operation in operations:
    #     payload = event['queryStringParameters'] if operation == 'GET' else json.loads(event['body'])
    #     dynamo = boto3.resource('dynamodb').Table(payload['TableName'])
    #     response = dynamo.scan(FilterExpression=Attr(payload['Attribute']).eq(payload['Value']))
    #     items = response['Items']
    #     # print(items[0][payload['Attribute']])
    #     result = {'ImageName': items[0]['ImageName'], 'Content': items[0]['Content'], 'Similarity': items[0]['Similarity']};
    #     print(result)
    #     # operations[operation](dynamo, payload)
    #     return respond(None, list(result))
    # else:
    #     return respond(ValueError('Unsupported method "{}"'.format(operation)))

    if operation in operations:
        if operation == 'POST':
            payload = event['body']
            # print(payload)
            dynamo = boto3.resource('dynamodb').Table(event['tableName'])

            # How many pictures the customer want
            returnNumber = payload['Count']

            # The content of the first picture
            content1 = payload['Content']
            imageName1 = payload['ImageName']

            # Get all the objects from bucket in S3
            # Get all the content in these objects and compare with the input picture
            # Batch write all the result to DynamoDB table: dynamo

            # Get all the items from table "Image"
            response = dynamo.scan()
            items = response['Items']
            diction = {}
            # Compare with all the pictures in the database.
            for item in items:
                imageName2 = item['ImageName']
                content2 = item['Content']
                print("Comparing" + imageName2 + " with " + imageName1)
                similarity = compareImages(content1, content2)
                diction[imageName2] = similarity

            # Batch write all the result in the dictionary to DynamoDB table "Result"
            dynamoResult = boto3.resource('dynamodb').Table(event['tableName'])
            with dynamoResult.batch_writer() as batch:
                for imageName, similarity in diction.iteritems():
                    batch.put_item(
                        Item={
                            'ImageName': imageName,
                            'Similarity': similarity
                        }
                    )

            # Quary only required number of items with ascend similarity.
            response = dynamoResult.scan(
                Limit=returnNumber
            )
            items = response['Items']

            # return the response.
            return respond(None, items[0])
        elif operation == "DELETE":
            payload = event['body']

            # get the Count(*) from table "Result".
            dynamoResult = boto3.resource('dynamodb').Table(event['resultTableName'])
            response = dynamoResult.scan()
            items = response['Items']
            responseList = []
            i = 0
            # Compare with all the pictures in the database.
            for item in items:
                imageName2 = item['ImageName']
                print("Deleting " + imageName2 + " from table: " + event['resultTableName'])
                response = dynamoResult.delete_item(
                    Key={
                        'ImageName': imageName2
                    }
                )
                responseList[i] = response
                i = i + 1
            return respond(None, responseList);
        else:
            return respond(ValueError('Unsupported PUT and GET "{}"'.format(operation)));
    else:
        return respond(ValueError('Unsupported method "{}"'.format(operation)))