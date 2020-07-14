package com.scand.auth.authenticator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;

/**
 * Copied from https://github.com/gwallet/keycloak-sms-authenticator
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger LOG = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private static enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }


    @Override
    public void authenticate(AuthenticationFlowContext context) {
        LOG.debug("authenticate called ... context = " + context);

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        String mobileNumberAttribute = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        if (mobileNumberAttribute == null) {
            LOG.error("Mobile number attribute is not configured for the SMS Authenticator.");
            return;
        }

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), mobileNumberAttribute);
        if (mobileNumber != null) {
            long nrOfDigits = SMSAuthenticatorUtil.getConfigLong(config, SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
            LOG.debug("Using nrOfDigits " + nrOfDigits);


            long ttl = SMSAuthenticatorUtil.getConfigLong(config, SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s

            LOG.debug("Using ttl " + ttl + " (s)");

            String code = getSmsCode(nrOfDigits);

            storeSMSCode(context, code, new Date().getTime() + (ttl * 1000)); // s --> ms
            if (sendSmsCode(mobileNumber, code, context.getAuthenticatorConfig())) {
                LOG.debug("SMS send success");
            } else {
                LOG.debug("SMS send  not success");
            }
        } else {
            LOG.debug("Mobile number not found");
        }
    }


    @Override
    public void action(AuthenticationFlowContext context) {
        LOG.debug("action called ... context = " + context);
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                LOG.debug("EXPIRED");
                break;
            case INVALID:
                if(context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.CONDITIONAL ||
                        context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                    LOG.debug("Calling context.attempted()");
                    context.attempted();
                } else if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                    LOG.debug("REQUIRED");
                } else {
                    // Something strange happened
                    LOG.warn("Undefined execution ...");
                }
                break;

            case VALID:
                context.success();
                break;

        }
    }

    // Store the code + expiration time in a UserCredential. Keycloak will persist these in the DB.
    // When the code is validated on another node (in a clustered environment) the other nodes have access to it's values too.
    private void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
        UserCredentialModel credentials = new UserCredentialModel();
        credentials.setType(SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
        credentials.setValue(code);
        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), credentials);

        credentials.setType(SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);
        credentials.setValue((expiringAt).toString());
        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), credentials);
    }


    protected CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.INVALID;

        LOG.debug("validateCode called ... ");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(SMSAuthenticatorContstants.ANSW_SMS_CODE);

        String expectedCode = SMSAuthenticatorUtil.getCredentialValue(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
        String expTimeString = SMSAuthenticatorUtil.getCredentialValue(context.getSession().userCredentialManager(), context.getRealm(), context.getUser(), SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);

        LOG.debug("Expected code = " + expectedCode + "    entered code = " + enteredCode);

        if(expectedCode != null) {
            result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            long now = new Date().getTime();

            LOG.debug("Valid code expires in " + (Long.parseLong(expTimeString) - now) + " ms");
            if(result == CODE_STATUS.VALID) {
                if (Long.parseLong(expTimeString) < now) {
                    LOG.debug("Code is expired !!");
                    result = CODE_STATUS.EXPIRED;
                }
            }
        }
        LOG.debug("result : " + result);
        return result;
    }

    @Override
    public boolean requiresUser() {
        LOG.debug("requiresUser called ... returning true");
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        LOG.debug("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        boolean result = true;
        LOG.debug("... returning "  +result);
        return result;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        LOG.debug("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    @Override
    public void close() {
        LOG.debug("close called ...");
    }


    private String getSmsCode(long nrOfDigits) {
        if(nrOfDigits < 1) {
            throw new RuntimeException("Nr of digits must be bigger than 0");
        }

        double maxValue = Math.pow(10.0, nrOfDigits); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long)(r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    private boolean sendSmsCode(String mobileNumber, String code, AuthenticatorConfigModel config) {
        // Send an SMS
        LOG.debug("Sending " + code + "  to mobileNumber " + mobileNumber);

        String smsUrl = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_URL);
        String smsUsr = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_USERNAME);
        String smsPwd = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_PASSWORD);

        String proxyUrl = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_PROXY_URL);
        String proxyUsr = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_PROXY_USERNAME);
        String proxyPwd = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_PROXY_PASSWORD);
        String contentType = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_CONTENT_TYPE);

        CloseableHttpClient httpClient = null;
        try {
            URL smsURL = (smsUrl != null && smsUrl.length() > 0) ? new URL(smsUrl) : null;
            URL proxyURL = (proxyUrl != null && proxyUrl.length() > 0) ? new URL(proxyUrl) : null;

            if(smsURL == null) {
                LOG.error("SMS gateway URL is not configured.");
                return false;
            }


            CredentialsProvider credsProvider;
            if(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_AUTHTYPE, "").equals(SMSAuthenticatorContstants.AUTH_METHOD_INMESSAGE)) {
                credsProvider = getCredentialsProvider(null, null, proxyUsr, proxyPwd, smsURL, proxyURL);
            } else {
                credsProvider = getCredentialsProvider(smsUsr, smsPwd, proxyUsr, proxyPwd, smsURL, proxyURL);
            }

            HttpHost target = new HttpHost(smsURL.getHost(), smsURL.getPort(), smsURL.getProtocol());
            HttpHost proxy = (proxyURL != null) ? new HttpHost(proxyURL.getHost(), proxyURL.getPort(), proxyURL.getProtocol()) : null;

            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();

            RequestConfig requestConfig;
                requestConfig = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();

            String httpMethod = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_METHOD);
            String smsText = createMessage(code, mobileNumber, config);
            if(httpMethod.equals(HttpMethod.GET)) {

                String path = getPath(mobileNumber, smsURL, smsText);

                HttpGet httpGet = new HttpGet(path);
                httpGet.setConfig(requestConfig);
                if(isNotEmpty(contentType)) {
                    httpGet.addHeader("Content-type", contentType);
                }

                LOG.debug("Executing request " + httpGet.getRequestLine() + " to " + target + " via " + proxy);

                CloseableHttpResponse response = httpClient.execute(target, httpGet);
                StatusLine sl = response.getStatusLine();
                response.close();
                if(sl.getStatusCode() != 200) {
                    LOG.error("SMS code for " + mobileNumber + " could not be sent: " + sl.getStatusCode() +  " - " + sl.getReasonPhrase());
                }
                return sl.getStatusCode() == 200;

            } else if (httpMethod.equals(HttpMethod.POST)) {

                String path = getPath(mobileNumber, smsURL, smsText);
                String uri = smsURL.getProtocol() + "://" + smsURL.getHost() + ":" + smsURL.getPort() + path;

                HttpPost httpPost = new HttpPost(uri);
                httpPost.setConfig(requestConfig);
                if(isNotEmpty(contentType)) {
                    httpPost.addHeader("Content-type", contentType);
                }

                HttpEntity entity = new ByteArrayEntity(smsText.getBytes("UTF-8"));
                httpPost.setEntity(entity);

                CloseableHttpResponse response = httpClient.execute(httpPost);
                StatusLine sl = response.getStatusLine();
                response.close();
                if(sl.getStatusCode() != 200) {
                    LOG.error("SMS code for " + mobileNumber + " could not be sent: " + sl.getStatusCode() +  " - " + sl.getReasonPhrase());
                }
                return sl.getStatusCode() == 200;
            }
            return true;
        } catch (IOException e) {
            LOG.error(e);
            return false;
        } finally {
            if(httpClient != null) {
                try {
                    httpClient.close();
                } catch(IOException ignore) {
                    // Ignore ...
                }
            }
        }
    }


    private String getPath(String mobileNumber, URL smsURL, String smsText) throws UnsupportedEncodingException {
        String path = smsURL.getPath();
        if(smsURL.getQuery() != null && smsURL.getQuery().length() > 0) {
            path += smsURL.getQuery();
        }
        path = path.replaceFirst("\\{message\\}", URLEncoder.encode(smsText, "UTF-8"));
        path = path.replaceFirst("\\{phonenumber\\}", URLEncoder.encode(mobileNumber, "UTF-8"));
        return path;
    }

    private CredentialsProvider getCredentialsProvider(String smsUsr, String smsPwd, String proxyUsr, String proxyPwd, URL smsURL, URL proxyURL) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        // If defined, add BASIC Authentication parameters
        if (isNotEmpty(smsUsr) && isNotEmpty(smsPwd)) {
            credsProvider.setCredentials(
                    new AuthScope(smsURL.getHost(), smsURL.getPort()),
                    new UsernamePasswordCredentials(smsUsr, smsPwd));

        }

        // If defined, add Proxy Authentication parameters
        if (isNotEmpty(proxyUsr) && isNotEmpty(proxyPwd)) {
            credsProvider.setCredentials(
                    new AuthScope(proxyURL.getHost(), proxyURL.getPort()),
                    new UsernamePasswordCredentials(proxyUsr, proxyPwd));

        }
        return credsProvider;
    }

    private String createMessage(String code, String mobileNumber, AuthenticatorConfigModel config) {
        String text = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_TEXT);
        text = text.replaceAll("%sms-code%", code);
        text = text.replaceAll("%phonenumber%", mobileNumber);

        if(SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_AUTHTYPE, "").equals(SMSAuthenticatorContstants.AUTH_METHOD_INMESSAGE)) {
            String smsUsr = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_USERNAME);
            String smsPwd = SMSAuthenticatorUtil.getConfigString(config, SMSAuthenticatorContstants.CONF_PRP_SMS_PASSWORD);

            text = text.replaceAll("%user%", smsUsr);
            text = text.replaceAll("%password%", smsPwd);
        }

        return text;
    }

    private boolean isNotEmpty(String s) {
        return (s != null && s.length() > 0);
    }

}
