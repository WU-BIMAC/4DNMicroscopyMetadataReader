# 4DN Microscopy Metadata Reader

This JAVA software uses the Bio-Formats library and the OME-XML metadata structure to accesses all the OME-compatible metadata present in a user selected images and maps it to the the scalable [4DN-BINA-OME](https://zenodo.org/record/4710731) Microscopy Metadata specifications that extend the [OME Data Model](https://www.openmicroscopy.org/Schemas/Documentation/Generated/OME-2016-06/ome.html) to produce a temporary, Micro-MetaApp-compatible JSON object that can be read by Micro-Meta App. The object is then passed on to the Micro-Meta App and read by the Manage Settings section of the App to pre-popululate the corresponding metadata fields.

# Background information

For more information please refer to our recent publications:

1. **A perspective on Microscopy Metadata: data provenance and quality control.**
Maximiliaan Huisman, Mathias Hammer, Alex Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://arxiv.org/abs/1910.11370

2. **Towards community-driven metadata standards for light microscopy: tiered specifications extending the OME model.**
Mathias Hammer, Maximiliaan Huisman, Alessandro Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, Farzin Farzam, Carlas Smith, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://www.biorxiv.org/content/10.1101/2021.04.25.441198v1. doi: https://doi.org/10.1101/2021.04.25.441198





