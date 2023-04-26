package org.springframework.security.saml2.provider.service.authentication;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.saml2.assertion.ConditionValidator;
import org.opensaml.saml.saml2.assertion.SAML20AssertionValidator;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.assertion.StatementValidator;
import org.opensaml.saml.saml2.assertion.SubjectConfirmationValidator;
import org.opensaml.saml.saml2.assertion.impl.AudienceRestrictionConditionValidator;
import org.opensaml.saml.saml2.assertion.impl.BearerSubjectConfirmationValidator;
import org.opensaml.saml.saml2.assertion.impl.DelegationRestrictionConditionValidator;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AuthnRequestUnmarshaller;
import org.opensaml.saml.saml2.core.impl.ResponseUnmarshaller;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ErrorCodes;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public final class OpenSaml4AuthenticationProvider implements AuthenticationProvider {

    static {
        OpenSamlInitializationService.initialize();
    }

    private final Log logger = LogFactory.getLog(this.getClass());

    private final ResponseUnmarshaller responseUnmarshaller;

    private static final AuthnRequestUnmarshaller authnRequestUnmarshaller;

    static {
        XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
        authnRequestUnmarshaller = (AuthnRequestUnmarshaller) registry.getUnmarshallerFactory().getUnmarshaller(AuthnRequest.DEFAULT_ELEMENT_NAME);
    }

    private final ParserPool parserPool;

    private final Converter<ResponseToken, Saml2ResponseValidatorResult> responseSignatureValidator = createDefaultResponseSignatureValidator();

    private Consumer<ResponseToken> responseElementsDecrypter = createDefaultResponseElementsDecrypter();

    private Converter<ResponseToken, Saml2ResponseValidatorResult> responseValidator = createDefaultResponseValidator();

    private final Converter<AssertionToken, Saml2ResponseValidatorResult> assertionSignatureValidator = createDefaultAssertionSignatureValidator();

    private Consumer<AssertionToken> assertionElementsDecrypter = createDefaultAssertionElementsDecrypter();

    private Converter<AssertionToken, Saml2ResponseValidatorResult> assertionValidator = createDefaultAssertionValidator();

    private Converter<ResponseToken, ? extends AbstractAuthenticationToken> responseAuthenticationConverter = createDefaultResponseAuthenticationConverter();

    public OpenSaml4AuthenticationProvider() {
        XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
        this.responseUnmarshaller = (ResponseUnmarshaller) registry.getUnmarshallerFactory().getUnmarshaller(Response.DEFAULT_ELEMENT_NAME);
        this.parserPool = registry.getParserPool();
    }

    public void setResponseElementsDecrypter(Consumer<ResponseToken> responseElementsDecrypter) {
        Assert.notNull(responseElementsDecrypter, "responseElementsDecrypter cannot be null");
        this.responseElementsDecrypter = responseElementsDecrypter;
    }

    public void setResponseValidator(Converter<ResponseToken, Saml2ResponseValidatorResult> responseValidator) {
        Assert.notNull(responseValidator, "responseValidator cannot be null");
        this.responseValidator = responseValidator;
    }

    public void setAssertionValidator(Converter<AssertionToken, Saml2ResponseValidatorResult> assertionValidator) {
        Assert.notNull(assertionValidator, "assertionValidator cannot be null");
        this.assertionValidator = assertionValidator;
    }

    public void setAssertionElementsDecrypter(Consumer<AssertionToken> assertionDecrypter) {
        Assert.notNull(assertionDecrypter, "assertionDecrypter cannot be null");
        this.assertionElementsDecrypter = assertionDecrypter;
    }

    public void setResponseAuthenticationConverter(Converter<ResponseToken, ? extends AbstractAuthenticationToken> responseAuthenticationConverter) {
        Assert.notNull(responseAuthenticationConverter, "responseAuthenticationConverter cannot be null");
        this.responseAuthenticationConverter = responseAuthenticationConverter;
    }

    public static Converter<ResponseToken, Saml2ResponseValidatorResult> createDefaultResponseValidator() {
        return (responseToken) -> {
            Response response = responseToken.getResponse();
            Saml2AuthenticationToken token = responseToken.getToken();
            Saml2ResponseValidatorResult result = Saml2ResponseValidatorResult.success();
            String statusCode = getStatusCode(response);
            if (!StatusCode.SUCCESS.equals(statusCode)) {
                String message = String.format("Invalid status [%s] for SAML response [%s]", statusCode, response.getID());
                result = result.concat(new Saml2Error(Saml2ErrorCodes.INVALID_RESPONSE, message));
            }
            String inResponseTo = response.getInResponseTo();
            result = result.concat(validateInResponseTo(token.getAuthenticationRequest(), inResponseTo));
            String issuer = response.getIssuer().getValue();
            String destination = response.getDestination();
            String location = token.getRelyingPartyRegistration().getAssertionConsumerServiceLocation();
            if (StringUtils.hasText(destination) && !destination.equals(location)) {
                String message = "Invalid destination [" + destination + "] for SAML response [" + response.getID() + "]";
                result = result.concat(new Saml2Error(Saml2ErrorCodes.INVALID_DESTINATION, message));
            }
            String assertingPartyEntityId = token.getRelyingPartyRegistration().getAssertingPartyDetails().getEntityId();
            if (!StringUtils.hasText(issuer) || !issuer.equals(assertingPartyEntityId)) {
                String message = String.format("Invalid issuer [%s] for SAML response [%s]", issuer, response.getID());
                result = result.concat(new Saml2Error(Saml2ErrorCodes.INVALID_ISSUER, message));
            }
            if (response.getAssertions().isEmpty()) {
                result = result.concat(new Saml2Error(Saml2ErrorCodes.MALFORMED_RESPONSE_DATA, "No assertions found in response."));
            }
            return result;
        };
    }

    private static Saml2ResponseValidatorResult validateInResponseTo(AbstractSaml2AuthenticationRequest storedRequest, String inResponseTo) {
        if (!StringUtils.hasText(inResponseTo)) {
            return Saml2ResponseValidatorResult.success();
        }
        AuthnRequest request = parseRequest(storedRequest);
        if (request == null) {
            String message = "The response contained an InResponseTo attribute [" + inResponseTo + "]" + " but no saved authentication request was found";
            return Saml2ResponseValidatorResult.failure(new Saml2Error(Saml2ErrorCodes.INVALID_IN_RESPONSE_TO, message));
        }
        if (!inResponseTo.equals(request.getID())) {
            String message = "The InResponseTo attribute [" + inResponseTo + "] does not match the ID of the " + "authentication request [" + request.getID() + "]";
            return Saml2ResponseValidatorResult.failure(new Saml2Error(Saml2ErrorCodes.INVALID_IN_RESPONSE_TO, message));
        }
        return Saml2ResponseValidatorResult.success();
    }

    public static Converter<AssertionToken, Saml2ResponseValidatorResult> createDefaultAssertionValidator() {
        return createAssertionValidator(Saml2ErrorCodes.INVALID_ASSERTION, (assertionToken) -> SAML20AssertionValidators.attributeValidator, (assertionToken) -> createValidationContext(assertionToken, (params) -> params.put(SAML2AssertionValidationParameters.CLOCK_SKEW, Duration.ofMinutes(5))));
    }

    public static Converter<AssertionToken, Saml2ResponseValidatorResult> createDefaultAssertionValidator(Converter<AssertionToken, ValidationContext> contextConverter) {
        return createAssertionValidator(Saml2ErrorCodes.INVALID_ASSERTION, (assertionToken) -> SAML20AssertionValidators.attributeValidator, contextConverter);
    }

    public static Converter<ResponseToken, Saml2Authentication> createDefaultResponseAuthenticationConverter() {
        return (responseToken) -> {
            Response response = responseToken.response;
            Saml2AuthenticationToken token = responseToken.token;
            Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
            String username = assertion.getSubject().getNameID().getValue();
            Map<String, List<Object>> attributes = getAssertionAttributes(assertion);
            List<String> sessionIndexes = getSessionIndexes(assertion);
            DefaultSaml2AuthenticatedPrincipal principal = new DefaultSaml2AuthenticatedPrincipal(username, attributes, sessionIndexes);
            String registrationId = responseToken.token.getRelyingPartyRegistration().getRegistrationId();
            principal.setRelyingPartyRegistrationId(registrationId);
            return new Saml2Authentication(principal, token.getSaml2Response(), AuthorityUtils.createAuthorityList("ROLE_USER"));
        };
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            Saml2AuthenticationToken token = (Saml2AuthenticationToken) authentication;
            String serializedResponse = token.getSaml2Response();
            Response response = parseResponse(serializedResponse);
            process(token, response);
            AbstractAuthenticationToken authenticationResponse = this.responseAuthenticationConverter.convert(new ResponseToken(response, token));
            if (authenticationResponse != null) {
                authenticationResponse.setDetails(authentication.getDetails());
            }
            return authenticationResponse;
        } catch (Saml2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw createAuthenticationException(Saml2ErrorCodes.INTERNAL_VALIDATION_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication != null && Saml2AuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Response parseResponse(String response) throws Saml2Exception, Saml2AuthenticationException {
        try {
            Document document = this.parserPool.parse(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
            Element element = document.getDocumentElement();
            return (Response) this.responseUnmarshaller.unmarshall(element);
        } catch (Exception ex) {
            throw createAuthenticationException(Saml2ErrorCodes.MALFORMED_RESPONSE_DATA, ex.getMessage(), ex);
        }
    }

    private void process(Saml2AuthenticationToken token, Response response) {
        String issuer = response.getIssuer().getValue();
        this.logger.debug(LogMessage.format("Processing SAML response from %s", issuer));
        boolean responseSigned = response.isSigned();
        ResponseToken responseToken = new ResponseToken(response, token);
        Saml2ResponseValidatorResult result = this.responseSignatureValidator.convert(responseToken);
        if (responseSigned) {
            this.responseElementsDecrypter.accept(responseToken);
        } else if (!response.getEncryptedAssertions().isEmpty()) {
            result = result.concat(new Saml2Error(Saml2ErrorCodes.INVALID_SIGNATURE, "Did not decrypt response [" + response.getID() + "] since it is not signed"));
        }
        result = result.concat(this.responseValidator.convert(responseToken));
        boolean allAssertionsSigned = true;
        for (Assertion assertion : response.getAssertions()) {
            AssertionToken assertionToken = new AssertionToken(assertion, token);
            result = result.concat(this.assertionSignatureValidator.convert(assertionToken));
            allAssertionsSigned = allAssertionsSigned && assertion.isSigned();
            if (responseSigned || assertion.isSigned()) {
                this.assertionElementsDecrypter.accept(new AssertionToken(assertion, token));
            }
            result = result.concat(this.assertionValidator.convert(assertionToken));
        }
        if (!responseSigned && !allAssertionsSigned) {
            String description = "Either the response or one of the assertions is unsigned. " + "Please either sign the response or all of the assertions.";
            result = result.concat(new Saml2Error(Saml2ErrorCodes.INVALID_SIGNATURE, description));
        }
        Assertion firstAssertion = CollectionUtils.firstElement(response.getAssertions());
        if (firstAssertion != null && !hasName(firstAssertion)) {
            Saml2Error error = new Saml2Error(Saml2ErrorCodes.SUBJECT_NOT_FOUND, "Assertion [" + firstAssertion.getID() + "] is missing a subject");
            result = result.concat(error);
        }
        if (result.hasErrors()) {
            Collection<Saml2Error> errors = result.getErrors();
            if (this.logger.isTraceEnabled()) {
                this.logger.debug("Found " + errors.size() + " validation errors in SAML response [" + response.getID() + "]: " + errors);
            } else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Found " + errors.size() + " validation errors in SAML response [" + response.getID() + "]");
            }
            Saml2Error first = errors.iterator().next();
            throw createAuthenticationException(first.getErrorCode(), first.getDescription(), null);
        } else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Successfully processed SAML Response [" + response.getID() + "]");
            }
        }
    }

    private Converter<ResponseToken, Saml2ResponseValidatorResult> createDefaultResponseSignatureValidator() {
        return (responseToken) -> {
            Response response = responseToken.getResponse();
            RelyingPartyRegistration registration = responseToken.getToken().getRelyingPartyRegistration();
            if (response.isSigned()) {
                return OpenSamlVerificationUtils.verifySignature(response, registration).post(response.getSignature());
            }
            return Saml2ResponseValidatorResult.success();
        };
    }

    private Consumer<ResponseToken> createDefaultResponseElementsDecrypter() {
        return (responseToken) -> {
            Response response = responseToken.getResponse();
            RelyingPartyRegistration registration = responseToken.getToken().getRelyingPartyRegistration();
            try {
                OpenSamlDecryptionUtils.decryptResponseElements(response, registration);
            } catch (Exception ex) {
                throw createAuthenticationException(Saml2ErrorCodes.DECRYPTION_ERROR, ex.getMessage(), ex);
            }
        };
    }

    private static String getStatusCode(Response response) {
        if (response.getStatus() == null) {
            return StatusCode.SUCCESS;
        }
        if (response.getStatus().getStatusCode() == null) {
            return StatusCode.SUCCESS;
        }
        return response.getStatus().getStatusCode().getValue();
    }

    private Converter<AssertionToken, Saml2ResponseValidatorResult> createDefaultAssertionSignatureValidator() {
        return createAssertionValidator(Saml2ErrorCodes.INVALID_SIGNATURE, (assertionToken) -> {
            RelyingPartyRegistration registration = assertionToken.getToken().getRelyingPartyRegistration();
            SignatureTrustEngine engine = OpenSamlVerificationUtils.trustEngine(registration);
            return SAML20AssertionValidators.createSignatureValidator(engine);
        }, (assertionToken) -> new ValidationContext(Collections.singletonMap(SAML2AssertionValidationParameters.SIGNATURE_REQUIRED, false)));
    }

    private Consumer<AssertionToken> createDefaultAssertionElementsDecrypter() {
        return (assertionToken) -> {
            Assertion assertion = assertionToken.getAssertion();
            RelyingPartyRegistration registration = assertionToken.getToken().getRelyingPartyRegistration();
            try {
                OpenSamlDecryptionUtils.decryptAssertionElements(assertion, registration);
            } catch (Exception ex) {
                throw createAuthenticationException(Saml2ErrorCodes.DECRYPTION_ERROR, ex.getMessage(), ex);
            }
        };
    }

    private boolean hasName(Assertion assertion) {
        if (assertion == null) {
            return false;
        }
        if (assertion.getSubject() == null) {
            return false;
        }
        if (assertion.getSubject().getNameID() == null) {
            return false;
        }
        return assertion.getSubject().getNameID().getValue() != null;
    }

    private static Map<String, List<Object>> getAssertionAttributes(Assertion assertion) {
        Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                List<Object> attributeValues = new ArrayList<>();
                for (XMLObject xmlObject : attribute.getAttributeValues()) {
                    Object attributeValue = getXmlObjectValue(xmlObject);
                    if (attributeValue != null) {
                        attributeValues.add(attributeValue);
                    }
                }
                attributeMap.put(attribute.getName(), attributeValues);
            }
        }
        return attributeMap;
    }

    private static List<String> getSessionIndexes(Assertion assertion) {
        List<String> sessionIndexes = new ArrayList<>();
        for (AuthnStatement statement : assertion.getAuthnStatements()) {
            sessionIndexes.add(statement.getSessionIndex());
        }
        return sessionIndexes;
    }

    private static Object getXmlObjectValue(XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        }
        if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        }
        if (xmlObject instanceof XSInteger) {
            return ((XSInteger) xmlObject).getValue();
        }
        if (xmlObject instanceof XSURI) {
            return ((XSURI) xmlObject).getURI();
        }
        if (xmlObject instanceof XSBoolean) {
            XSBooleanValue xsBooleanValue = ((XSBoolean) xmlObject).getValue();
            return (xsBooleanValue != null) ? xsBooleanValue.getValue() : null;
        }
        if (xmlObject instanceof XSDateTime) {
            return ((XSDateTime) xmlObject).getValue();
        }
        return xmlObject;
    }

    private static Saml2AuthenticationException createAuthenticationException(String code, String message, Exception cause) {
        return new Saml2AuthenticationException(new Saml2Error(code, message), cause);
    }

    private static Converter<AssertionToken, Saml2ResponseValidatorResult> createAssertionValidator(String errorCode, Converter<AssertionToken, SAML20AssertionValidator> validatorConverter, Converter<AssertionToken, ValidationContext> contextConverter) {
        return (assertionToken) -> {
            Assertion assertion = assertionToken.assertion;
            SAML20AssertionValidator validator = validatorConverter.convert(assertionToken);
            ValidationContext context = contextConverter.convert(assertionToken);
            try {
                ValidationResult result = validator.validate(assertion, context);
                if (result == ValidationResult.VALID) {
                    return Saml2ResponseValidatorResult.success();
                }
            } catch (Exception ex) {
                String message = String.format("Invalid assertion [%s] for SAML response [%s]: %s", assertion.getID(), ((Response) assertion.getParent()).getID(), ex.getMessage());
                return Saml2ResponseValidatorResult.failure(new Saml2Error(errorCode, message));
            }
            String message = String.format("Invalid assertion [%s] for SAML response [%s]: %s", assertion.getID(), ((Response) assertion.getParent()).getID(), context.getValidationFailureMessage());
            return Saml2ResponseValidatorResult.failure(new Saml2Error(errorCode, message));
        };
    }

    private static ValidationContext createValidationContext(AssertionToken assertionToken, Consumer<Map<String, Object>> paramsConsumer) {
        Saml2AuthenticationToken token = assertionToken.token;
        RelyingPartyRegistration relyingPartyRegistration = token.getRelyingPartyRegistration();
        String audience = relyingPartyRegistration.getEntityId();
        String recipient = relyingPartyRegistration.getAssertionConsumerServiceLocation();
        String assertingPartyEntityId = relyingPartyRegistration.getAssertingPartyDetails().getEntityId();
        Map<String, Object> params = new HashMap<>();
        Assertion assertion = assertionToken.getAssertion();
        if (assertionContainsInResponseTo(assertion)) {
            String requestId = getAuthnRequestId(token.getAuthenticationRequest());
            params.put(SAML2AssertionValidationParameters.SC_VALID_IN_RESPONSE_TO, requestId);
        }
        params.put(SAML2AssertionValidationParameters.COND_VALID_AUDIENCES, Collections.singleton(audience));
        params.put(SAML2AssertionValidationParameters.SC_VALID_RECIPIENTS, Collections.singleton(recipient));
        params.put(SAML2AssertionValidationParameters.VALID_ISSUERS, Collections.singleton(assertingPartyEntityId));
        paramsConsumer.accept(params);
        return new ValidationContext(params);
    }

    private static boolean assertionContainsInResponseTo(Assertion assertion) {
        if (assertion.getSubject() == null) {
            return false;
        }
        for (SubjectConfirmation confirmation : assertion.getSubject().getSubjectConfirmations()) {
            SubjectConfirmationData confirmationData = confirmation.getSubjectConfirmationData();
            if (confirmationData == null) {
                continue;
            }
            if (StringUtils.hasText(confirmationData.getInResponseTo())) {
                return true;
            }
        }
        return false;
    }

    private static String getAuthnRequestId(AbstractSaml2AuthenticationRequest serialized) {
        AuthnRequest request = parseRequest(serialized);
        if (request == null) {
            return null;
        }
        return request.getID();
    }

    private static AuthnRequest parseRequest(AbstractSaml2AuthenticationRequest request) {
        if (request == null) {
            return null;
        }
        String samlRequest = request.getSamlRequest();
        if (!StringUtils.hasText(samlRequest)) {
            return null;
        }
        if (request.getBinding() == Saml2MessageBinding.REDIRECT) {
            samlRequest = Saml2Utils.samlInflate(Saml2Utils.samlDecode(samlRequest));
        } else {
            samlRequest = new String(Saml2Utils.samlDecode(samlRequest), StandardCharsets.UTF_8);
        }
        try {
            Document document = XMLObjectProviderRegistrySupport.getParserPool().parse(new ByteArrayInputStream(samlRequest.getBytes(StandardCharsets.UTF_8)));
            Element element = document.getDocumentElement();
            return (AuthnRequest) authnRequestUnmarshaller.unmarshall(element);
        } catch (Exception ex) {
            String message = "Failed to deserialize associated authentication request [" + ex.getMessage() + "]";
            throw createAuthenticationException(Saml2ErrorCodes.MALFORMED_REQUEST_DATA, message, ex);
        }
    }

    private static class SAML20AssertionValidators {

        private static final Collection<ConditionValidator> conditions = new ArrayList<>();

        private static final Collection<SubjectConfirmationValidator> subjects = new ArrayList<>();

        private static final Collection<StatementValidator> statements = new ArrayList<>();

        private static final SignaturePrevalidator validator = new SAMLSignatureProfileValidator();

        static {
            conditions.add(new AudienceRestrictionConditionValidator());
            conditions.add(new DelegationRestrictionConditionValidator());
            conditions.add(new ConditionValidator() {

                @Nonnull
                @Override
                public QName getServicedCondition() {
                    return OneTimeUse.DEFAULT_ELEMENT_NAME;
                }

                @Nonnull
                @Override
                public ValidationResult validate(Condition condition, Assertion assertion, ValidationContext context) {
                    return ValidationResult.VALID;
                }
            });
            subjects.add(new BearerSubjectConfirmationValidator() {

                @Override
                protected ValidationResult validateAddress(SubjectConfirmation confirmation, Assertion assertion, ValidationContext context, boolean required) {
                    return ValidationResult.VALID;
                }
            });
        }

        private static final SAML20AssertionValidator attributeValidator = new SAML20AssertionValidator(conditions, subjects, statements, null, null, null) {

            @Nonnull
            @Override
            protected ValidationResult validateSignature(Assertion token, ValidationContext context) {
                return ValidationResult.VALID;
            }
        };

        static SAML20AssertionValidator createSignatureValidator(SignatureTrustEngine engine) {
            return new SAML20AssertionValidator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, engine, validator) {

                @Nonnull
                @Override
                protected ValidationResult validateConditions(Assertion assertion, ValidationContext context) {
                    return ValidationResult.VALID;
                }

                @Nonnull
                @Override
                protected ValidationResult validateSubjectConfirmation(Assertion assertion, ValidationContext context) {
                    return ValidationResult.VALID;
                }

                @Nonnull
                @Override
                protected ValidationResult validateStatements(Assertion assertion, ValidationContext context) {
                    return ValidationResult.VALID;
                }

                @Override
                protected ValidationResult validateIssuer(Assertion assertion, ValidationContext context) {
                    return ValidationResult.VALID;
                }
            };
        }
    }

    public static class ResponseToken {

        private final Saml2AuthenticationToken token;

        private final Response response;

        ResponseToken(Response response, Saml2AuthenticationToken token) {
            this.token = token;
            this.response = response;
        }

        public Response getResponse() {
            return this.response;
        }

        public Saml2AuthenticationToken getToken() {
            return this.token;
        }
    }

    public static class AssertionToken {

        private final Saml2AuthenticationToken token;

        private final Assertion assertion;

        AssertionToken(Assertion assertion, Saml2AuthenticationToken token) {
            this.token = token;
            this.assertion = assertion;
        }

        public Assertion getAssertion() {
            return this.assertion;
        }

        public Saml2AuthenticationToken getToken() {
            return this.token;
        }
    }
}
