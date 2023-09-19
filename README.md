# Multimedia Systems Design

#### Content

1. Quantization and Subsampling
2. Image segmentation
3. DWT Compression
4. Background-Foreground Panorama Analysis



####   **Quantization and Subsampling**

**Parameter Description**

1. **Image Name**:
   - Description: The first parameter is the name of the image.
   - Format: Provided in an 8-bit per channel RGB format (Total 24 bits per pixel).
   - Assumption: You may assume that all images will be of the same size for this assignment.
   - Size: HD size = 1920w x 1080h.
   - Additional Information: More information on the image format will be placed on the class website.
2. **Subsampling Control**:
   - Description: The next three parameters are integers controlling the subsampling of your Y, U, and V spaces respectively.
   - Convention: For the sake of simplicity, we will follow the convention that subsampling occurs only along the width dimension and not the height.
   - Range: Each of these parameters can take on values from 1 to n for some n, 1 suggesting no subsampling and n suggesting a subsampling by n.
3. **Image Scaling Control**:
   - Description: The next two parameters are single precision floats Sw and Sh.
   - Range: They take positive values < 1.0.
   - Function: Control the scaled output image width and height independently.
4. **Antialiasing Setting**:
   - Description: A integer A (0 or 1).
   - Indication: Suggests whether antialiasing (prefiltering needs to be performed).
   - 0 indicates no antialiasing, and 1 indicates antialiasing.



**Dataflow pipeline**

![image-20230915122156493](https://p.ipic.vip/xhqv0s.png)

1. YourProgram,exe image1.rgb 1 1 1 1.0 1.0 0

   No subsampling in the Y, U or V, and no scaling in w and h and no antialiasing, which implies that the output is the same as the input  

2. YourProgram,exe image1.rgb 1 1 1 0.5 0.5 1 

   No subsampling in Y, U or V, but the image is one fourth its original size (antialiased) 

3. YourProgram,exe image1.rgb 1 2 2 1.0 1.0 0 

   The output is not scaled in size, but the U and V channels are subsampled by 2. No subsampling in the Y channels. 

**Output Result**

1. image1.rgb 1 1 1 1.0 1.0 0

![image-20230915124857883](https://p.ipic.vip/ubsz8g.png)

2. image1.rgb 1 1 1 0.5 0.5 1 

![image-20230915234613860](https://p.ipic.vip/qcsnpc.png)

3. image1.rgb 1 2 2 1.0 1.0 0  

![image-20230915234508242](https://p.ipic.vip/t1i7u6.png)



#### Image segmentation
**mode 1:**
In mode 1, the foreground video utilizes a green screen, which might not have a consistent color due to various factors like lighting and noise during capture. It's crucial to set thresholds based on video analysis to detect these green screen pixels. The main task is to replace green screen pixels from the foreground with the corresponding pixels from the background in all frames.

**mode 0:**
In mode 0, there's no constant colored green screen in the foreground video. However, the videos provided will always have a moving element in each frame, with a static camera. The challenge is to identify "green screen" pixels by comparing two frames. Pixels that remain consistent between frames can be considered "green screen" pixels and should be replaced with corresponding background video pixels.

**Output Result**

mode1

![Kapture 2023-09-18 at 18.15.33](./4.gif)

mode2

![Kapture 2023-09-18 at 18.15.33](./5.gif)



#### DWT Compression

Input to your program will be 2 parameters where:
• The first parameter is the name of the input image rgb file.
• The second parameter n is an integral number from 0 to 9 that defines the low pass level to be used in your decoding. For a given n, this translates to using 2n low pass coefficients in rows and columns respectively to use in the decoding process . Additionally, n could also take a value of -1 to show progressive decoding. 

**Output Result**

![Kapture 2023-09-16 at 00.47.07](https://p.ipic.vip/vglgo1.gif)



#### **Generating novel videos assisted by Background-Foreground Panorama Analysis**

###### **Project Topic:**

The focus of this project is on block-based motion compensation, background/foreground detection, graphics transformations, creating panoramas by image stitching, etc. The core idea is to detect foreground/background regions and objects per frame to create intermediary data structures (like a panorama). This can then be used to create novel video applications.

###### **Algorithm and Processing:**

**Step 1: Detecting foreground and background macroblocks using motion compensation**

1. Divide each image frame into blocks of size 16x16 pixels.
2. Compute the Motion Vectors based on the previous frame.
3. Organize the blocks into background and foreground.

**Step 2: Creating background only plates and foreground objects**

For all the frames, zero out all the foreground objects, but keep track of them for future processing. The background plates with holes need to be composited to form the background panorama. Content is the holes in frame n will be filled in by the correct and continuous background content using the next step.![](https://p.ipic.vip/ro5xgd.png)

**Step 3: Creating a panorama for the background**

Next we need to choose an “anchor” frame to initialize your panorama. The anchor frame is typically first frame, last frame or middle frame. The background panorama is created by warping the neighborhood frames around the anchor frame and compositing the warped “missing” content at each step. This amounts to computing a transform from every frame to see how it fits into the panorama image.
a. If the camera has moved horizontally or vertically, this transform may be approximated as a translation matrix. This may be true for some of the given datasets.
b. If the camera has rotated about is pivot, this transform may be approximated as a rotation matrix. This may be true for some of the given datasets.
c. In general, given the various degrees of freedom (T, R, S and perspective changes), this transform is best approximated as a 3x3 perspective transform (homography)

![image-20230918173142576](https://p.ipic.vip/fxafj1.png)

##### Inputs, Intermediary Outputs, and Final Outputs

- **Input**: Videos (mp4 or rgb sequence) following the naming convention `videoname_width_height_numofframes`.
- **Intermediary Outputs:**
  1. Generated panorama of the background with foreground elements removed.
  2. Foreground macroblock elements or objects with coordinates on a frame-by-frame basis.
- **Application Outputs**:
  Applications like display motion trails, creating a new video, and removing objects from the video.

![image-20230918162013558](https://p.ipic.vip/9pn5rl.png)

![task1](https://p.ipic.vip/3roek4.jpg)

![Kapture 2023-09-18 at 16.46.28](https://p.ipic.vip/8orv3i.gif)

![Alternative Text](./3.gif)


![Alternative Text](./2.gif)

