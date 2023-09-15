from PIL import Image
import numpy as np

if __name__ == '__main__':
    path = '7.jpg'
    filename = path.split('/')[-1][::-1].split('.', 1)[1][::-1]
    img = Image.open(path, 'r')
    width, height = img.size
    pixel_values = list(img.getdata())
    pixel_values = np.array(pixel_values).reshape((width, height, 3))
    pixel_values = np.array([pixel_values[:, :, 0], pixel_values[:, :, 1], pixel_values[:, :, 2]])
    print(pixel_values.shape)
    pixel_values.tofile(filename + ".rgb")