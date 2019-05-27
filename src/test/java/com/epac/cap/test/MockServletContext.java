package com.epac.cap.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.tomcat.util.descriptor.web.SecurityCollection;
//import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.epac.cap.common.SimplePrincipal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * An extension of the spring MockServletContext which adds the ability to load web.xml for initial configuration.
 * 
 */
public class MockServletContext extends org.springframework.mock.web.MockServletContext {
  protected static final Log logger = LogFactory.getLog(MockServletContext.class);

  //private List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>();

  /**
	 * 
	 */
  public MockServletContext() {
    super();
  }

  /**
   * @param resourceLoader
   * @see org.springframework.mock.web.MockServletContext#MockServletContext(ResourceLoader)
   */
  public MockServletContext(ResourceLoader resourceLoader) {
    super(resourceLoader);
  }

  /**
   * @param resourceBasePath
   * @see org.springframework.mock.web.MockServletContext#MockServletContext(String)
   */
  public MockServletContext(String resourceBasePath) {
    super(resourceBasePath);
  }

  /**
   * @param resourceBasePath
   * @param resourceLoader
   * 
   * @see org.springframework.mock.web.MockServletContext#MockServletContext(String, ResourceLoader)
   */
  public MockServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
    super(resourceBasePath, resourceLoader);
  }

  /**
   * @param pathToWebXml
   * @throws Exception
   */
  public void initFromWebXml(String pathToWebXml) throws Exception {
    logger.debug("initing from web xml using web xml path: " + pathToWebXml);
    InputStream is = getResourceAsStream(pathToWebXml);

    if (is != null) {
      logger.debug("Loading web.xml from InputStream [" + is + "]");
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.parse(is);
      logger.debug("parsing web.xml");
      //parseWebXml(doc);
    } else {
      logger.warn("InputStream for web xml was null so nothing to load. Check path.");
    }

  }

  /**
   * @throws Exception
   */
  public void initFromWebXml() throws Exception {
    initFromWebXml("/WEB-INF/web.xml");
  }

  /**
   * Parse the web.xml for the context-params for now, might expand later if necessary
   
  private void parseWebXml(Document webxml) throws Exception {
    NodeList nl = webxml.getElementsByTagName("context-param");
    for (int i = 0; i < nl.getLength(); i++) {
      parseContextParamNode((Element) nl.item(i));
    }
    // now add the security roles
    NodeList rolesNodeList = webxml.getElementsByTagName("security-role");
    for (int i = 0; i < rolesNodeList.getLength(); i++) {
      Element currentNode = (Element) rolesNodeList.item(i);
      Node name = currentNode.getElementsByTagName("role-name").item(0);
      super.declareRoles(getText(name));
    }

    // now parse the security constraints auth-constraint
    NodeList constraintsNodeList = webxml.getElementsByTagName("security-constraint");
    for (int i = 0; i < constraintsNodeList.getLength(); i++) {
      Element currentNode = (Element) constraintsNodeList.item(i);
      SecurityConstraint constraint = new SecurityConstraint();
      NodeList authConstraintNodeList = currentNode.getElementsByTagName("auth-constraint");
      if (authConstraintNodeList != null && authConstraintNodeList.getLength() > 0) {
        constraint.setAuthConstraint(true);
        for (int j = 0; j < authConstraintNodeList.getLength(); j++) {
          Element currentAuthNode = (Element) authConstraintNodeList.item(j);
          NodeList rolesNL = currentAuthNode.getElementsByTagName("role-name");
          for (int k = 0; k < rolesNL.getLength(); k++) {
            Element currentRoleNode = (Element) rolesNL.item(k);
            Node roleNode = currentRoleNode.getFirstChild();
            String authRole = getText(roleNode);
            if (!"*".equals(authRole)) {
              constraint.addAuthRole(authRole);
            } else {
              for (String declaredRole : super.getDeclaredRoles()) {
                constraint.addAuthRole(declaredRole);
              }
            }
          }

        }
      } else {
        constraint.setAuthConstraint(false);
      }

      NodeList resourceNodeList = currentNode.getElementsByTagName("web-resource-collection");
      if (resourceNodeList != null) {
        for (int k = 0; k < resourceNodeList.getLength(); k++) {
          Element currentResourceNode = (Element) resourceNodeList.item(k);
          SecurityCollection secColl = new SecurityCollection();
          NodeList urlNodeList = currentResourceNode.getElementsByTagName("url-pattern");
          for (int l = 0; l < urlNodeList.getLength(); l++) {
            Element currentUrlElement = (Element) urlNodeList.item(l);
            Node urlNode = currentUrlElement.getFirstChild();
            secColl.addPattern(getText(urlNode));
          }

          NodeList methodList = currentResourceNode.getElementsByTagName("http-method");
          for (int l = 0; l < methodList.getLength(); l++) {
            Element currentMethodElement = (Element) urlNodeList.item(l);
            Node methodNode = currentMethodElement.getFirstChild();
            secColl.addMethod(getText(methodNode));
          }
          constraint.addCollection(secColl);
        }
      }
      securityConstraints.add(constraint);
    }
    logger.debug(securityConstraints);
  }*/

  private void parseContextParamNode(Element n) throws Exception {
    // <context-param>
    // <param-name>configPath</param-name>
    // <param-value>/WEB-INF/applicationContext.xml</param-value>
    // </context-param>

    Node name = n.getElementsByTagName("param-name").item(0);
    Node value = n.getElementsByTagName("param-value").item(0);
    addInitParameter(getText(name), getText(value));
  }

  private String getText(Node e) {
    if (e instanceof Text) {
      return ((Text) e).getData();
    }

    NodeList nl2 = e.getChildNodes();
    if (nl2.getLength() != 1 || !(nl2.item(0) instanceof Text)) {
      throw new IllegalArgumentException("Unexpected element or type mismatch: " + nl2.item(0) + "; tag name was <"
              + ((Element) e).getTagName() + ">");
    }
    Text t = (Text) nl2.item(0);
    return t.getData();
  }

  /**
   * Returns true if the user passes all security constraints declared in web.xml for the given URI or if the URI doesnt
   * match any security constraints.
   * 
   * @param user the principal to check the authorization for. Should have the roles populated
   * @param uri the URI to check
   * @return
  
  public boolean checkSecurityContraints(SimplePrincipal user, String uri) {
    boolean passed = false;
    boolean foundMatchingConstraint = false;
    for (SecurityConstraint constraint : securityConstraints) {
      // once we've found a matching constraint then the URI must pass all other matching constraints
      if (!passed || foundMatchingConstraint) {
        // assuming that all calls will be either a get or a post
        boolean matchingGet = constraint.included(uri, "GET");
        boolean matchingPost = constraint.included(uri, "POST");
        if (matchingGet || matchingPost) {
          foundMatchingConstraint = true;
          // authConstraint == false indicates there was no auth constraint i.e. no access control checking
          passed = !constraint.getAuthConstraint();
          if (!passed) {
            for (String role : constraint.findAuthRoles()) {
              if (user.isUserInRole(role)) {
                // user has 1 of the auth roles which is good enough for authorization
                passed = true;
                break;
              }
            }
          } else {
            // once there is a matching constraint that has no auth constraint then the security constraint auto-passes
            passed = true;
            break;
          }
        }
      }
    }
    return passed || !foundMatchingConstraint;
  } */

  /**
   * @param uri
   * @return
   
  public Set<String> getRequiredRoles(String uri) {
    Set<String> requiredRoles = new HashSet<String>();
    for (SecurityConstraint constraint : securityConstraints) {
      boolean matchingGet = constraint.included(uri, "GET");
      boolean matchingPost = constraint.included(uri, "POST");
      if ((matchingGet || matchingPost)) {
        if (!constraint.getAuthConstraint()) {
          // there is a security constraint without an auth constraint meaning anyone can access
          // which overrides any existing check
          requiredRoles.clear();
          break;
        }
        requiredRoles.addAll(Arrays.asList(constraint.findAuthRoles()));
      }
    }
    return requiredRoles;
  }*/
}
