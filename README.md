# ChiffonUpdater Project
Welcome to **ChiffonUpdater**! This project is an open-source effort to provide software editors as well as end-users the best experience
in terms of updating their products. This project also allows end-users to rollback products to a previous version.

This project is entirely released under the *MIT License*

For more informations, please [check out the website on this link](https://explodingbottle.github.io/ChiffonUpdaterProject/).

*This project includes*:
- A toolkit allowing software editors to manage their update infrastructure
- A website with an agent which will run on the end-user computer to provide a graphical update flow
- The hearth of the system, the standalone update package binary which processes the updates and allow rolling back

*This repository contains*:

| Folder name                 | Content                                                            |
|:----------------------------|:-------------------------------------------------------------------|
| ChiffonUpdaterAgent         | This is the code of the agent component, which deals with detecting updates and running update packages |
| ChiffonUpdaterDemoBatch      | This is an example which teaches how a non-Java program can register itself to the products list |
| ChiffonUpdaterDemoModule      | This is an example which teaches how to write a module for the standalone update package |
| ChiffonUpdaterExternalLibrary      | This is the code of the external library, which does internal calls to register a product to the products list |
| ChiffonUpdaterSelfExtract | This is the code of the self-extractor, which is used while building an update package |
| ChiffonUpdaterShared | This is the code of an internal shared library which is used nearly everywhere in this project |
| ChiffonUpdaterSharedStaticModule | This is the code of the default module embedded inside the standalone update package |
| ChiffonUpdaterStandalonePackage | This is the code of the standalone update package, which handles updating and rolling back products |
| ChiffonUpdaterStandaloneSDK | This is the code of the SDK, which allows easy creation of modules for the update package |
| ChiffonUpdaterToolkit | This is the code of the Toolkit which allows a software editor to manage their update infrastructure |
| ChiffonUpdaterWebsite | This is the code of the update website, which talks to the agent and is used by the end-user |
| ChiffonUpdaterWebsiteXCF | This folder contains the XCF files used for making icons for the website |

**All the Java projects above are compilable with Eclipse**

# Used Java version
The Java version used for this project is **Java 8 update 202** (this release will always be used for binaries and coding to avoid creating a blocking point around the Java license)
