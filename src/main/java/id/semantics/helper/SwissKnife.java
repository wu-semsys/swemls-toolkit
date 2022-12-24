package id.semantics.helper;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;

public class SwissKnife {

    public static File getFileFromResource(String fileName) {
        return new File(SwissKnife.class.getClassLoader().getResource(fileName).getFile());
    }

    public static Model initAndLoadModelFromURL(String dataModelURL, Lang lang) throws FileNotFoundException {
        Model dataModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        dataModel.read(dataModelURL, null);
        return dataModel;
    }

    public static Model initAndLoadModelFromResource(String dataModelFile, Lang lang) throws FileNotFoundException {
        InputStream dataModelIS = new FileInputStream(dataModelFile);
        Model dataModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        RDFDataMgr.read(dataModel, dataModelIS, lang);
        return dataModel;
    }

    public static Model initAndLoadModelFromResourceWithInference(String dataModelFile, Lang lang) throws FileNotFoundException {
        InputStream dataModelIS = new FileInputStream(dataModelFile);
        Model dataModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        RDFDataMgr.read(dataModel, dataModelIS, lang);
        return dataModel;
    }

    public static Model initAndLoadModelFromFilesInFolder(String folder, Lang lang) {
        File[] files = new File(folder).listFiles();
        Model dataModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        for (File f : files) {
            dataModel.add(RDFDataMgr.loadModel(f.getAbsolutePath(), lang));
        }

        return dataModel;
    }
}
