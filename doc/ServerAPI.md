# Face Recongnition System (API)
## API: FaceRecongnition Dynamo Image CRUD APIs
1. POST /images
```
    Request
    {
        “ImageName": “Henry17”,
        “size”: 600,
        “Content”: “sdfsdfdagfgfdgjsgjkngergnoengodnfgjfdngkjnsjgnfkngjkdngjfn"
    }
    
    Response
    200 OK
```
2. GET /images/{image-name}
```
    Response
    {
        “Content”: “sdfsdfdagfgfdgjsgjkngergnoengodnfgjfdngkjnsjgnfkngjkdngjfn"
    }
```
3. DELETE  /images/{image-name}
```
    Response
    200OK
```
4. PUT /images/{image-name}
```
    Request
    {
        “ImageName": “Henry17”,
        “size”: 600,
        “Content”: “ABC"
    }
```

## API: FaceRecongnition Matching Result CRUD APIs
1. GET /faces/results?number=5
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




