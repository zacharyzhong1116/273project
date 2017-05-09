# Problems in our code
## coding
service.py (Result)
```
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
```
+ We use except Exception to ignore all the exception when we compareImages
+ It may be not good.
