package com.company;

/**
 * Created by neal1 on 2017/8/18.
 */
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public class TestSearch {
    private static final String indexPath = "C:\\\\Users\\\\neal1\\\\project\\\\lucene-test\\\\indexedFiles";
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        //IndexSearcher searcher = SearchUtil.getMultiSearcher("C:\\Users\\neal1\\project\\lucene-test\\indexedFiles", Executors.newCachedThreadPool(), false);
        IndexSearcher searcher = SearchUtil.getSearcher(indexPath);
        Query query = SearchUtil.getQuery("contents", "string", "test", false);
//        Query phoneQuery = SearchUtil.getRegexExpQuery("content", "1[0-9]{10}");
//        Query mailQuery = SearchUtil.getRegexExpQuery("content", "([a-z0-9A-Z]+[-_|\\.]?)+[a-z0-9A-Z]*@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}");
//        Query finaQuery = SearchUtil.getMultiQueryLikeSqlIn(new Query[]{phoneQuery,mailQuery});
//        TopDocs topDocs = SearchUtil.getScoreDocsByPerPageAndSortField(searcher, query, 0, 20, null);
        TopDocs topDocs = SearchUtil.getHits(searcher);
        System.out.println("符合条件的数据总数：" + topDocs.totalHits);
        System.out.println("本次查询到的数目为：" + topDocs.scoreDocs.length);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc.get("path") + "    " + doc.get("contents"));
            }
    }

}
