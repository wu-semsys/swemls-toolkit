package id.semantics.shacl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class SwemlsTookit {

    public static void main(String[] args) throws FileNotFoundException {

        String validationMessage = "validation failed";

        String patternsFolder = "input/patterns/";
        String shapesFolder = "input/shapes/";

        String swemlsPrefix = "swemls";
        String swemlsNS = "https://w3id.org/semsys/ns/swemls#";
        String date = "15122022";

        String rawSWeMLSdata = "input/data/pattern-instances-" + date + ".ttl";
        String swCompoundFile = "input/data/sw-compound-mapping-2911-0023.ttl";
        String mlCompoundFile = "input/data/ml-compound-mapping-2911-0011.ttl";
        String krCompoundFile = "input/data/kr-compound-mapping-2911-0011.ttl";

        String ontologyFile = "input/swemls-ontology-" + date + ".ttl";

        String outputFolder = "output/";
        String ruleExecutionResults = outputFolder + "rule_results-" + date + ".ttl";
        String constraintExecutionResults = outputFolder + "constraint_results-" + date + ".ttl";
        String kgOutput = outputFolder + "swemls-kg-" + date + ".ttl";

        // load SHACL constraints for patterns
        Model shapesGraph = SwissKnife.initAndLoadModelFromFilesInFolder(shapesFolder, Lang.TURTLE);

        // load data
        Model dataGraph = SwissKnife.initAndLoadModelFromResource(rawSWeMLSdata, Lang.TURTLE);

        // add ontology to dataGraph and set swemls prefix
        dataGraph.add(SwissKnife.initAndLoadModelFromResource(ontologyFile, Lang.TURTLE));
        dataGraph.setNsPrefix(swemlsPrefix, swemlsNS);

        // executing SHACL-AF rules for generating workflow and write the results to an
        // output file
        Model ruleResults = RuleUtil.executeRules(dataGraph, shapesGraph, dataGraph, null);
        RDFDataMgr.write(new FileOutputStream(ruleExecutionResults), ruleResults, Lang.TURTLE);

        // add rule execution results to datagraphs
        dataGraph.add(ruleResults);

        // execute SHACL constraints on dataGraphs
        Resource validationResult = ValidationUtil.validateModel(dataGraph, shapesGraph, false);

        // write validation report and set SHACL namespace
        Model shaclValidationReport = validationResult.getModel();
        shaclValidationReport.setNsPrefix(SH.PREFIX, SH.NS);

        // check if the validation successful
        if (shaclValidationReport.containsLiteral(null, SH.conforms, false)) {
            System.out.println(validationMessage);
            RDFDataMgr.write(new FileOutputStream(constraintExecutionResults), shaclValidationReport, Lang.TURTLE);

            // generate final knowledge graphs
        } else {

            // add pattern graph to data
            Model patternGraph = SwissKnife.initAndLoadModelFromFilesInFolder(patternsFolder, Lang.TURTLE);
            dataGraph.add(patternGraph);

            // add SW, KR & ML compounds to KG
            dataGraph.add(SwissKnife.initAndLoadModelFromResource(swCompoundFile, Lang.TURTLE));
            dataGraph.add(SwissKnife.initAndLoadModelFromResource(krCompoundFile, Lang.TURTLE));
            dataGraph.add(SwissKnife.initAndLoadModelFromResource(mlCompoundFile, Lang.TURTLE));

            // save to file
            RDFDataMgr.write(new FileOutputStream(kgOutput), dataGraph, Lang.TURTLE);
        }

    }
}
