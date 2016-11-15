import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.benchmark.byTask.feeds.TrecDocParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class indexComparison {

     public static void main(String[] args) throws IOException {
        //Alternative
      StringBuilder textBuff = new StringBuilder();
       indexComparison c = new indexComparison();

       String corpusPath = "C:\Users\user\Desktop\corpus";
        File documentDir = new File(corpusPath);
        int total_count = 0;

        if (!documentDir.exists() || !documentDir.canRead()) {
            System.out.println("Document directory '" +documentDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
	}else{
            File[] dirList = documentDir.listFiles();
             if (dirList != null) {
                for (File child : dirList) {
                    if(c.checkFile(child)){

                        StringBuilder stringBuff = new StringBuilder();
                        String line = null;
                        BufferedReader reader = new BufferedReader( new FileReader (child.getAbsolutePath()));
                        while( ( line = reader.readLine() ) != null ) {
                            stringBuff.append(line );
                        }
                        String TEXT = TrecDocParser.extract(stringBuff, "<TEXT>","</TEXT>" ,50000 , null);
                        textBuff.append(TEXT + "\t");
                    }
                }
        
        //Total terms in text field
                 for (Object eachT : textBuff.toString().split("\\s+")) {
                     total_count = total_count+1;
                 }
                 System.out.println("total count>>"+total_count);
                
                
                 Analyzer analyzer = null;         
        //Keyword Analyzer
        analyzer= new KeywordAnalyzer();        
        stringTokenize(analyzer, textBuff.toString(), "Keyword");
        
        //Simple Analyzer
        analyzer= new SimpleAnalyzer();        
        stringTokenize(analyzer, textBuff.toString(), "Simple");
        
        //Simple Analyzer
        analyzer= new StopAnalyzer();        
        stringTokenize(analyzer, textBuff.toString(), "Stop");
        
        //Stop Analyzer
        analyzer= new StandardAnalyzer();        
        stringTokenize(analyzer, textBuff.toString(), "Standard");
        

            }
        }
                 
   }
     
  public static List stringTokenize(Analyzer analyzer, String str, String type) {
    List result = new ArrayList();
    int count = 0;
    int unique_count = 0;
    try {
      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
      stream.reset();
      while (stream.incrementToken()) {
        if(stream.getAttribute(CharTermAttribute.class).toString()!= null){
            result.add(stream.getAttribute(CharTermAttribute.class).toString());
            count = count + 1;

        }
      }
      for (Object term : result) {
             String eachTerm = (String) term;
             for (Object term1 : eachTerm.split("\t")) {
                 if(!result.get(0).equals(term1)){
                     unique_count += 1;
                 }
             }
         }
      System.out.println("Number of tokens for analyzer "+type+" count: " + count);



    } catch (IOException e) {
     
      throw new RuntimeException(e);
    }
    return result;
  }
  
    private boolean checkFile(File pathname) {
      return pathname.getName().toLowerCase().endsWith(".trectext");
   }
  
}
