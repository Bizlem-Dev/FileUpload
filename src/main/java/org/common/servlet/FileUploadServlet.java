package org.common.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.common.service.FileUploadService;


@Component(immediate=true, metatype=false)
@Service(value=javax.servlet.Servlet.class)
@Properties({
	@Property(name="service.description", value="Prefix Test Servlet Minus One"),
	@Property(name="service.vendor", value="The Apache Software Foundation"),
	@Property(name="sling.servlet.paths", value={"/servlet/upload/info"})

})
@SuppressWarnings("serial")
public class FileUploadServlet extends SlingAllMethodsServlet {

	
	@Reference
	private SlingRepository repos;
	
	@Reference
	private FileUploadService service;
	
	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		String filePath=request.getParameter("filePath");
		String fileName=request.getParameter("fileName");
		service.uploadFile(filePath,fileName,request);
		
		
	}
}
