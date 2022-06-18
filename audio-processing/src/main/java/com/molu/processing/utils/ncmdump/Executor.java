package com.molu.processing.utils.ncmdump;

import java.io.File;

public class Executor {

    public static void main(String[] args) throws Exception {

        //find ncm file;
        /*if (args.length == 0) {
            throw new Exception("no ncm file");
        } else {
            for (String arg : args) {
                File ncm_f = new File(arg);
                Dump dump = new Dump(ncm_f);
                dump.execute();
            }
        }*/

        File file = new File("D:\\IDEAproject\\molusic\\audio-processing\\src\\main\\resources\\music\\野外合作社 - 诚实假日酒店.ncm");
        Dump dump = new Dump(file);
        dump.execute();
    }
}
