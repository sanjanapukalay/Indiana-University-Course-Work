import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;


public class JenaReasonerAssingment {
	


	private static String fnameschema = "/Users/Sanjana/SA/rdfsSchema.rdf";
	private static String fnameinstance = "/Users/Sanjana/SA/rdfsData.rdf";
	
	private static String NS = "urn:x-hp:eg/";
	
	public static void main(String args[]) {

	Model schema = FileManager.get().loadModel(fnameschema);
	Model data = FileManager.get().loadModel(fnameinstance);
	InfModel infmodel = ModelFactory.createRDFSModel(schema, data);
        
	Resource sanjana = infmodel.getResource(NS+"sanjana");
	System.out.println("sanjana has types:");
	for (StmtIterator i = infmodel.listStatements(sanjana, RDF.type , (RDFNode)null); i.hasNext(); ) {
        Statement s = i.nextStatement();
        System.out.println(s); } 
	
	Resource pukalay = infmodel.getResource(NS+"pukalay");
	System.out.println("pukalay has types:");
	for (StmtIterator i = infmodel.listStatements(pukalay, RDF.type , (RDFNode)null); i.hasNext(); ) {
        Statement s = i.nextStatement();
        System.out.println(s); } 
	
	Resource Person = infmodel.getResource(NS+"Person");
	System.out.println("\nPerson has types:");
	for (StmtIterator i = infmodel.listStatements(Person, RDF.type, (RDFNode)null); i.hasNext(); ) {
        Statement s = i.nextStatement(); 
        System.out.println(s);} 

	}


}
