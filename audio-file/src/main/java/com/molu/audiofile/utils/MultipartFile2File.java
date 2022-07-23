package com.molu.audiofile.utils;

import lombok.SneakyThrows;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

public class MultipartFile2File {

    @SneakyThrows
    public static MultipartFile parse(File file){
        return new MockMultipartFile("file", file.getName(), null, Files.newInputStream(file.toPath()));
    }

}
