import java.io.File;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSName;
import sun.security.jgss.GSSUtil;
import sun.security.krb5.PrincipalName;

public class ReferralsTest {

    private static final boolean DEBUG = true;

    private static final String krbConfigName = "krb5-localkdc.conf";

    private static final String realmKDC1 = "RABBIT.HOLE";

    private static final String realmKDC2 = "DEV.RABBIT.HOLE";

    private static final char[] password = "123qwe@Z".toCharArray();

    private static final String clientName = "test";

    private static final String serviceName = "http" + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + "server.dev.rabbit.hole";

    private static final String clientAlias = clientName + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC1;

    private static final String clientKDC1Name = clientAlias.replaceAll(PrincipalName.NAME_REALM_SEPARATOR_STR, "\\\\" + PrincipalName.NAME_REALM_SEPARATOR_STR) + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC1;

    private static final String clientKDC2Name = clientName + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC2;

    private static final String serviceKDC2Name = serviceName + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC2;

    public static void main(String[] args) throws Exception {
        try {
            initializeKDCs();
            testSubjectCredentials();
            testDelegated();
        } finally {
            cleanup();
        }
    }

    private static void initializeKDCs() throws Exception {
        KDC kdc1 = KDC.create(realmKDC1, "localhost", 0, true);
        kdc1.addPrincipalRandKey(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC1);
        kdc1.addPrincipal(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC1 + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC2, password);
        kdc1.addPrincipal(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC2, password);
        KDC kdc2 = KDC.create(realmKDC2, "localhost", 0, true);
        kdc2.addPrincipalRandKey(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC2);
        kdc2.addPrincipal(clientKDC2Name, password);
        kdc2.addPrincipal(serviceName, password);
        kdc2.addPrincipal(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC1, password);
        kdc2.addPrincipal(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC2 + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC1, password);
        kdc1.registerAlias(clientAlias, kdc2);
        kdc1.registerAlias(serviceName, kdc2);
        kdc2.registerAlias(clientAlias, clientKDC2Name);
        Map<String, List<String>> mapKDC2 = new HashMap<>();
        mapKDC2.put(serviceName + "@" + realmKDC2, Arrays.asList(new String[] { serviceName + "@" + realmKDC2 }));
        kdc2.setOption(KDC.Option.ALLOW_S4U2PROXY, mapKDC2);
        KDC.saveConfig(krbConfigName, kdc1, kdc2, "forwardable=true");
        System.setProperty("java.security.krb5.conf", krbConfigName);
    }

    private static void cleanup() {
        File f = new File(krbConfigName);
        if (f.exists()) {
            f.delete();
        }
    }

    private static void testSubjectCredentials() throws Exception {
        Subject clientSubject = new Subject();
        Context clientContext = Context.fromUserPass(clientSubject, clientKDC1Name, password, false);
        Set<Principal> clientPrincipals = clientSubject.getPrincipals();
        if (clientPrincipals.size() != 1) {
            throw new Exception("Only one client subject principal expected");
        }
        Principal clientPrincipal = clientPrincipals.iterator().next();
        if (DEBUG) {
            System.out.println("Client subject principal: " + clientPrincipal.getName());
        }
        if (!clientPrincipal.getName().equals(clientKDC1Name)) {
            throw new Exception("Unexpected client subject principal.");
        }
        clientContext.startAsClient(serviceName, GSSUtil.GSS_KRB5_MECH_OID);
        clientContext.take(new byte[0]);
        Set<KerberosTicket> clientTickets = clientSubject.getPrivateCredentials(KerberosTicket.class);
        boolean tgtFound = false;
        boolean tgsFound = false;
        for (KerberosTicket clientTicket : clientTickets) {
            String cname = clientTicket.getClient().getName();
            String sname = clientTicket.getServer().getName();
            if (cname.equals(clientKDC2Name)) {
                if (sname.equals(PrincipalName.TGS_DEFAULT_SRV_NAME + PrincipalName.NAME_COMPONENT_SEPARATOR_STR + realmKDC2 + PrincipalName.NAME_REALM_SEPARATOR_STR + realmKDC2)) {
                    tgtFound = true;
                } else if (sname.equals(serviceKDC2Name)) {
                    tgsFound = true;
                }
            }
            if (DEBUG) {
                System.out.println("Client subject KerberosTicket:");
                System.out.println(clientTicket);
            }
        }
        if (!tgtFound || !tgsFound) {
            throw new Exception("client subject tickets (TGT/TGS) not found.");
        }
        int numOfTickets = clientTickets.size();
        clientContext.startAsClient(serviceName, GSSUtil.GSS_KRB5_MECH_OID);
        clientContext.take(new byte[0]);
        clientContext.status();
        int newNumOfTickets = clientSubject.getPrivateCredentials(KerberosTicket.class).size();
        if (DEBUG) {
            System.out.println("client subject number of tickets: " + numOfTickets);
            System.out.println("client subject new number of tickets: " + newNumOfTickets);
        }
        if (numOfTickets != newNumOfTickets) {
            throw new Exception("Useless client subject TGS request because" + " TGS was not found in private credentials.");
        }
    }

    private static void testDelegated() throws Exception {
        Context c = Context.fromUserPass(clientKDC2Name, password, false);
        c.startAsClient(serviceName, GSSUtil.GSS_KRB5_MECH_OID);
        Context s = Context.fromUserPass(serviceKDC2Name, password, true);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        Context delegatedContext = s.delegated();
        delegatedContext.startAsClient(serviceName, GSSUtil.GSS_KRB5_MECH_OID);
        delegatedContext.x().requestMutualAuth(false);
        Context s2 = Context.fromUserPass(serviceKDC2Name, password, true);
        s2.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(delegatedContext, s2);
        if (!delegatedContext.x().isEstablished() || !s2.x().isEstablished()) {
            throw new Exception("Delegated authentication failed");
        }
        GSSName contextInitiatorName = delegatedContext.x().getSrcName();
        GSSName contextAcceptorName = delegatedContext.x().getTargName();
        if (DEBUG) {
            System.out.println("Context initiator: " + contextInitiatorName);
            System.out.println("Context acceptor: " + contextAcceptorName);
        }
        if (!contextInitiatorName.toString().equals(clientKDC2Name) || !contextAcceptorName.toString().equals(serviceName)) {
            throw new Exception("Unexpected initiator or acceptor names");
        }
    }
}
