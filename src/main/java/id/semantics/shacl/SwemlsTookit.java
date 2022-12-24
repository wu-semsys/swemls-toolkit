package id.semantics.shacl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class SwemlsTookit {

    public static void main(String[] args) throws FileNotFoundException {

        String ontologyFile = "https://w3id.org/semsys/ns/swemls/ontology.ttl";
        String outputFolder = "output/";
        String inputFolder = "input/";

        String patternsFolder = inputFolder + "patterns/";
        String shapesFolder = inputFolder + "shapes/";
        String dataFolder = inputFolder + "data/";

        String inputInstances = dataFolder + "pattern-instances-15122022.ttl";
        String inputSWCompounds = dataFolder + "sw-compound-mapping-2911-0023.ttl";
        String inputMLCompounds = dataFolder + "ml-compound-mapping-2911-0011.ttl";
        String inputKRCompounds = dataFolder + "kr-compound-mapping-2911-0011.ttl";

        String combinedInstancesOutput = outputFolder + "swemls-sms-metadata.ttl";
        String inferredInstancesOutput = outputFolder + "generated-statements.ttl";
        String shapesOutput = outputFolder + "swemls-shapes.ttl";
        String enhancementOutput = outputFolder + "enrichments-results.ttl";
        String swemlsKGOutput = outputFolder + "SWeMLS-KG.ttl";

        String validationReport = outputFolder + "validation-reports.ttl";

        // load SHACL constraints for patterns
        Model shapesGraph = SwissKnife.initAndLoadModelFromFilesInFolder(shapesFolder, Lang.TURTLE);
        RDFDataMgr.write(new FileOutputStream(shapesOutput), shapesGraph, Lang.TURTLE);

        // load data & ontology
        Model ontoGraph = SwissKnife.initAndLoadModelFromURL(ontologyFile, Lang.TURTLE);
        Model dataGraph = SwissKnife.initAndLoadModelFromResource(inputInstances, Lang.TURTLE);

        // add all compound files to datagraph
        dataGraph.add(SwissKnife.initAndLoadModelFromResource(inputSWCompounds, Lang.TURTLE));
        dataGraph.add(SwissKnife.initAndLoadModelFromResource(inputKRCompounds, Lang.TURTLE));
        dataGraph.add(SwissKnife.initAndLoadModelFromResource(inputMLCompounds, Lang.TURTLE));
        RDFDataMgr.write(new FileOutputStream(combinedInstancesOutput), dataGraph, Lang.TURTLE);

        // initialise inference graphs for validation
        Model inferenceGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        inferenceGraph.add(ontoGraph);
        inferenceGraph.add(dataGraph);
        inferenceGraph.setNsPrefixes(ontoGraph.getNsPrefixMap());
        RDFDataMgr.write(new FileOutputStream(inferredInstancesOutput), inferenceGraph, Lang.TURTLE);

        // executing SHACL-AF rules for generating workflow and write the results to an output file
        Model ruleResults = RuleUtil.executeRules(inferenceGraph, shapesGraph, inferenceGraph, null);
        RDFDataMgr.write(new FileOutputStream(enhancementOutput), ruleResults, Lang.TURTLE);

        // execute SHACL constraints on dataGraphs
        Resource validationResult = ValidationUtil.validateModel(inferenceGraph, shapesGraph, false);

        // write validation report and set SHACL namespace
        Model shaclValidationReport = validationResult.getModel();
        shaclValidationReport.setNsPrefix(SH.PREFIX, SH.NS);

        // check if the validation successful
        if (shaclValidationReport.containsLiteral(null, SH.conforms, false)) {
            System.out.println("Validation Failed! See validation results at " + validationReport);

        // generate final knowledge graphs
        } else {
            // load all paterns (and write it to a separate file)
            Model patternGraph = SwissKnife.initAndLoadModelFromFilesInFolder(patternsFolder, Lang.TURTLE);

            // create an integrated KG
            Model finalKG = ModelFactory.createDefaultModel();
            finalKG.add(dataGraph);
            finalKG.add(ruleResults);
            finalKG.add(patternGraph);

            // add all compound files
            finalKG.add(SwissKnife.initAndLoadModelFromResource(inputSWCompounds, Lang.TURTLE));
            finalKG.add(SwissKnife.initAndLoadModelFromResource(inputKRCompounds, Lang.TURTLE));
            finalKG.add(SwissKnife.initAndLoadModelFromResource(inputMLCompounds, Lang.TURTLE));

            // save to file
            RDFDataMgr.write(new FileOutputStream(swemlsKGOutput), finalKG, Lang.TURTLE);

            System.out.println("Validation Successful! See completed KG at " + swemlsKGOutput);
        }

        RDFDataMgr.write(new FileOutputStream(validationReport), shaclValidationReport, Lang.TURTLE);
    }
}
