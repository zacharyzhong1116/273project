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




