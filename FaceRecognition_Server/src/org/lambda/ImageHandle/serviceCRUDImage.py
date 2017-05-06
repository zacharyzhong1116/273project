# We save base64 String in S3.
from __future__ import print_function

import base64
import urllib
import boto3
import json
import sys

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

    # s3 = boto3.client('s3')
    print("Received event: " + json.dumps(event, indent=2))

    operations = {
        'DELETE': lambda dynamo, x: dynamo.delete_item(**x),
        'GET': lambda dynamo, x: dynamo.scan(**x),
        'POST': lambda dynamo, x: dynamo.put_item(**x),
        'PUT': lambda dynamo, x: dynamo.update_item(**x),
    }

    operation = event['httpMethod']

    if operation in operations:
        if operation == 'GET':
            bucket = event['Records'][0]['s3']['bucket']['name']
            key = urllib.unquote_plus(event['Records'][0]['s3']['object']['key'].encode('utf8'))

            obj = s3.Object(bucket, key)
            content = obj.get()['Body'].read()

            # Since we don't do integration test, our jpg file content is binary
            # contentString = base64.b64encode(content)
            result = {"ImageName": key, "Content": content}
            return respond(None, result)
        elif operation == 'POST':
            bucket = event['Records'][0]['s3']['bucket']['name']
            key = urllib.unquote_plus(event['Records'][0]['s3']['object']['key'].encode('utf8'))
            data = base64.b64encode(event['Records'][0]['s3']['object']['data'])
            obj = s3.Object(bucket, key)
            # f = open('test1.jpg', 'rb')
            # with f as myfile:
            #     dataRaw = myfile.read()
            # f.close()
            # data = base64.b64encode(dataRaw)

            # Here we need to use Body=data since the arguement is a json object
            obj.put(Body=data);
            print("Object is " + str(obj))
            result = {"ImageName": key, "Result": "POST Success"}
            return respond(None, result);
        elif operation == "DELETE":
            bucketName = event['Records'][0]['s3']['bucket']['name']
            key = urllib.unquote_plus(event['Records'][0]['s3']['object']['key'].encode('utf8'))
            if key == "":
                bucket = s3.Bucket(bucketName)
                bucket.objects.delete()
            else:
                obj = s3.Object(bucketName, key)
                obj.delete()
            result = {"Result": "DELETE all the objects in the bucket"}
            return respond(None, result)
        else:
            return respond(ValueError('Unsupported PUT "{}"'.format(operation)));
    else:
        return respond(ValueError('Unsupported method "{}"'.format(operation)))