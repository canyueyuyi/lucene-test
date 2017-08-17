package com.company;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by neal1 on 2017/8/16.
 */
public class TextFileFilter implements FileFilter{

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}
