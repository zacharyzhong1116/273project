# Face Recongnition System (API)
## API: FaceRecongnition Dynamo Image CRUD APIs
0. URL: https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable
1. POST /images
```
    Request
    {
        "body": {
            "TableName": "Image",
            "ImageName": "Henry17",
            "Content": "He is going hiking"
        }
    }
    
    Response
    {
        "body": "{\"ResponseMetadata\": {\"RetryAttempts\": 0, \"HTTPStatusCode\": 200, \"RequestId\": \"7EQ4R6TIG8B50854TPTF5N3H4RVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"HTTPHeaders\": {\"x-amzn-requestid\": \"7EQ4R6TIG8B50854TPTF5N3H4RVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"content-length\": \"2\", \"server\": \"Server\", \"connection\": \"keep-alive\", \"x-amz-crc32\": \"2745614147\", \"date\": \"Mon, 01 May 2017 19:26:30 GMT\", \"content-type\": \"application/x-amz-json-1.0\"}}}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```
2. GET /images/{image-name}
```
    Response
    {
        "body": "{\"Content\": \"dsdfdfgsgsdgfdsgdfgfgergergergfdgdfgrgtergdfkmdlgmrltjgjdfkmglksjgjgsk\", \"ImageName\": \"Henry16\"}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```
3. DELETE  /images/{image-name}
```
    Response
    {
        "body": "{\"ResponseMetadata\": {\"RetryAttempts\": 0, \"HTTPStatusCode\": 200, \"RequestId\": \"49RT1TUOC6CCO8DNSH5JOS9BEJVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"HTTPHeaders\": {\"x-amzn-requestid\": \"49RT1TUOC6CCO8DNSH5JOS9BEJVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"content-length\": \"2\", \"server\": \"Server\", \"connection\": \"keep-alive\", \"x-amz-crc32\": \"2745614147\", \"date\": \"Mon, 01 May 2017 19:28:47 GMT\", \"content-type\": \"application/x-amz-json-1.0\"}}}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```

## API: FaceRecongnition Matching Result CRUD APIs
1. GET /faces/results?number=4
```
    Response
    {
        “ImageArray”: [
            {
                “ImageName”: “Henry16”,
                “Similarity”: 0.01
            }, 
            {
                “ImageName”: “Sam17”,
                “Similarity”: 0.97
            },
            {
                “ImageName”: “Ami18”,
                “Similarity”: 0.98
            },
            {
                “ImageName”: “Chary20”,
                “Similarity”: 0.66
            }
        ]
    }
```
2. DELETE /faces/results
```
    Response
    200 OK
```
3. DELETE /faces/results/{Image-name}
```
    Response
    200 OK
```




