# SWeMLS Toolkit

This small app allows you to enhance and validate your SWeMLS-KG data according to workflow definitions. 
More specifically, we uses the reference implementation of [SHACL Engine](https://github.com/TopQuadrant/shacl) to    
* augment SWeMLS-KG with workflows using SHACL-AF rules.    
* to validate instances of SWeMLS against SHACL constraints according to their pattern.

Both SHACL-AF rules and SHACL constraints are available in folder (`input/patterns`).   

SWeMLS toolkit is part of the resources available for our ESWC 2023 submission entitled "_Describing and Organizing Semantic Web and Machine Learning Systems in the SWeMLS-KG_".

## Instructions

Build your maven project:  
* open the project in an IDE (e.g., IntelliJ or Eclipse)
* `mvn clean install`
* open and run the `src/main/java/SwemlsToolkit.java`

The result of rule executions and validation will be saved in the `output` folder.

