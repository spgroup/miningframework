package edu.harvard.iq.dataverse.dashboard;

import edu.harvard.iq.dataverse.DataverseRequestServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.EjbDataverseEngine;
import edu.harvard.iq.dataverse.PermissionsWrapper;
import edu.harvard.iq.dataverse.UserServiceBean;
import edu.harvard.iq.dataverse.api.Admin;
import edu.harvard.iq.dataverse.authorization.AuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.impl.GrantSuperuserStatusCommand;
import edu.harvard.iq.dataverse.engine.command.impl.RevokeAllRolesCommand;
import edu.harvard.iq.dataverse.engine.command.impl.RevokeSuperuserStatusCommand;
import edu.harvard.iq.dataverse.mydata.Pager;
import edu.harvard.iq.dataverse.userdata.UserListMaker;
import edu.harvard.iq.dataverse.userdata.UserListResult;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.JsfHelper;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ViewScoped
@Named("DashboardUsersPage")
public class DashboardUsersPage implements java.io.Serializable {

    @EJB
    AuthenticationServiceBean authenticationService;

    @EJB
    UserServiceBean userService;

    @Inject
    DataverseSession session;

    @Inject
    PermissionsWrapper permissionsWrapper;

    @EJB
    EjbDataverseEngine commandEngine;

    @Inject
    DataverseRequestServiceBean dvRequestService;

    private static final Logger logger = Logger.getLogger(DashboardUsersPage.class.getCanonicalName());

    private AuthenticatedUser authUser = null;

    private Integer selectedPage = 1;

    private UserListMaker userListMaker = null;

    private Pager pager;

    private List<AuthenticatedUser> userList;

    private String searchTerm;

    public String init() {
        if ((session.getUser() != null) && (session.getUser().isAuthenticated()) && (session.getUser().isSuperuser())) {
            authUser = (AuthenticatedUser) session.getUser();
            userListMaker = new UserListMaker(userService);
            runUserSearch();
        } else {
            return permissionsWrapper.notAuthorized();
        }
        return null;
    }

    public boolean runUserSearchWithPage(Integer pageNumber) {
        System.err.println("runUserSearchWithPage");
        setSelectedPage(pageNumber);
        runUserSearch();
        return true;
    }

    public boolean runUserSearch() {
        logger.fine("Run the search!");
        UserListResult userListResult = userListMaker.runUserSearch(searchTerm, UserListMaker.ITEMS_PER_PAGE, getSelectedPage(), null);
        if (userListResult == null) {
            try {
                throw new Exception("userListResult should not be null!");
            } catch (Exception ex) {
                Logger.getLogger(DashboardUsersPage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        setSelectedPage(userListResult.getSelectedPageNumber());
        this.userList = userListResult.getUserList();
        this.pager = userListResult.getPager();
        return true;
    }

    public String getListUsersAPIPath() {
        return Admin.listUsersFullAPIPath;
    }

    public String getUserCount() {
        return NumberFormat.getInstance().format(userService.getTotalUserCount());
    }

    public Long getSuperUserCount() {
        return userService.getSuperUserCount();
    }

    public List<AuthenticatedUser> getUserList() {
        return this.userList;
    }

    public Pager getPager() {
        return this.pager;
    }

    public void setSelectedPage(Integer pgNum) {
        if ((pgNum == null) || (pgNum < 1)) {
            this.selectedPage = 1;
        }
        selectedPage = pgNum;
    }

    public Integer getSelectedPage() {
        if ((selectedPage == null) || (selectedPage < 1)) {
            setSelectedPage(null);
        }
        return selectedPage;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    AuthenticatedUser selectedUserDetached = null;

    AuthenticatedUser selectedUserPersistent = null;

    public void setSelectedUserDetached(AuthenticatedUser user) {
        this.selectedUserDetached = user;
    }

    public AuthenticatedUser getSelectedUserDetached() {
        return this.selectedUserDetached;
    }

    public void setUserToToggleSuperuserStatus(AuthenticatedUser user) {
        selectedUserDetached = user;
    }

    public void saveSuperuserStatus() {
        logger.fine("Get persisent AuthenticatedUser for id: " + selectedUserDetached.getId());
        selectedUserPersistent = userService.find(selectedUserDetached.getId());
        if (selectedUserPersistent != null) {
            logger.fine("Toggling user's " + selectedUserDetached.getIdentifier() + " superuser status; (current status: " + selectedUserDetached.isSuperuser() + ")");
            logger.fine("Attempting to save user " + selectedUserDetached.getIdentifier());
            logger.fine("selectedUserPersistent info: " + selectedUserPersistent.getId() + " set to: " + selectedUserDetached.isSuperuser());
            selectedUserPersistent.setSuperuser(selectedUserDetached.isSuperuser());
            try {
                if (!selectedUserPersistent.isSuperuser()) {
                    commandEngine.submit(new RevokeSuperuserStatusCommand(selectedUserPersistent, dvRequestService.getDataverseRequest()));
                } else {
                    commandEngine.submit(new GrantSuperuserStatusCommand(selectedUserPersistent, dvRequestService.getDataverseRequest()));
                }
            } catch (Exception ex) {
                logger.warning("Failed to permanently toggle the superuser status for user " + selectedUserDetached.getIdentifier() + ": " + ex.getMessage());
            }
        } else {
            logger.warning("selectedUserPersistent is null.  AuthenticatedUser not found for id: " + selectedUserDetached.getId());
        }
    }

    public void cancelSuperuserStatusChange() {
        selectedUserDetached.setSuperuser(!selectedUserDetached.isSuperuser());
        selectedUserPersistent = null;
    }

    public void removeUserRoles() {
        logger.fine("Get persisent AuthenticatedUser for id: " + selectedUserDetached.getId());
        selectedUserPersistent = userService.find(selectedUserDetached.getId());
        selectedUserDetached.setRoles(null);
        try {
            commandEngine.submit(new RevokeAllRolesCommand(selectedUserPersistent, dvRequestService.getDataverseRequest()));
        } catch (Exception ex) {
            JsfHelper.addErrorMessage(BundleUtil.getStringFromBundle("dashboard.list_users.removeAll.message.failure", Arrays.asList(selectedUserPersistent.getUserIdentifier())));
            return;
        }
        JsfHelper.addSuccessMessage(BundleUtil.getStringFromBundle("dashboard.list_users.removeAll.message.success", Arrays.asList(selectedUserPersistent.getUserIdentifier())));
    }

    public String getConfirmRemoveRolesMessage() {
        if (selectedUserDetached != null) {
            return BundleUtil.getStringFromBundle("dashboard.list_users.tbl_header.roles.removeAll.confirmationText", Arrays.asList(selectedUserDetached.getUserIdentifier()));
        }
        return BundleUtil.getStringFromBundle("dashboard.list_users.tbl_header.roles.removeAll.confirmationText");
    }

    public String getAuthProviderFriendlyName(String authProviderId) {
        return AuthenticationProvider.getFriendlyName(authProviderId);
    }
}
