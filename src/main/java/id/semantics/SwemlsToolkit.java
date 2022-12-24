package id.semantics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import id.semantics.helper.SwissKnife;

/**
 * A simple app to help the generation of SWeMLS-KG
 */
public class SwemlsToolkit {

    private static final Logger log = LoggerFactory.getLogger(SwemlsToolkit.class);

    public static void main(String[] args) throws FileNotFoundException {

        String ontologyFile = "https://w3id.org/semsys/ns/swemls/ontology.ttl";
        
        String inputFolder = "input/";
        // ... File "input/swemls-patterns.ttl" is an aggregation of all patterns in the "input/patterns" folder
        String inputPatterns = inputFolder + "swemls-patterns.ttl";
        // ... File "input/swemls-shapes.ttl" is an aggregation of all SHACL constraints & rules in the "input/shapes" folder
        String inputShapes = inputFolder + "swemls-shapes.ttl";
        // ... File "swemls-instances.ttl" is the RDF Graph representation of SWeMLS metadata extracted from the SMS process
        String inputInstances = inputFolder + "swemls-instances.ttl";

        String outputFolder = "output/";
        String outputEnhancements = outputFolder + "enrichments-results.ttl";
        String outputSwemlsKG = outputFolder + "swemls-kg.ttl";
        String outputValidationReport = outputFolder + "validation-reports.ttl";

        Stopwatch timer = Stopwatch.createStarted();
        log.info("SWeMLS-KG creation started");
        log.info("....");

        // load SHACL constraints for patterns
        Model shapesGraph = SwissKnife.initAndLoadModelFromResource(inputShapes, Lang.TURTLE);

        // load data & ontology
        Model ontoGraph = SwissKnife.initAndLoadModelFromURL(ontologyFile, Lang.TURTLE);
        Model dataGraph = SwissKnife.initAndLoadModelFromResourceWithInference(inputInstances, Lang.TURTLE);
        dataGraph.add(ontoGraph);
        log.info("loading shapes, instances and ontology finished in " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");

        // initialise inference graphs for rule generation and validation
        Model inferenceGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        inferenceGraph.add(dataGraph);

        // executing SHACL-AF rules for generating workflow and write the results to an output file
        Model ruleResults = RuleUtil.executeRules(inferenceGraph, shapesGraph, inferenceGraph, null);
        log.info("executing SHACL-AF rules finished in " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");

        // ## Uncomment these lines if you want to only taking the generated triples based on SHACL-AF by calculating the diff; 
        // ## ... unfortunately it takes a lot of time (~15 minutes) since we're relying on Jena diff method
        // ruleResults = ruleResults.difference(dataGraph);
        // ruleResults.setNsPrefixes(ontoGraph.getNsPrefixMap());
        // RDFDataMgr.write(new FileOutputStream(outputEnhancements), ruleResults, Lang.TURTLE);
        // log.info("calculating the diff between rule execution results and original data finished in " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");
        

        // execute SHACL constraints on dataGraphs
        Resource validationResult = ValidationUtil.validateModel(inferenceGraph, shapesGraph, false);
        log.info("SHACL data validation finished in " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");

        // write validation report and set SHACL namespace
        Model shaclValidationReport = validationResult.getModel();
        shaclValidationReport.setNsPrefix(SH.PREFIX, SH.NS);

        // check if the validation successful
        if (shaclValidationReport.containsLiteral(null, SH.conforms, false)) {
            System.out.println("Validation Failed! See validation results at " + outputValidationReport);

        // generate final knowledge graphs
        } else {
            // load the representation of SWeMLS patterns to be added to the final KG
            Model patternGraph = SwissKnife.initAndLoadModelFromResource(inputPatterns, Lang.TURTLE);

            // create an integrated KG
            Model finalKG = ModelFactory.createDefaultModel();
            finalKG.add(SwissKnife.initAndLoadModelFromResource(inputInstances, Lang.TURTLE));
            finalKG.add(ontoGraph);
            finalKG.add(ruleResults);
            finalKG.add(patternGraph);

            // save to file
            RDFDataMgr.write(new FileOutputStream(outputSwemlsKG), finalKG, Lang.TURTLE);
            log.info("SWeMLS-KG creation is finished in " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms");
            log.info("....");
            log.info("Validation Successful! See completed KG at " + outputSwemlsKG);
        }

        RDFDataMgr.write(new FileOutputStream(outputValidationReport), shaclValidationReport, Lang.TURTLE);
    }
}
