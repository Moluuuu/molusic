package com.molu.audiofile.dictionary;


import org.springframework.beans.factory.annotation.Value;

// MFD = Music/FileDictionary
public class MFD {
    public static final String FLAC = ".flac";
    public static final String WAV = ".wav";
    public static final String MP3 = ".mp3";
    public static final String LRC = ".lrc";
    public static final String NCM = ".ncm";
    public static final String MFLAC = ".mflac";
    @Value("")
    public static final String FILEPATH = "D:\\IDEAproject\\molusic\\audio-file\\src\\main\\resources\\music";
    public static final String UPLOADFILEPATH = "D:\\IDEAproject\\molusic\\audio-file\\src\\main\\resources\\lyric";
    public static final String LYRICPATH = "D:\\IDEAproject\\molusic\\audio-file\\src\\main\\resources\\upload";
    public static final String CODEC = "libmp3lame";
}
