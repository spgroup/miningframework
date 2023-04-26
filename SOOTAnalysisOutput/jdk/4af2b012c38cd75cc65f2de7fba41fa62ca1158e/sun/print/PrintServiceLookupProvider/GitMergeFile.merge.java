package sun.print;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;

public class PrintServiceLookupProvider extends PrintServiceLookup {

    private String defaultPrinter;

    private PrintService defaultPrintService;

    private String[] printers;

    private PrintService[] printServices;

    private static final int DEFAULT_REFRESH_TIME = 240;

    private static final int MINIMUM_REFRESH_TIME = 120;

    private static final boolean pollServices;

    private static final int refreshTime;

    static {
        String pollStr = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.java2d.print.polling"));
        pollServices = !("false".equalsIgnoreCase(pollStr));
        String refreshTimeStr = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.java2d.print.minRefreshTime"));
        refreshTime = (refreshTimeStr != null) ? getRefreshTime(refreshTimeStr) : DEFAULT_REFRESH_TIME;
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("awt");
                return null;
            }
        });
    }

    private static int getRefreshTime(final String refreshTimeStr) {
        try {
            int minRefreshTime = Integer.parseInt(refreshTimeStr);
            return (minRefreshTime < MINIMUM_REFRESH_TIME) ? MINIMUM_REFRESH_TIME : minRefreshTime;
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_TIME;
        }
    }

    private static PrintServiceLookupProvider win32PrintLUS;

    public static PrintServiceLookupProvider getWin32PrintLUS() {
        if (win32PrintLUS == null) {
            PrintServiceLookup.lookupDefaultPrintService();
        }
        return win32PrintLUS;
    }

    public PrintServiceLookupProvider() {
        if (win32PrintLUS == null) {
            win32PrintLUS = this;
            String osName = AccessController.doPrivileged(new sun.security.action.GetPropertyAction("os.name"));
            if (osName != null && osName.startsWith("Windows 98")) {
                return;
            }
            Thread thr = new Thread(null, new PrinterChangeListener(), "PrinterListener", 0, false);
            thr.setDaemon(true);
            thr.start();
            if (pollServices) {
                Thread remThr = new Thread(null, new RemotePrinterChangeListener(), "RemotePrinterListener", 0, false);
                remThr.setDaemon(true);
                remThr.start();
            }
        }
    }

    public synchronized PrintService[] getPrintServices() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        if (printServices == null) {
            refreshServices();
        }
        return printServices;
    }

    private synchronized void refreshServices() {
        printers = getAllPrinterNames();
        if (printers == null) {
            printServices = new PrintService[0];
            return;
        }
        PrintService[] newServices = new PrintService[printers.length];
        PrintService defService = getDefaultPrintService();
        for (int p = 0; p < printers.length; p++) {
            if (defService != null && printers[p].equals(defService.getName())) {
                newServices[p] = defService;
            } else {
                if (printServices == null) {
                    newServices[p] = new Win32PrintService(printers[p]);
                } else {
                    int j;
                    for (j = 0; j < printServices.length; j++) {
                        if ((printServices[j] != null) && (printers[p].equals(printServices[j].getName()))) {
                            newServices[p] = printServices[j];
                            printServices[j] = null;
                            break;
                        }
                    }
                    if (j == printServices.length) {
                        newServices[p] = new Win32PrintService(printers[p]);
                    }
                }
            }
        }
        if (printServices != null) {
            for (int j = 0; j < printServices.length; j++) {
                if ((printServices[j] instanceof Win32PrintService) && (!printServices[j].equals(defaultPrintService))) {
                    ((Win32PrintService) printServices[j]).invalidateService();
                }
            }
        }
        printServices = newServices;
    }

    public synchronized PrintService getPrintServiceByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        } else {
            PrintService[] printServices = getPrintServices();
            for (int i = 0; i < printServices.length; i++) {
                if (printServices[i].getName().equals(name)) {
                    return printServices[i];
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    boolean matchingService(PrintService service, PrintServiceAttributeSet serviceSet) {
        if (serviceSet != null) {
            Attribute[] attrs = serviceSet.toArray();
            Attribute serviceAttr;
            for (int i = 0; i < attrs.length; i++) {
                serviceAttr = service.getAttribute((Class<PrintServiceAttribute>) attrs[i].getCategory());
                if (serviceAttr == null || !serviceAttr.equals(attrs[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        PrintRequestAttributeSet requestSet = null;
        PrintServiceAttributeSet serviceSet = null;
        if (attributes != null && !attributes.isEmpty()) {
            requestSet = new HashPrintRequestAttributeSet();
            serviceSet = new HashPrintServiceAttributeSet();
            Attribute[] attrs = attributes.toArray();
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i] instanceof PrintRequestAttribute) {
                    requestSet.add(attrs[i]);
                } else if (attrs[i] instanceof PrintServiceAttribute) {
                    serviceSet.add(attrs[i]);
                }
            }
        }
        PrintService[] services = null;
        if (serviceSet != null && serviceSet.get(PrinterName.class) != null) {
            PrinterName name = (PrinterName) serviceSet.get(PrinterName.class);
            PrintService service = getPrintServiceByName(name.getValue());
            if (service == null || !matchingService(service, serviceSet)) {
                services = new PrintService[0];
            } else {
                services = new PrintService[1];
                services[0] = service;
            }
        } else {
            services = getPrintServices();
        }
        if (services.length == 0) {
            return services;
        } else {
            ArrayList<PrintService> matchingServices = new ArrayList<>();
            for (int i = 0; i < services.length; i++) {
                try {
                    if (services[i].getUnsupportedAttributes(flavor, requestSet) == null) {
                        matchingServices.add(services[i]);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
            services = new PrintService[matchingServices.size()];
            return matchingServices.toArray(services);
        }
    }

    public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors, AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        return new MultiDocPrintService[0];
    }

    public synchronized PrintService getDefaultPrintService() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }
        defaultPrinter = getDefaultPrinterName();
        if (defaultPrinter == null) {
            return null;
        }
        if ((defaultPrintService != null) && defaultPrintService.getName().equals(defaultPrinter)) {
            return defaultPrintService;
        }
        defaultPrintService = null;
        if (printServices != null) {
            for (int j = 0; j < printServices.length; j++) {
                if (defaultPrinter.equals(printServices[j].getName())) {
                    defaultPrintService = printServices[j];
                    break;
                }
            }
        }
        if (defaultPrintService == null) {
            defaultPrintService = new Win32PrintService(defaultPrinter);
        }
        return defaultPrintService;
    }

    class PrinterChangeListener implements Runnable {

        long chgObj;

        PrinterChangeListener() {
            chgObj = notifyFirstPrinterChange(null);
        }

        @Override
        public void run() {
            if (chgObj != -1) {
                while (true) {
                    if (notifyPrinterChange(chgObj) != 0) {
                        try {
                            refreshServices();
                        } catch (SecurityException se) {
                            break;
                        }
                    } else {
                        notifyClosePrinterChange(chgObj);
                        break;
                    }
                }
            }
        }
    }

    class RemotePrinterChangeListener implements Comparator<String>, Runnable {

        RemotePrinterChangeListener() {
        }

        @Override
        public int compare(String o1, String o2) {
            return ((o1 == null) ? ((o2 == null) ? 0 : 1) : ((o2 == null) ? -1 : o1.compareTo(o2)));
        }

        @Override
        public void run() {
            String[] prevRemotePrinters = getRemotePrintersNames();
            if (prevRemotePrinters != null) {
                Arrays.sort(prevRemotePrinters, this);
            }
            while (true) {
                try {
                    Thread.sleep(refreshTime * 1000);
                } catch (InterruptedException e) {
                    break;
                }
                String[] currentRemotePrinters = getRemotePrintersNames();
                if (currentRemotePrinters != null) {
                    Arrays.sort(currentRemotePrinters, this);
                }
                if (!Arrays.equals(prevRemotePrinters, currentRemotePrinters)) {
                    refreshServices();
                    prevRemotePrinters = currentRemotePrinters;
                }
            }
        }
    }

    private native String getDefaultPrinterName();

    private native String[] getAllPrinterNames();

    private native long notifyFirstPrinterChange(String printer);

    private native void notifyClosePrinterChange(long chgObj);

    private native int notifyPrinterChange(long chgObj);

    private native String[] getRemotePrintersNames();
}
