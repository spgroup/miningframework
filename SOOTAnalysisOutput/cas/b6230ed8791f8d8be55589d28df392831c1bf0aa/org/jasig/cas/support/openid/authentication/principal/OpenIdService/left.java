package org.jasig.cas.support.openid.authentication.principal;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.util.ApplicationContextProvider;
import org.openid4java.association.Association;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public final class OpenIdService extends AbstractWebApplicationService {

    private static final String CRLF = "\r\n";

    protected static final Logger LOG = LoggerFactory.getLogger(OpenIdService.class);

    private static final long serialVersionUID = 5776500133123291301L;

    private static final String CONST_PARAM_SERVICE = "openid.return_to";

    private String identity;

    private final String artifactId;

    private final ParameterList requestParameters;

    protected OpenIdService(final String id, final String originalUrl, final String artifactId, final String openIdIdentity, final String signature, final ParameterList parameterList) {
        super(id, originalUrl, artifactId, null);
        this.identity = openIdIdentity;
        this.artifactId = artifactId;
        this.requestParameters = parameterList;
    }

    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();
        if (ticketId != null) {
            ServerManager manager = (ServerManager) ApplicationContextProvider.getApplicationContext().getBean("serverManager");
            CentralAuthenticationService cas = (CentralAuthenticationService) ApplicationContextProvider.getApplicationContext().getBean("centralAuthenticationService");
            boolean associated = false;
            boolean associationValid = true;
            try {
                AuthRequest authReq = AuthRequest.createAuthRequest(requestParameters, manager.getRealmVerifier());
                Map parameterMap = authReq.getParameterMap();
                if (parameterMap != null && parameterMap.size() > 0) {
                    String assocHandle = (String) parameterMap.get("openid.assoc_handle");
                    if (assocHandle != null) {
                        Association association = manager.getSharedAssociations().load(assocHandle);
                        if (association != null) {
                            associated = true;
                            if (association.hasExpired()) {
                                associationValid = false;
                            }
                        }
                    }
                }
            } catch (MessageException me) {
                LOG.error("Message exception : " + me.getMessage(), me);
            }
            boolean successFullAuthentication = true;
            try {
                if (associated) {
                    if (associationValid) {
                        cas.validateServiceTicket(ticketId, this);
                        LOG.info("Validated openid ticket");
                    } else {
                        successFullAuthentication = false;
                    }
                }
            } catch (TicketException te) {
                LOG.error("Could not validate ticket : " + te.getMessage(), te);
                successFullAuthentication = false;
            }
            Message response = manager.authResponse(requestParameters, this.identity, this.identity, successFullAuthentication, true);
            parameters.putAll(response.getParameterMap());
            if (!associated) {
                parameters.put("openid.assoc_handle", ticketId);
            }
        } else {
            parameters.put("openid.mode", "cancel");
        }
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }

    public boolean logOutOfService(final String sessionIdentifier) {
        return false;
    }

    public static OpenIdService createServiceFrom(final HttpServletRequest request) {
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String openIdIdentity = request.getParameter("openid.identity");
        final String signature = request.getParameter("openid.sig");
        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }
        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter("openid.assoc_handle");
        ParameterList paramList = new ParameterList(request.getParameterMap());
        return new OpenIdService(id, service, artifactId, openIdIdentity, signature, paramList);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.identity == null) ? 0 : this.identity.hashCode());
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OpenIdService other = (OpenIdService) obj;
        if (this.identity == null) {
            if (other.identity != null)
                return false;
        } else if (!this.identity.equals(other.identity))
            return false;
        return true;
    }

    public String getIdentity() {
        return this.identity;
    }
}
