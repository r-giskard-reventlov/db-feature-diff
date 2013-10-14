package org.rgiskard.script_build.dao;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import org.rgiskard.script_build.jaxbentities.Features;

@Component
public class XmlDaoImpl implements XmlDao {

	private final File scriptPath;
	
	@Autowired
	public XmlDaoImpl(
			@Value("${project.path}") String projectPath, 
			@Value("${script.defaultPath}") String defaultProjectRelativeScriptPath, 
			@Value("${script.output}") String outputFile) {
		this.scriptPath = new File(projectPath + (defaultProjectRelativeScriptPath.isEmpty() ? "/src/main/resources/database" : defaultProjectRelativeScriptPath));
	}
	
	public Features features() {
		try {
			final File featureFile = new File(this.scriptPath + File.separator + "features.xml");
			final JAXBContext jaxbContext = JAXBContext.newInstance(Features.class);
	        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        final ClassPathResource cpr = new ClassPathResource("schemas/features.xsd");
	        final Schema schema = factory.newSchema(new StreamSource(cpr.getInputStream()));
	        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	        jaxbUnmarshaller.setSchema(schema);
	        Logger.getLogger(XmlDaoImpl.class.getName()).log(Level.INFO, "Looking for features.xml file [" + featureFile.getAbsolutePath() + "].");
			return (Features) jaxbUnmarshaller.unmarshal(featureFile);
		} 
		catch (JAXBException e) {
			Logger.getLogger(LogDaoImpl.class.getName()).log(Level.INFO, "Could not find features.xml in location [" + this.scriptPath + "].");
			throw new RuntimeException("Could not load features.xml file. " + e.getMessage());
		} catch (SAXException e) {
			Logger.getLogger(LogDaoImpl.class.getName()).log(Level.INFO, "Could not find features.xml in location [" + this.scriptPath + "].");
			throw new RuntimeException("Could not load features.xml file. " + e.getMessage());
		} catch (IOException e) {
			Logger.getLogger(LogDaoImpl.class.getName()).log(Level.INFO, "Could not find features.xml in location [" + this.scriptPath + "].");
			throw new RuntimeException("Could not load features.xml file. " + e.getMessage());
		}
	}

}
