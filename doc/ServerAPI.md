# Face Recongnition System (API)
## API: FaceRecongnition Dynamo Image CRUD APIs
0. basicURL = https://zc2oz2npjg.execute-api.us-east-1.amazonaws.com/ImageTable
1. POST {basicURL}/images
```
    Request
    {
        "object": {
            "key": "Henry19.jpg",
            "data": "What about you?"
        }
    }
    
    Response
    {
        "body": "{\"ImageName\": \"Henry19.jpg\", \"Result\": \"POST Success\"}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```
2. GET {basicURL}/images/{image-name}
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
3. DELETE  {basicURL}/images/{image-name}
```
    Response
    {
        "body": "{\"Result\": \"DELETE all the objects in the bucket\"}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```
4. DELETE  {basicURL}/images
```
    Response
    {
        "body": "{\"Result\": \"DELETE all the objects in the bucket\"}",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```
## API: FaceRecongnition Matching Result CRUD APIs
1. POST {basicURL}/results/{result-count}
```
    Request
    {
        "body": {
            "ImageName": "Henry32.jpg",
            "Content": "jpg binary array encoded with base64."
        }
    }
    
    Response Success
    {
        'body': '{"Exist": true, "Response": [{"ImageName": "Henry21.jpg", "Similarity": 100}]}', 
        'headers': {'Content-Type': 'application/json'}, 
    }
    
    Response Failed
    {
        "body": "Content can not be decoded by base64.",
        "headers": {"Content-Type":"application/json"}
    }
```
2. DELETE {basicURL}/results
```
    Response
    {
        "body": "[{\"Status\": \"Success\", \"Delete\": \"Henry21.jpg\"}, {\"Status\": \"Success\", \"Delete\": \"Henry16.jpg\"}]",
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": "200"
    }
```




