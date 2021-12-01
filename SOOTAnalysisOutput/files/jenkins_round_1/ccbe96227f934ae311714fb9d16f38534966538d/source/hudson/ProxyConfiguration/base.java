/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson;

import com.google.common.collect.Lists;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.Scrambler;
import hudson.util.Secret;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import com.thoughtworks.xstream.XStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jvnet.robust_http_client.RetryableHttpStream;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * HTTP proxy configuration.
 *
 * <p>
 * Use {@link #open(URL)} to open a connection with the proxy setting.
 * <p>
 * Proxy authentication (including NTLM) is implemented by setting a default
 * {@link Authenticator} which provides a {@link PasswordAuthentication}
 * (as described in the Java 6 tech note 
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/http-auth.html">
 * Http Authentication</a>).
 * 
 * @see jenkins.model.Jenkins#proxy
 */
public final class ProxyConfiguration extends AbstractDescribableImpl<ProxyConfiguration> implements Saveable, Serializable {
    public final String name;
    public final int port;

    /**
     * Possibly null proxy user name.
     */
    private final String userName;

    /**
     * List of host names that shouldn't use proxy, as typed by users.
     *
     * @see #getNoProxyHostPatterns() 
     */
    public final String noProxyHost;

    @Deprecated
    private String password;

    /**
     * encrypted password
     */
    private Secret secretPassword;

    public ProxyConfiguration(String name, int port) {
        this(name,port,null,null);
    }

    public ProxyConfiguration(String name, int port, String userName, String password) {
        this(name,port,userName,password,null);
    }

    @DataBoundConstructor
    public ProxyConfiguration(String name, int port, String userName, String password, String noProxyHost) {
        this.name = Util.fixEmptyAndTrim(name);
        this.port = port;
        this.userName = Util.fixEmptyAndTrim(userName);
        this.secretPassword = Secret.fromString(password);
        this.noProxyHost = Util.fixEmptyAndTrim(noProxyHost);
    }

    public String getUserName() {
        return userName;
    }

//    This method is public, if it was public only for jelly, then should make it private (or inline contents)
//    Have left public, as can't tell if anyone else is using from plugins
    /**
     * @return the password in plain text
     */
    public String getPassword() {
        return Secret.toString(secretPassword);
    }

    public String getEncryptedPassword() {
        return (secretPassword == null) ? null : secretPassword.getEncryptedValue();
    }

    /**
     * Returns the list of properly formatted no proxy host names.
     */
    public List<Pattern> getNoProxyHostPatterns() {
        return getNoProxyHostPatterns(noProxyHost);
    }

    /**
     * Returns the list of properly formatted no proxy host names.
     */
    public static List<Pattern> getNoProxyHostPatterns(String noProxyHost) {
        if (noProxyHost==null)  return Collections.emptyList();

        List<Pattern> r = Lists.newArrayList();
        for (String s : noProxyHost.split("[ \t\n,|]+")) {
            if (s.length()==0)  continue;
            r.add(Pattern.compile(s.replace(".", "\\.").replace("*", ".*")));
        }
        return r;
    }

    /**
     * @deprecated
     *      Use {@link #createProxy(String)}
     */
    public Proxy createProxy() {
        return createProxy(null);
    }

    public Proxy createProxy(String host) {
        return createProxy(host, name, port, noProxyHost);
    }

    public static Proxy createProxy(String host, String name, int port, String noProxyHost) {
        if (host!=null && noProxyHost!=null) {
            for (Pattern p : getNoProxyHostPatterns(noProxyHost)) {
                if (p.matcher(host).matches())
                    return Proxy.NO_PROXY;
            }
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(name,port));
    }

    public void save() throws IOException {
        if(BulkChange.contains(this))   return;
        XmlFile config = getXmlFile();
        config.write(this);
        SaveableListener.fireOnChange(this, config);
    }

    public Object readResolve() {
        if (secretPassword == null)
            // backward compatibility : get crambled password and store it encrypted
            secretPassword = Secret.fromString(Scrambler.descramble(password));
        password = null;
        return this;
    }

    public static XmlFile getXmlFile() {
        return new XmlFile(XSTREAM, new File(Jenkins.getInstance().getRootDir(), "proxy.xml"));
    }

    public static ProxyConfiguration load() throws IOException {
        XmlFile f = getXmlFile();
        if(f.exists())
            return (ProxyConfiguration) f.read();
        else
            return null;
    }

    /**
     * This method should be used wherever {@link URL#openConnection()} to internet URLs is invoked directly.
     */
    public static URLConnection open(URL url) throws IOException {
        Jenkins h = Jenkins.getInstance(); // this code might run on slaves
        ProxyConfiguration p = h!=null ? h.proxy : null;
        if(p==null)
            return url.openConnection();

        URLConnection con = url.openConnection(p.createProxy(url.getHost()));
        if(p.getUserName()!=null) {
        	// Add an authenticator which provides the credentials for proxy authentication
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestorType()!=RequestorType.PROXY)    return null;
                    ProxyConfiguration p = Jenkins.getInstance().proxy;
                    return new PasswordAuthentication(p.getUserName(),
                            p.getPassword().toCharArray());
                }
            });
        }

        for (URLConnectionDecorator d : URLConnectionDecorator.all())
            d.decorate(con);

        return con;
    }
    
    public static InputStream getInputStream(URL url) throws IOException {
        Jenkins h = Jenkins.getInstance(); // this code might run on slaves
        final ProxyConfiguration p = (h != null) ? h.proxy : null;
        if (p == null) 
            return new RetryableHttpStream(url);

        InputStream is = new RetryableHttpStream(url, p.createProxy(url.getHost()));
        if (p.getUserName() != null) {
            // Add an authenticator which provides the credentials for proxy authentication
            Authenticator.setDefault(new Authenticator() {

                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestorType() != RequestorType.PROXY) {
                        return null;
                    }
                    return new PasswordAuthentication(p.getUserName(), p.getPassword().toCharArray());
                }
            });
        }
        
        return is;
    }

    private static final XStream XSTREAM = new XStream2();

    private static final long serialVersionUID = 1L;

    static {
        XSTREAM.alias("proxy", ProxyConfiguration.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ProxyConfiguration> {
        @Override
        public String getDisplayName() {
            return "Proxy Configuration";
        }

        public FormValidation doCheckPort(@QueryParameter String value) {
            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }
            int port;
            try {
                port = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(Messages.PluginManager_PortNotANumber());
            }
            if (port < 0 || port > 65535) {
                return FormValidation.error(Messages.PluginManager_PortNotInRange(0, 65535));
            }
            return FormValidation.ok();
        }
    }
}
