package cranfield;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

public class QueryIndex
{

    // the location of the search index
    private static String INDEX_DIRECTORY = "index";

    // The location of the queries
    private static String QUERIES_DIRECTORY = "cranfield-collection/cran.qry";

    // The location of the search results
    private static String RESULTS_DIRECTORY = "results.txt";

    public static void main(String[] args) throws Exception
    {
        // Open the folder that contains our search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // create objects to read and search across the index
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        // Use BM25 scoring approach
        isearcher.setSimilarity(new BM25Similarity());

        // Analyzer that is used to process the queries
        // Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new EnglishAnalyzer();

        // Query parser suited to search multiple field
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "author", "bibliography", "words"}, analyzer);

        BufferedReader reader = new BufferedReader(new FileReader(QUERIES_DIRECTORY));
        PrintWriter writer = new PrintWriter(RESULTS_DIRECTORY, "UTF-8");
        System.out.println("Parsing queries and producing search results ...");

        // Read and process queries
        int index = 1;
        String currentLine = reader.readLine();
        while (currentLine != null) {
            String queryString = "";

            if (currentLine.startsWith(".I")) {
                currentLine = reader.readLine();
            }

            if (currentLine.startsWith(".W")) {
                currentLine = reader.readLine();

                while (currentLine != null && !currentLine.startsWith(".I") ) {
                    queryString += currentLine + " ";
                    currentLine = reader.readLine();
                }
            }

            Query query = parser.parse(QueryParser.escape(queryString));

            ScoreDoc[] hits = isearcher.search(query,  1400).scoreDocs;
            System.out.println("query " + index + ": " + hits.length + " hits");
            // Write search results to the results file
            for (int i = 0; i < hits.length; i++) {
                Document doc = isearcher.doc(hits[i].doc);
                writer.println(index + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " BM25");
            }

            index++;
        }

        // close everything we used
        ireader.close();
        directory.close();
        writer.close();

        System.out.println("Complete");
    }
}