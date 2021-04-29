import cv2
import numpy as np

# read the image
image = cv2.imread('C:/Users/fer27/OneDrive/Escritorio/Java_Corba/corba/img/monaLisa.jpg')
# apply the 3x3 median filter on the image
#edge detection filter
kernel = np.array([[0.0, -1.0, 0.0], 
                   [-1.0, 4.0, -1.0],
                   [0.0, -1.0, 0.0]])

kernel = kernel/(np.sum(kernel) if np.sum(kernel)!=0 else 1)

#filter the source image
img_rst = cv2.filter2D(image,-1,kernel)
print("Saving image...")
#save result image
cv2.imwrite('C:/Users/fer27/OneDrive/Escritorio/Java_Corba/corba/img/filter.jpg',img_rst)