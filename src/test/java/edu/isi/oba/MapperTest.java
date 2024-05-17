package edu.isi.oba;

import edu.isi.oba.config.AuthConfig;
import edu.isi.oba.config.CONFIG_FLAG;
import edu.isi.oba.config.YamlConfig;
import static edu.isi.oba.Oba.logger;
import static edu.isi.oba.ObaUtils.get_yaml_data;

import io.swagger.v3.oas.models.media.Schema;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;

public class MapperTest {
    @Test
    public void testFilter() throws Exception{
        String config_test_file_path = "src/test/config/dbpedia.yaml";
        YamlConfig config_data = get_yaml_data(config_test_file_path);
        Mapper mapper = new Mapper(config_data);
        Set<String> config = config_data.getClasses();
        Set<OWLClass> classes = mapper.filter_classes();
        Set<String> filter_classes = new HashSet<>();
        for (OWLClass _class : classes){
            filter_classes.add(_class.getIRI().getIRIString());
        }

        Assertions.assertEquals(config, filter_classes);
    }
    
    /**
     * This test attempts to load a local ontology.
     * @throws java.lang.Exception
     */
    @Test
    public void testLocalFile() throws Exception{
        String local_ontology = "src/test/config/mcat_reduced.yaml";
        YamlConfig config_data = get_yaml_data(local_ontology);
        Mapper mapper = new Mapper(config_data);
        Assertions.assertEquals(false, mapper.ontologies.isEmpty());
    }

    /**
     * This test attempts to load a config in a folder with spaces.
     * @throws java.lang.Exception
     */
    @Test
    public void testSpacesInPath() throws Exception{
        String local_ontology = "examples/example with spaces/config.yaml";
        YamlConfig config_data = get_yaml_data(local_ontology);
        Mapper mapper = new Mapper(config_data);
        Assertions.assertEquals(false, mapper.ontologies.isEmpty());
    }
    
    /**
     * This test attempts to run OBA with an online ontology through a URI.
     * The ontology is hosted in GitHub, but there is a small risk of the test
     * not passing due to the unavailability of the ontology.
     * @throws java.lang.Exception
     */
    @Test
    public void testRemoteOntology() throws Exception{
        String example_remote = "src/test/config/pplan.yaml";
        YamlConfig config_data = get_yaml_data(example_remote);
        Mapper mapper = new Mapper(config_data);
        Assertions.assertEquals(false, mapper.ontologies.isEmpty());
        
    }

    /**
     * Test an ontology (very simple, two classes) with a missing import
     */
    @Test
    public void testMissingImportOntology() throws Exception{
        String example_remote = "src/test/resources/missing_import/config.yaml";
        YamlConfig config_data = get_yaml_data(example_remote);
        Mapper mapper = new Mapper(config_data);
        Assertions.assertEquals(false, mapper.ontologies.isEmpty());
    }

    /**
     * Test an ontology (very simple, two classes) with a missing import
     */
    @Test
    public void testComplexOntology() throws Exception{
        InputStream stream = Oba.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            logger = Logger.getLogger(Oba.class.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.setLevel(Level.FINE);
        logger.addHandler(new ConsoleHandler());
        String example_remote = "src/test/resources/complex_expr/config.yaml";
        YamlConfig config_data = get_yaml_data(example_remote);
        config_data.setAuth(new AuthConfig());
        Mapper mapper = new Mapper(config_data);
        OWLClass cls = mapper.manager.getOWLDataFactory().getOWLClass("https://businessontology.com/ontology/Person");
        String desc = ObaUtils.getDescription(cls, mapper.ontologies.stream().findFirst().get(), true);
        MapperSchema mapperSchema = new MapperSchema(mapper.ontologies, cls, desc, mapper.schemaNames, mapper.ontologies.stream().findFirst().get(), Map.ofEntries(Map.entry(CONFIG_FLAG.DEFAULT_DESCRIPTIONS, true), Map.entry(CONFIG_FLAG.DEFAULT_PROPERTIES, true), Map.entry(CONFIG_FLAG.FOLLOW_REFERENCES, true)));
        Schema schema = mapperSchema.getSchema();
        // The person schema must not be null.
        Assertions.assertNotNull(schema);
        Assertions.assertEquals(schema.getName(),"Person");
    }
}
