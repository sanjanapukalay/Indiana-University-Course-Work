
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.TrecDocParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;


public class generateIndex {

    public static void main(String[] args) throws IOException {
  
        String indexPath = "C:\Users\user\Desktop\index";
        Directory dir = FSDirectory.open(Paths.get (indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        String docsPath = "C:\Users\user\Desktop\corpus";
        File docDir = new File(docsPath);
        generateIndex genIndex = new generateIndex();
        IndexWriter writer = new IndexWriter(dir, iwc);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
	}else{
            File[] files = docDir.listFiles();
             if (files != null) {
                for (File child : files) {
                    if(genIndex.checkFile(child)){
                        StringBuilder stringBuff = new StringBuilder();
                        String line = null;
                        BufferedReader reader = new BufferedReader( new FileReader (child.getAbsolutePath()));
                        while( ( line = reader.readLine() ) != null ) {
                            stringBuff.append(line );
                        }


                        String DOCNO = TrecDocParser.extract(stringBuff, "<DOCNO>","</DOCNO>" ,50000 , null);
                        String HEAD = TrecDocParser.extract(stringBuff, "<HEAD>","</HEAD>" ,50000 , null);
                        String BYLINE = TrecDocParser.extract(stringBuff, "<BYLINE>","</BYLINE>" ,50000 , null);
                        String DATELINE = TrecDocParser.extract(stringBuff, "<DATELINE>","</DATELINE>" ,50000 , null);
                        String TEXT = TrecDocParser.extract(stringBuff, "<TEXT>","</TEXT>" ,50000 , null);
                        

                        Document luceneDocument = new Document();
                        luceneDocument.add(new StringField("DOCNO", DOCNO,Field.Store.YES));
                        luceneDocument.add(new TextField("HEAD", HEAD,Field.Store.YES));
                        luceneDocument.add(new TextField("BYLINE", BYLINE,Field.Store.YES));
                        luceneDocument.add(new TextField("DATELINE", DATELINE,Field.Store.YES));
                        luceneDocument.add(new TextField("TEXT", TEXT,Field.Store.YES));
                        writer.addDocument(luceneDocument);
                    }
                }

            }
        }
        
       writer.close();

       IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get((indexPath))));
       
      System.out.println("Total number of documents in the corpus:"+reader.maxDoc());
       
       System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
       
       System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
       Terms vocab = MultiFields.getTerms(reader, "TEXT");
       
       System.out.println("Size of the vocabulary for this field: "+vocab.size());
       
       System.out.println("Number of documents that have at least one term for this field: "+vocab.getDocCount());
       
       System.out.println("Number of tokens for this field: "+vocab.getSumTotalTermFreq());
       
       System.out.println("Number of postings for this field: "+vocab.getSumDocFreq());
       
       TermsEnum iterator = vocab.iterator();
       BytesRef byteRef = null;
       System.out.println("\n******* Printing Vocabulary-Start**********");
       while((byteRef = iterator.next()) != null) {
           String term = byteRef.utf8ToString();
           System.out.print(term+"\t");
       }
       System.out.println("\n*******Printing Vocabulary-End**********");
       reader.close(); 
  
             
    }
    
    private boolean checkFile(File pathname) {
      return pathname.getName().toLowerCase().endsWith(".trectext");
   }
    
 
}