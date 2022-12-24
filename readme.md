# SWeMLS Toolkit

This small app allows you to enhance and validate your SWeMLS metadata according to SWeMLS ontology and patterns. 
More specifically, we uses the reference implementation of [SHACL Engine](https://github.com/TopQuadrant/shacl) to    
* augment SWeMLS metadata acquired from  with workflows using SHACL-AF rules.    
* validate instances of SWeMLS against SHACL constraints according to their pattern.

The SWeMLS metadata is available in `input/swemls-instances.ttl`, which is structured according to the [SWeMLS ontology](https://w3id.org/semsys/ns/swemls/).
Furthermore, both SHACL-AF rules and SHACL constraints are available in folder `input/shapes`, while the SWeMLS patterns are available in folder `input/patterns`.

The SWeMLS toolkit is part of the resources available for our ESWC 2023 submission entitled "_Describing and Organizing Semantic Web and Machine Learning Systems in the SWeMLS-KG_".

## Instructions

Prerequisite: 
* Java 11 and Maven 3 installation

Build your maven project:  
* open the project in an IDE (e.g., IntelliJ or Eclipse)
* build the application using the following command: `mvn clean install`
* run the application with the following command:  `java -jar target/swemls-toolkit-0.1.0-jar-with-dependencies.jar`

The result of rule executions and validation will be saved in the `output` folder as the following: 
* SHACL-AF rules results: `output/enrichment-results.ttl`
* SHACL-constraints validation results: `validation-reports.ttl`
* Output KG (if validation successful): `SWeMLS-KG.ttl`

