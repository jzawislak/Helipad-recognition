# Helipad-recognition
Image recognition application.
Recognizes *helipad* signs, that is H inside a circle.

##Basic functionality
In the main window user is able to perform such image operations:

* Filtering
    * Low pass
    * High pass
    * Various Edge detecting
    * Ranking filters
* Segmentation
    * Black and white
    * On colorful images
* Calculating multiple shape descriptors

To accumulate different operations:

 * perform first operation
 * change default image File->Set as default
 * perform second operation
 * ... and so on

##Detecting helipads
There are also implemented two full paths for helipad detection.

###First algorithm steps are as follows:

1. Detect H signs.
    1. Threshold (leave white).
    2. Perform segmentation.
    3. Calculate shape descriptors.
    4. Filter H signs based on calculated shape descriptors.
2. Detect circle signs. The same algorithm as for H sign. The only difference is leaving yellow instead of white.
3. Combine H and circle together and filter only pairs with close centers.

###Second algorithm steps are as follows:

1. Apply multiple times 3x3 average, low pass filtering (usually between 1 and 3 times).
2. Segmentation on colorful image.
3. Calculate shape descriptors (only once).
4. Detect H signs based on calculated shape descriptors.
5. Detect circles based on calculated shape descriptors.
6. Combine H and circle together and filter only pairs with close centers.

Second algorithm is much slower because segmentation on colorful image produces much more segments.