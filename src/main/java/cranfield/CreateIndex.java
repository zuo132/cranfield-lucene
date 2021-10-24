package cranfield;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateIndex
{

    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "index";

    // Directory where the cranfield collection is
    private static String COLLECTION_DIRECTORY = "cranfield-collection/cran.all.1400";

    public static void main(String[] args) throws IOException
    {
        // Analyzer that is used to process TextField
        Analyzer analyzer = new StandardAnalyzer();

        // To store an index in memory
        // Directory directory = new RAMDirectory();
        // To store an index on disk
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Index opening mode
        // IndexWriterConfig.OpenMode.CREATE = create a new index
        // IndexWriterConfig.OpenMode.APPEND = open an existing index
        // IndexWriterConfig.OpenMode.CREATE_OR_APPEND = create an index if it
        // does not exist, otherwise it opens it
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter iwriter = new IndexWriter(directory, config);
        BufferedReader reader = new BufferedReader(new FileReader(COLLECTION_DIRECTORY));

        System.out.println("Indexing documents ...");

        // ArrayList of documents in the corpus
        ArrayList<Document> documents = new ArrayList<Document>();

        String currentLine = reader.readLine();
        while(currentLine != null) {
            // Create a new document
            Document doc = new Document();

            // Read and process each section of a document
            if (currentLine.startsWith(".I")) {
                doc.add(new TextField("id", currentLine.substring(3), Field.Store.YES));
                currentLine = reader.readLine();
            }

            if (currentLine.startsWith(".T")) {
                String title = "";
                currentLine = reader.readLine();

                while(!currentLine.startsWith(".A")) {
                    title += currentLine + " ";
                    currentLine = reader.readLine();
                }

                doc.add(new TextField("title", title, Field.Store.YES));
            }

            if (currentLine.startsWith(".A")) {
                String author = "";
                currentLine = reader.readLine();

                while(!currentLine.startsWith(".B")){
                    author += currentLine + " ";
                    currentLine = reader.readLine();
                }

                doc.add(new TextField("author", author, Field.Store.YES));
            }

            if (currentLine.startsWith(".B")) {
                String bibliography = "";
                currentLine = reader.readLine();

                while(!currentLine.startsWith(".W")){
                    bibliography += currentLine + " ";
                    currentLine = reader.readLine();
                }

                doc.add(new TextField("bibliography", bibliography, Field.Store.YES));
            }

            if (currentLine.startsWith(".W")) {
                String words = "";
                currentLine = reader.readLine();

                while(currentLine != null && !currentLine.startsWith(".I") ){
                    words += currentLine + " ";
                    currentLine = reader.readLine();
                }

                doc.add(new TextField("words", words, Field.Store.YES));
            }

            // Add the file to our linked list
            documents.add(doc);
        }

        // Write all the documents in the linked list to the search index
        iwriter.addDocuments(documents);

        // Commit changes and close everything
        iwriter.close();
        directory.close();

        System.out.println("Indexing complete");
    }
}
