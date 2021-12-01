/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Seiji Sogabe
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
package hudson.security;

import hudson.Extension;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * {@link AuthorizationStrategy} that grants full-control to authenticated user
 * (other than anonymous users.)
 *
 * @author Kohsuke Kawaguchi
 */
public class FullControlOnceLoggedInAuthorizationStrategy extends AuthorizationStrategy {
    @DataBoundConstructor
    public FullControlOnceLoggedInAuthorizationStrategy() {
    }

    @Override
    public ACL getRootACL() {
        return THE_ACL;
    }

    public List<String> getGroups() {
        return Collections.emptyList();
    }

    private static final SparseACL THE_ACL = new SparseACL(null);

    static {
        THE_ACL.add(ACL.EVERYONE, Jenkins.ADMINISTER,true);
        THE_ACL.add(ACL.ANONYMOUS, Jenkins.ADMINISTER,false);
        THE_ACL.add(ACL.ANONYMOUS,Permission.READ,true);
    }

    /**
     * @deprecated as of 1.643
     *      Inject descriptor via {@link Inject}.
     */
    @Restricted(NoExternalUse.class)
    public static Descriptor<AuthorizationStrategy> DESCRIPTOR;

    @Extension
    public static class DescriptorImpl extends Descriptor<AuthorizationStrategy> {
        public DescriptorImpl() {
            DESCRIPTOR = this;
        }

        public String getDisplayName() {
            return Messages.FullControlOnceLoggedInAuthorizationStrategy_DisplayName();
        }
    }
}