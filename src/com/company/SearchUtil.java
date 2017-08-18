package com.company;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by neal1 on 2017/8/18.
 */
public class SearchUtil {
    public static final Analyzer analyzer = new IKAnalyzer();
    // obtain IndexSearcher Object
    public static IndexSearcher getIndexSearcher(String indexPath, ExecutorService service, boolean realtime) throws IOException, InterruptedIOException {
        DirectoryReader reader = DirectoryReader.open()
    }
}
