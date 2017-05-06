from __future__ import print_function

import base64
import urllib
import boto3
import json
import os

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
    s3 = boto3.resource('s3')
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
        if operation == 'GET':
            bucket = s3.Bucket(event['Records'][0]['s3']['bucket']['name'])
            key = urllib.unquote_plus(event['Records'][0]['s3']['object']['key'].encode('utf8'))
            try:
                # response = s3.get_object(Bucket=bucket, Key=key)
                f = open('temp.jpg.txt', 'wb')
                with f as data:
                    bucket.download_fileobj(key, data)
                f.close()
                f = open('temp.jpg.txt', 'r')
                with f as myfile:
                    content = myfile.read()
                f.close()

                os.remove('temp.jpg.txt')
                contentString = base64.b64encode(content)
                # print("CONTENT TYPE: " + response['ContentType'])
                # print("Body: " + response['Body'].read())
            except Exception as e:
                print(e)
                print(
                    'Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(
                        key, bucket))
                raise e

            # byteArray = base64.b64encode(response['Body'].read())
            # content = byteArray.decode("utf-8");
            result = {"ImageName": key, "Content": contentString}
            return respond(None, result)
        # elif operation == 'POST':
        #     payload = event['body']
        #     # print(payload)
        #     dynamo = boto3.resource('dynamodb').Table(payload['TableName'])
        #     response = dynamo.put_item(
        #         Item={
        #             'ImageName': payload['ImageName'],
        #             'Content': payload['Content'],
        #         }
        #     )
        #     return respond(None, response);
        # elif operation == "DELETE":
        #     payload = event['body']
        #     dynamo = boto3.resource('dynamodb').Table(payload['TableName'])
        #     response = dynamo.delete_item(
        #         Key={
        #             'ImageName': payload['ImageName']
        #         }
        #     )
        #     return respond(None, response);
        else:
            return respond(ValueError('Unsupported PUT "{}"'.format(operation)));
    else:
        return respond(ValueError('Unsupported method "{}"'.format(operation)))