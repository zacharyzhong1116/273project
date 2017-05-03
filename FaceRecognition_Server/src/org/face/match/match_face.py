import cv2.face
import mtcnn
import numpy as np
import cv2

def match_face(model, pair):
    global_conf = None
    nparr_model = np.fromstring(model, np.uint8)
    path = cv2.imdecode(nparr_model, cv2.CV_LOAD_IMAGE_COLOR)
    recognizer = cv2.face.createLBPHFaceRecognizer()
    # path = './train_dir/yu/yu2.jpg'
    model_faces, model_labels = mtcnn.get_face(path)
    print model_labels
    model_faces_gray = []

    for face in model_faces:
        gray_image = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)
        model_faces_gray.append(gray_image)

    recognizer.train(model_faces_gray, np.array(model_labels))

    nparr_pair = np.fromstring(pair, np.uint8)
    imgPath = cv2.imdecode(nparr_pair, cv2.CV_LOAD_IMAGE_COLOR)
    # imgPath = './train_dir/yu/yu.jpg'
    # img_pair = cv2.imread(path)
    pair_faces, pair_labels = mtcnn.get_face(imgPath)

    pair_faces_gray = []

    for face in pair_faces:
        gray_image = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)
        pair_faces_gray.append(gray_image)

    for face in pair_faces_gray:
        global global_conf
        nbr_predicted, conf = recognizer.predict(face)
        print "Recognized with confidence {}".format(conf)
        global_conf = conf

    return global_conf
