package com.company;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import static org.apache.lucene.search.BooleanClause.*;
import static org.apache.lucene.search.SortField.Type;

/**
 * Created by neal1 on 2017/8/18.
 */
public class SearchUtil {
    public static final Analyzer analyzer = new IKAnalyzer();
    // obtain IndexSearcher Object
    public static IndexSearcher getIndexSearcher(String indexPath, ExecutorService service, boolean realtime) throws IOException, InterruptedIOException {
        // to-do the parameter realtime
        DirectoryReader reader = DirectoryReader.open(IndexUtil.getIndexWriter(indexPath, true), realtime, true);
        IndexSearcher searcher = new IndexSearcher(reader, service);
        if (service != null) {
            service.shutdown();
        }
        return searcher;
    }

    public static IndexSearcher getMultiSearcher(String parentPath, ExecutorService service, boolean realtime) throws IOException, InterruptedIOException {
        MultiReader multiReader;
        File file = new File(parentPath);
        File[] files = file.listFiles();
        IndexReader[] readers = new IndexReader[files.length];
        if (!realtime) {
            for (int i = 0; i < files.length; i ++) {
                readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(files[i].getPath(), new String[0])));
            }
        } else {
            for (int i = 0; i < files.length; i ++) {
                readers[i] = DirectoryReader.open(IndexUtil.getIndexWriter(files[i].getPath(), true), true, true);
            }
        }
        multiReader = new MultiReader(readers);
        IndexSearcher searcher = new IndexSearcher(multiReader, service);
        if (service != null) {
            service.shutdown();
        }
        return searcher;
    }

    public static Query getQuery(String field, String fieldType, String queryStr, boolean range) {
        Query q = null;
        if (queryStr != null && !"".equals(queryStr)) {
            if (range) {
                String[] strs = queryStr.split("\\|");
                if ("int".equals(fieldType)) {
                    int min = new Integer(strs[0]);
                    int max = new Integer(strs[1]);
                    q = LegacyNumericRangeQuery.newIntRange(field, min, max, true, true);
                } else if ("double".equals(fieldType)) {
                    Double min = new Double(strs[0]);
                    Double max = new Double(strs[1]);
                    q = LegacyNumericRangeQuery.newDoubleRange(field, min, max, true, true);
                } else if ("float".equals(fieldType)) {
                    Float min = new Float(strs[0]);
                    Float max = new Float(strs[1]);
                    q = LegacyNumericRangeQuery.newFloatRange(field, min, max, true, true);
                } else if ("long".equals(fieldType)) {
                    Long min = new Long(strs[0]);
                    Long max = new Long(strs[1]);
                    q = LegacyNumericRangeQuery.newLongRange(field, min, max, true, true);
                }
            } else {
                if ("int".equals(fieldType)) {
                    q = LegacyNumericRangeQuery.newIntRange(field, new Integer(queryStr), new Integer(queryStr), true, true);
                } else if ("double".equals(fieldType)) {
                    q = LegacyNumericRangeQuery.newDoubleRange(field, new Double(queryStr), new Double(queryStr), true, true);
                } else if ("float".equals(fieldType)) {
                    q = LegacyNumericRangeQuery.newFloatRange(field, new Float(queryStr), new Float(queryStr), true, true);
                } else {
                    Term term = new Term(field, queryStr);
                    q = new TermQuery(term);
                }
            }
        } else {
            q = new MatchAllDocsQuery();
        }
        System.out.println(q);
        return q;
    }

    public static Query getMultiQueryLikeSqlAnd(Query ... querys){
        BooleanQuery query = new BooleanQuery();
        for (Query subQuery : querys) {
            query.add(subQuery, Occur.MUST);
        }
        return query;
    }

    public static Query getRegexExpQuery(String field, String regex) {
        Query query = null;
        Term term = new Term(field, regex);
        query = new RegexpQuery(term);
        return query;
    }

//    public static Query getMultiQueryLikeSqlAnd(Query ... querys){
//        BooleanQuery.Builder query = new BooleanQuery.Builder();
//        for (Query subQuery : querys) {
//            query.add(subQuery,BooleanClause.Occur.MUST);
//        }
//        return (Query) query;
//    }
    public static Sort getSortInfo(String[] fields,Type[] types,boolean[] reverses){
        SortField[] sortFields = null;
        int fieldLength = fields.length;
        int typeLength = types.length;
        int reverLength = reverses.length;
        if(!(fieldLength == typeLength) || !(fieldLength == reverLength)){
            return null;
        }else{
            sortFields = new SortField[fields.length];
            for (int i = 0; i < fields.length; i++) {
                sortFields[i] = new SortField(fields[i], types[i], reverses[i]);
            }
        }
        return new Sort(sortFields);
    }
    // query according to query, query conditions, page num, sort condition
    public static TopDocs getScoreDocsByPerPageAndSortField(IndexSearcher searcher,Query query, int first,int max, Sort sort){
        try {
            if(query == null){
                System.out.println(" Query is null return null ");
                return null;
            }
            TopFieldCollector collector = null;
            if(sort != null){
                collector = TopFieldCollector.create(sort, first+max, false, false, false);
            }else{
                sort = new Sort(new SortField[]{new SortField("modified", SortField.Type.LONG)});
                collector = TopFieldCollector.create(sort, first+max, false, false, false);
            }
            searcher.search(query, collector);
            return collector.topDocs(first, max);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }

    public static Integer getLastIndexBeanID(IndexReader multiReader){
        Query query = new MatchAllDocsQuery();
        IndexSearcher searcher = null;
        searcher = new IndexSearcher(multiReader);
        SortField sortField = new SortField("id", SortField.Type.INT,true);
        Sort sort = new Sort(new SortField[]{sortField});
        TopDocs docs = getScoreDocsByPerPageAndSortField(searcher,query, 0, 1, sort);
        ScoreDoc[] scoreDocs = docs.scoreDocs;
        int total = scoreDocs.length;
        if(total > 0){
            ScoreDoc scoreDoc = scoreDocs[0];
            Document doc = null;
            try {
                doc = searcher.doc(scoreDoc.doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Integer(doc.get("id"));
        }
        return 0;
    }
}
