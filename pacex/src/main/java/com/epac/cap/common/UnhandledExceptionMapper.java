package com.epac.cap.common;

//import com.epac.cap.common.EpacException;
//import com.epac.cap.common.EmailService;
//import com.epac.cap.common.TemplateEmailBean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
//import java.util.HashMap;
//import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A catch-all JAX-RS Exception handler for cases when a more specific Exception handler isn't defined. Simple logs the
 * error and returns a plain text response containing the exception message and stack trace.
 */
@Provider
@Component
public class UnhandledExceptionMapper implements ExceptionMapper<Exception> {
  private static final Logger logger = Logger.getLogger(UnhandledExceptionMapper.class);
  //@Autowired
  //private EmailService emailService;

  @Override
  public Response toResponse(Exception exception) {
    String message = null;
    if (StringUtils.isBlank(exception.getMessage())) {
      message = "Exception occured processing request. A System Administrator has been notified.";
    } else {
      message = "Error: " + exception.getMessage();
    }
    logger.error("Exception occurred handling a webservice: " + message, exception);
    sendExceptionEmail(exception);
    Status status = Status.INTERNAL_SERVER_ERROR;
    if (exception instanceof RuntimeException) {
      // assuming runtime exceptions are programming or client error
      status = Status.BAD_REQUEST;
    }
    return Response.status(status).entity(message).build();
  }

  protected void sendExceptionEmail(Exception exception) {
    /*if (emailService != null) {
      // notify interested parties if we can
      TemplateEmailBean email = new TemplateEmailBean();
      email.setHtmlTemplateName("EXCEPTION");
      email.setToAddresses(emailService.getDebugEmailAddress());
      email.setSubject("Exception occurred processing a web service - " + exception.getClass().getSimpleName());
      Map<String, Object> data = new HashMap<String, Object>();
      email.setTemplateModelData(data);
      email.getTemplateModelData().put("exception", exception);
      email.getTemplateModelData().put("stackTrace", getExceptionStack(exception));
      try {
        LOG.debug("sending exception email");
        emailService.sendTemplateEmail(email);
      } catch (Exception ex) {
        LOG.error("Unable to send exception email", ex);
      }
    } else {
      LOG.warn("no emailservice");
    }*/
  }

  protected String getExceptionStack(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    e.printStackTrace(pw);
    return sw.toString();
  }

  /*public EmailService getEmailService() {
    return emailService;
  }

  public void setEmailService(EmailService emailService) {
    this.emailService = emailService;
  }*/
}
