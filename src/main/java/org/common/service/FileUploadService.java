package org.common.service;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;

public interface FileUploadService {

    public void uploadFile(String filePath, String fileName,
            SlingHttpServletRequest request) throws ServletException,
            IOException;

    public void allFileUpload(String filePath, String fileName,
            SlingHttpServletRequest request) throws ServletException,
            IOException;

    String uploadNThumbnail(String userId, SlingHttpServletRequest request,
            int width, int height, boolean flag, String fileName)
            throws ServletException, IOException;
    
    void uploadSlideShow(String nodePath,
            SlingHttpServletRequest request, int width, int height) 
                    throws ServletException, IOException;
    String uploadSpSearchImage(SlingHttpServletRequest request,
            int width, int height, String fileName)throws ServletException, IOException;
;
}
