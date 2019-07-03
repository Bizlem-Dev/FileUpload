package org.common.service.impl;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.jcr.api.SlingRepository;
import org.common.service.FileUploadService;

@Component(configurationFactory = true)
@Service(FileUploadService.class)
@Properties({ @Property(name = "FileUploadService", value = "upload") })
public class FileUploadServieImpl implements FileUploadService {

    @Reference
    private SlingRepository repos;

    @SuppressWarnings("deprecation")
    public void uploadFile(String filePath, String fileName,
            SlingHttpServletRequest request) throws ServletException,
            IOException {

        Session session;
        Node node, fileNode, jcrNode = null;

        try {

            session = repos.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));

            node = session.getNode(filePath);

            fileNode = node.addNode(fileName, "nt:file");

            jcrNode = fileNode.addNode("jcr:content", "nt:resource");
            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {

                        p.getInputStream();
                        jcrNode.setProperty("jcr:data", p.getInputStream());
                    }
                }
            }

            jcrNode.setProperty("jcr:lastModified", Calendar.getInstance());
            jcrNode.setProperty("jcr:mimeType", "image/jpg");

            session.save();
        } catch (LoginException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation")
    public void allFileUpload(String filePath, String fileName,
            SlingHttpServletRequest request) throws ServletException,
            IOException {

        Session session;
        Node node, filePathNode, fileNode, jcrNode = null;

        try {

            session = repos.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));

            filePathNode = session.getNode(filePath);
            node = filePathNode.addNode(fileName);
            String mimeType = "";

            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {
                        mimeType = p.getContentType();
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }
                        fileNode = node.addNode(p.getFileName(), "nt:file");
                        jcrNode = fileNode
                                .addNode("jcr:content", "nt:resource");
                        p.getInputStream();
                        jcrNode.setProperty("jcr:data", p.getInputStream());
                        jcrNode.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        jcrNode.setProperty("jcr:mimeType", mimeType);
                    }
                }
            }

            session.save();
        } catch (LoginException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation")
    public String uploadNThumbnail(String userId,
            SlingHttpServletRequest request, int width, int height,
            boolean flag, String fileName) throws ServletException, IOException {

        Session session;
        Node userNode, mediaNode, fileNode, thumbnalNode2, thumbnalNodeJcrNode2 = null;
        String path = request.getSession().getServletContext().getRealPath("/")
                + "/temp/";
        DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
        Date date = new Date();
        String randomNumber = "";
        if (flag) {
            randomNumber = generateRandomNumber();
        } else {
            randomNumber = fileName;
        }
        try {

            session = repos.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            if (flag) {
                userNode = session.getNode("/content/user/" + userId);
                if (userNode.hasNode("search")) {
                    mediaNode = userNode.getNode("search");
                } else {
                    mediaNode = userNode.addNode("search");

                }
            } else {
                mediaNode = session.getNode(userId);
            }
            String mimeType = "";

            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {
                        mimeType = p.getContentType();
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }

                        fileNode = mediaNode.addNode(randomNumber);
                        fileNode.setProperty("photoDate",
                                dateFormat.format(date));

                        generateThumbnail(path, randomNumber,
                                p.getInputStream(), width, height);
                        File fileThumbnail2 = new File(path + randomNumber
                                + ".jpg");
                        InputStream thumbnailStream2 = new FileInputStream(
                                fileThumbnail2);
                        thumbnalNode2 = fileNode.addNode("x150", "nt:file");
                        thumbnalNodeJcrNode2 = thumbnalNode2.addNode(
                                "jcr:content", "nt:resource");

                        thumbnalNodeJcrNode2.setProperty("jcr:data",
                                thumbnailStream2);
                        thumbnalNodeJcrNode2.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        thumbnalNodeJcrNode2.setProperty("jcr:mimeType",
                                mimeType);
                        fileThumbnail2.delete();
                    }
                }
            }

            session.save();
        } catch (Exception e) {
            return "";
        }
        return randomNumber;
    }

    private void generateThumbnail(String filePath, String filname,
            InputStream fileData, int width, int height) {
        try {
            BufferedImage src = ImageIO.read(fileData);
            if (src == null) {
                final StringBuffer sb = new StringBuffer();
                for (String fmt : ImageIO.getReaderFormatNames()) {
                    sb.append(fmt);
                    sb.append(' ');
                }
                throw new IOException(
                        "Unable to read image, registered formats: " + sb);
            }

            final double scale = (double) width / src.getWidth();

            int destWidth = width;
            int destHeight = 100;
            if (height == 0) {
                destHeight = new Double(src.getHeight() * scale).intValue();
            } else {
                destHeight = height;
            }

            BufferedImage dest = new BufferedImage(destWidth, destHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dest.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(
                    (double) destWidth / src.getWidth(), (double) destHeight
                            / src.getHeight());
            g.drawRenderedImage(src, at);
            File files = new File(filePath);
            if (!files.exists()) {
                files.mkdirs();
            }
            File fileThumbnail = new File(filePath + filname + ".jpg");
            ImageIO.write(dest, "jpg", fileThumbnail);
        } catch (Exception e) {

        }
    }

    @SuppressWarnings({ "deprecation", "unused" })
    public void uploadSlideShow(String nodePath,
            SlingHttpServletRequest request, int width, int height)
            throws ServletException, IOException {

        Session session;
        Node mediaNode, fileNode, thumbnalNodeJcrNode2 = null;
        String path = request.getSession().getServletContext().getRealPath("/")
                + "/temp/";
        DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
        Date date = new Date();
        int count = 1;
        try {

            session = repos.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            mediaNode = session.getNode(nodePath);
            if (mediaNode.hasProperty("number")) {
                count = (int) (mediaNode.getProperty("number").getLong() + 1);
            } else {
                count = 1;
            }
            String mimeType = "";
            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {
                        mimeType = p.getContentType();
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }

                        fileNode = mediaNode.addNode(count + "", "nt:file");
                        generateThumbnail(path, count + "slideS",
                                p.getInputStream(), width, height);
                        File fileThumbnail2 = new File(path + count + "slideS"
                                + ".jpg");
                        InputStream thumbnailStream2 = new FileInputStream(
                                fileThumbnail2);
                        thumbnalNodeJcrNode2 = fileNode.addNode("jcr:content",
                                "nt:resource");

                        thumbnalNodeJcrNode2.setProperty("jcr:data",
                                thumbnailStream2);
                        thumbnalNodeJcrNode2.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        thumbnalNodeJcrNode2.setProperty("jcr:mimeType",
                                mimeType);
                        fileThumbnail2.delete();
                        count++;
                    }
                }
            }
            mediaNode.setProperty("number", count);
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public String uploadSpSearchImage(SlingHttpServletRequest request,
            int width, int height, String fileName)
            throws ServletException, IOException {

        Session session;
        Node userNode, mediaNode, fileNode, thumbnalNode2, thumbnalNodeJcrNode2 = null;
        String path = request.getSession().getServletContext().getRealPath("/")
                + "/temp/";
        DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
        Date date = new Date();
        String randomNumber = "";
            randomNumber = generateRandomNumber();
        try {

            session = repos.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
                userNode = session.getNode("/content");
                if (userNode.hasNode("spsearch")) {
                    mediaNode = userNode.getNode("spsearch");
                } else {
                    mediaNode = userNode.addNode("spsearch");
                }
            String mimeType = "";

            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {
                        mimeType = p.getContentType();
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }

                        fileNode = mediaNode.addNode(randomNumber);
                        fileNode.setProperty("photoDate",
                                dateFormat.format(date));

                        generateThumbnail(path, randomNumber,
                                p.getInputStream(), width, height);
                        File fileThumbnail2 = new File(path + randomNumber
                                + ".jpg");
                        InputStream thumbnailStream2 = new FileInputStream(
                                fileThumbnail2);
                        thumbnalNode2 = fileNode.addNode("x150", "nt:file");
                        thumbnalNodeJcrNode2 = thumbnalNode2.addNode(
                                "jcr:content", "nt:resource");

                        thumbnalNodeJcrNode2.setProperty("jcr:data",
                                thumbnailStream2);
                        thumbnalNodeJcrNode2.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        thumbnalNodeJcrNode2.setProperty("jcr:mimeType",
                                mimeType);
                        fileThumbnail2.delete();
                    }
                }
            }

            session.save();
        } catch (Exception e) {
            return "";
        }
        return randomNumber;
    }

    private String generateRandomNumber() {
        Random rand = new Random();
        long accumulator = 1 + rand.nextInt(9); // ensures that the 16th digit
                                                // isn't 0
        for (int i = 0; i < 15; i++) {
            accumulator *= 10L;
            accumulator += rand.nextInt(10);
        }
        
        return accumulator + "";
    }
}
