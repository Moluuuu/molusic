package com.molu.processing.service;

import java.io.File;
import java.util.Map;


public interface UploadService {

    Map<String, String> uploadToQiNiu(File file);

    Map<String,String> uploadToMinio(File file);
}
