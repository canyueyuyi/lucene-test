package com.company;

/**
 * Created by neal1 on 2017/8/19.
 */
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.company.bean.FileBean;

import com.lucene.bean.FileBean;
import com.lucene.index.FileBeanIndex;
import com.lucene.index.util.FileUtil;

public class TestIndex {
    public static void main(String[] args) {
        try {
            List<FileBean> fileBeans = FileUtil.getFolderFiles("C:\\Users\\lenovo\\Desktop\\lucene\\lucene-5.1.0");
            int totalCount = fileBeans.size();
            int perThreadCount = 3000;
            System.out.println("查询到的数据总数是"+fileBeans.size());
            int threadCount = totalCount/perThreadCount + (totalCount%perThreadCount == 0 ? 0 : 1);
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch1 = new CountDownLatch(1);
            CountDownLatch countDownLatch2 = new CountDownLatch(threadCount);
            System.out.println(fileBeans.size());

            for(int i = 0; i < threadCount; i++) {
                int start = i*perThreadCount;
                int end = (i+1) * perThreadCount < totalCount ? (i+1) * perThreadCount : totalCount;
                List<FileBean> subList = fileBeans.subList(start, end);
                Runnable runnable = new FileBeanIndex("index",i, countDownLatch1, countDownLatch2, subList);
                //子线程交给线程池管理
                pool.execute(runnable);
            }
            countDownLatch1.countDown();
            System.out.println("开始创建索引");
            //等待所有线程都完成
            countDownLatch2.await();
            //线程全部完成工作
            System.out.println("所有线程都创建索引完毕");
            //释放线程池资源
            pool.shutdown();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
