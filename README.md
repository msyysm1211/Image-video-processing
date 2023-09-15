# Multimedia Systems Design

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

![image-20230915122156493](/Users/mashengyuan/Library/Application Support/typora-user-images/image-20230915122156493.png)

1. YourProgram,exe image1.rgb 1 1 1 1.0 1.0 0

   No subsampling in the Y, U or V, and no scaling in w and h and no antialiasing, which implies that the output is the same as the input  

2. YourProgram,exe image1.rgb 1 1 1 0.5 0.5 1 

   No subsampling in Y, U or V, but the image is one fourth its original size (antialiased) 

3. YourProgram,exe image1.rgb 1 2 2 1.0 1.0 0 

   The output is not scaled in size, but the U and V channels are subsampled by 2. No subsampling in the Y channels. 

**Output Result**

1. image1.rgb 1 1 1 1.0 1.0 0

![image-20230915124857883](/Users/mashengyuan/Library/Application Support/typora-user-images/image-20230915124857883.png)

2. image1.rgb 1 1 1 0.5 0.5 1 
3. image1.rgb 1 2 2 1.0 1.0 0  



