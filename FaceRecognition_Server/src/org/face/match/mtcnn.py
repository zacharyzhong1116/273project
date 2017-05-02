import tensorflow as tf
import detect_face
import cv2
import matplotlib as mil
mil.use('TKAgg')
import matplotlib.pyplot as plt
import cv2.face



def get_face(imgPath):
#face detection parameters
    minsize = 20 # minimum size of face
    threshold = [ 0.6, 0.7, 0.7 ]  # three steps's threshold
    factor = 0.709 # scale factor

    #facenet embedding parameters
    # imgPath = "./test1.JPG"
    img = cv2.imread(imgPath)


    # def to_rgb(img):
    #   w, h = img.shape
    #   ret = np.empty((w, h, 3), dtype=np.uint8)
    #   ret[:, :, 0] = ret[:, :, 1] = ret[:, :, 2] = img
    #   return ret

    #restore mtcnn model

    print('Creating networks and loading parameters')
    gpu_memory_fraction=1.0
    with tf.Graph().as_default():
        gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=gpu_memory_fraction)
        sess = tf.Session(config=tf.ConfigProto(gpu_options=gpu_options, log_device_placement=False))
        with sess.as_default():
            pnet, rnet, onet = detect_face.create_mtcnn(sess, './model_check_point/')

    bounding_boxes, _ = detect_face.detect_face(img, minsize, pnet, rnet, onet, threshold, factor)
    nrof_faces = bounding_boxes.shape[0]
    print('find face number:{}'.format(nrof_faces))

    crop_faces = []
    labels = []
    i = 0
    for face_position in bounding_boxes:

        face_position = face_position.astype(int)
        print(face_position[0:4])
        cv2.rectangle(img, (face_position[0], face_position[1]), (face_position[2], face_position[3]), (0, 255, 0), 2)
        crop = img[face_position[1]:face_position[3],
               face_position[0]:face_position[2], ]

        crop = cv2.resize(crop, (96, 96), interpolation=cv2.INTER_CUBIC)

        print(crop.shape)
        crop_faces.append(crop)
        labels.append(i)
        i+=1

        plt.imshow(crop)
        plt.show()

        plt.imshow(img)
        plt.show()

        return crop_faces, labels


# plt.imshow(img)
# plt.show()

