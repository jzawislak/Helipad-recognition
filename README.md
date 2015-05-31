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
There is also implemented a full path for helipad detection.

Algorithm steps are as follows:

1. Detect H signs.
    1. Threshold (leave white).
    2. Perform segmentation.
    3. Calculate shape descriptors.
    4. Filter H signs based on calculated shape descriptors.
2. Detect circle signs. The same algorithm as for H sign. The only difference is leaving yellow instead of white.
3. Combine H and circle together and filter only pairs with close centers.