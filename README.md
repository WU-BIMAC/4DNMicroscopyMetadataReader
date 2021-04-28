# 4DN Microscopy Metadata Reader

This software is written in JAVA to fully take advantage of the Bio-Formats library and the OME-XML metadata structure. Using these two dependencies, this software accesses all the OME-compatible metadata present in a user selected images and maps it to the 4DN-BINA-OME Data Model to produce a temporary, Micro-MetaApp-compatible JSON object that can be read by Micro-Meta App. The object is then passed on to the Micro-Meta App and read by the Manage Settings section of the App to pre-polulate the corresponding metadata fields.





