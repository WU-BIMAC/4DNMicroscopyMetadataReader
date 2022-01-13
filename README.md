<img align="right" src="https://github.com/WU-BIMAC/MicroMetaApp.github.io/blob/master/images/Nature%20Methods_COVER.png">

This software is a **Micro-Meta App** dependency, which was developed as part of a **global community initiative** including the **[4D Nucleome Imaging Working Group](https://www.4dnucleome.org/)**, **[BINA Quality Control and Data Management Working Group](https://www.bioimagingna.org/qc-dm-wg)** and **[QUAREP-LiMi](https://quarep.org/)**. 

> **News!** The works of this **global community effort** resulted in multiple publications featured on a recent **Nature Methods FOCUS ISSUE** dedicated to **[Reporting and reproducibility in microscopy](https://www.nature.com/collections/djiciihhjh)**. 

> **Learn More!** For a thorought description of Micro-Meta App consult our recent **[Nature Methods](https://doi.org/10.1038/s41592-021-01315-z)** and **[BioRxiv.org](https://doi.org/10.1101/2021.05.31.446382)** publications!

**Background** If you are a newby and you want to learn more about the importannce of metadata and quality control to ensure full reproducibility, quality and scientific value in light microscopy, please take a look at our recently posted overview manuscript entitled **"A perspective on Microscopy Metadata: data provenance and quality control"**, which is available on [ArXiv.org](https://arxiv.org/abs/1910.11370).

# 4DN Microscopy Metadata Reader

This JAVA software uses the Bio-Formats library and the OME-XML metadata structure to accesses all the OME-compatible metadata present in the header of a user-selected image data file and maps it to the scalable [4DN-BINA-OME](https://zenodo.org/record/4710731) Microscopy Metadata specifications that extend the [OME Data Model](https://www.openmicroscopy.org/Schemas/Documentation/Generated/OME-2016-06/ome.html) to produce a temporary, Micro-Meta App-compatible JSON object that can be read by [Micro-Meta App](https://wu-bimac.github.io/MicroMetaApp.github.io/). The object is then passed on to the Micro-Meta App and read by the Manage Settings section of the App to pre-populate the corresponding metadata fields.

# Background information

For more information please refer to our recent publications:

1. **A perspective on Microscopy Metadata: data provenance and quality control.**
Maximiliaan Huisman, Mathias Hammer, Alex Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://arxiv.org/abs/1910.11370

2. **Towards community-driven metadata standards for light microscopy: tiered specifications extending the OME model.**
Mathias Hammer, Maximiliaan Huisman, Alessandro Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, Farzin Farzam, Carlas Smith, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://www.biorxiv.org/content/10.1101/2021.04.25.441198v1. doi: https://doi.org/10.1101/2021.04.25.441198





