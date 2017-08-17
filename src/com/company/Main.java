package com.company;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {

    private static String INDEX_DIR = "C:\\index";

    public static void main(String[] args) throws Exception {
	// write your code here
//        IndexWriter writer = createWriter();
//        List<Document> documents = new ArrayList<>();
//        Document document1 = createDocument(1, "Lokesh", "Gupta", "asdasdasd.com");
//        documents.add(document1);
//        Document document2 = createDocument(2, "Brian", "Schultz", "example.com");
//        documents.add(document2);

        String docsPath = "inputFiles";
        String indexPath = "indexedFiles";

        Path docDir = Paths.get(docsPath);


        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig();
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        if (writer != null) {
            writer.deleteAll();
        }

        indexDocs(writer, docDir);
        writer.close();

//        Directory dir = FSDirectory.open(Paths.get(indexPath));

        IndexReader reader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser qp = new QueryParser("contents", analyzer);

        Query query = qp.parse("begin");

        TopDocs hits = searcher.search(query, 10);

        Formatter formatter = new SimpleHTMLFormatter();

        QueryScorer scorer = new QueryScorer(query);

        Highlighter highlighter = new Highlighter(formatter, scorer);

        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);
        highlighter.setTextFragmenter(fragmenter);

        for (int i = 0; i < hits.scoreDocs.length; i ++) {
            int docid = hits.scoreDocs[i].doc;
            Document doc = searcher.doc(docid);
            String title = doc.get("path");

            System.out.println("Path " + " : " + title);

            String text = doc.get("contents");

            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            String[] frags = highlighter.getBestFragments(stream, text, 10);

            for (String frag : frags) {
                System.out.println("=========================");
                System.out.println(frag);
            }
        }
        dir.close();



//        writer.deleteAll();
//        writer.addDocuments(documents);
//        writer.commit();
//        writer.close();

//        IndexSearcher searcher = createSearcher();
//
//        // search by ID
//        TopDocs foundDocs = searchById(1, searcher);
//
//        System.out.println("Total Results:" + foundDocs.totalHits);
//
//        for (ScoreDoc sd : foundDocs.scoreDocs) {
//            Document d = searcher.doc(sd.doc);
//            System.out.println(String.format(d.get("firstName")));
//        }
//
//        // search by firstName
//        TopDocs foundDocs1 = searchByFirstName("Brian", searcher);
//        System.out.println("Total Results:" + foundDocs1.totalHits);
//
//        for (ScoreDoc sd : foundDocs1.scoreDocs) {
//            Document d = searcher.doc(sd.doc);
//            System.out.println(String.format(d.get("firstName")));
//        }
    }

    private static IndexWriter createWriter() throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);
        return writer;
    }

    private static Document createDocument(Integer id, String firstName, String lastName, String website) {
        Document document = new Document();
        document.add(new StringField("id", id.toString(), Field.Store.YES));
        document.add(new TextField("firstName", firstName, Field.Store.YES));
        document.add(new TextField("lastName", lastName, Field.Store.YES));
        document.add(new TextField("website", website, Field.Store.YES));
        return document;
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }

    private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("id", new StandardAnalyzer());
        Query idQuery = qp.parse(id.toString());
        TopDocs hits = searcher.search(idQuery, 10);
        return hits;
    }

    private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
        Query firstNameQuery = qp.parse(firstName);
        TopDocs hits = searcher.search(firstNameQuery, 10);
        return hits;
    }

    static void indexDocs(final IndexWriter writer, Path path) throws IOException
    {
        //Directory?
        if (Files.isDirectory(path))
        {
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    try
                    {
                        //Index this file
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else
        {
            //Index this file
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    private static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException
    {
        try (InputStream stream = Files.newInputStream(file))
        {
            //Create lucene Document
            Document doc = new Document();

            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Field.Store.YES));

            //Updates a document by first deleting the document(s)
            //containing <code>term</code> and then adding the new
            //document.  The delete and then add are atomic as seen
            //by a reader on the same index
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }


}
