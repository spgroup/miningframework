package com.android.launcher3;

import static com.android.launcher3.LauncherAppState.ACTION_FORCE_ROLOAD;
import static com.android.launcher3.config.FeatureFlags.IS_DOGFOOD_BUILD;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.Nullable;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.icons.IconCache;
import com.android.launcher3.icons.LauncherIcons;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.model.AddWorkspaceItemsTask;
import com.android.launcher3.model.AllAppsList;
import com.android.launcher3.model.BaseModelUpdateTask;
import com.android.launcher3.model.BgDataModel;
import com.android.launcher3.model.BgDataModel.Callbacks;
import com.android.launcher3.model.CacheDataUpdatedTask;
import com.android.launcher3.model.LoaderResults;
import com.android.launcher3.model.LoaderTask;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.model.PackageInstallStateChangedTask;
import com.android.launcher3.model.PackageUpdatedTask;
import com.android.launcher3.model.ShortcutsChangedTask;
import com.android.launcher3.model.UserLockStateChangedTask;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.util.IntSparseArrayMap;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.Thunk;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class LauncherModel extends BroadcastReceiver implements LauncherAppsCompat.OnAppsChangedCallbackCompat {

    private static final boolean DEBUG_RECEIVER = false;

    static final String TAG = "Launcher.Model";

    @Thunk
    final LauncherAppState mApp;

    @Thunk
    final Object mLock = new Object();

    @Thunk
    LoaderTask mLoaderTask;

    @Thunk
    boolean mIsLoaderTaskRunning;

    private boolean mModelLoaded;

    public boolean isModelLoaded() {
        synchronized (mLock) {
            return mModelLoaded && mLoaderTask == null;
        }
    }

    @Thunk
    WeakReference<Callbacks> mCallbacks;

    private final AllAppsList mBgAllAppsList;

    static final BgDataModel sBgDataModel = new BgDataModel();

    private final Runnable mShortcutPermissionCheckRunnable = new Runnable() {

        @Override
        public void run() {
            if (mModelLoaded) {
                boolean hasShortcutHostPermission = DeepShortcutManager.getInstance(mApp.getContext()).hasHostPermission();
                if (hasShortcutHostPermission != sBgDataModel.hasShortcutHostPermission) {
                    forceReload();
                }
            }
        }
    };

    LauncherModel(LauncherAppState app, IconCache iconCache, AppFilter appFilter) {
        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache, appFilter);
    }

    public void setPackageState(PackageInstallInfo installInfo) {
        enqueueModelUpdateTask(new PackageInstallStateChangedTask(installInfo));
    }

    public void updateSessionDisplayInfo(final String packageName, final UserHandle user) {
        HashSet<String> packages = new HashSet<>();
        packages.add(packageName);
        enqueueModelUpdateTask(new CacheDataUpdatedTask(CacheDataUpdatedTask.OP_SESSION_UPDATE, user, packages));
    }

    public void addAndBindAddedWorkspaceItems(List<Pair<ItemInfo, Object>> itemList) {
        Callbacks callbacks = getCallback();
        if (callbacks != null) {
            callbacks.preAddApps();
        }
        enqueueModelUpdateTask(new AddWorkspaceItemsTask(itemList));
    }

    public ModelWriter getWriter(boolean hasVerticalHotseat, boolean verifyChanges) {
        return new ModelWriter(mApp.getContext(), this, sBgDataModel, hasVerticalHotseat, verifyChanges);
    }

    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            Preconditions.assertUIThread();
            mCallbacks = new WeakReference<>(callbacks);
        }
    }

    @Override
    public void onPackageChanged(String packageName, UserHandle user) {
        int op = PackageUpdatedTask.OP_UPDATE;
        enqueueModelUpdateTask(new PackageUpdatedTask(op, user, packageName));
    }

    public void onSessionFailure(String packageName, UserHandle user) {
        enqueueModelUpdateTask(new BaseModelUpdateTask() {

            @Override
            public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
                final IntSparseArrayMap<Boolean> removedIds = new IntSparseArrayMap<>();
                synchronized (dataModel) {
                    for (ItemInfo info : dataModel.itemsIdMap) {
                        if (info instanceof WorkspaceItemInfo && ((WorkspaceItemInfo) info).hasPromiseIconUi() && user.equals(info.user) && info.getIntent() != null && TextUtils.equals(packageName, info.getIntent().getPackage())) {
                            removedIds.put(info.id, true);
                        }
                    }
                }
                if (!removedIds.isEmpty()) {
                    deleteAndBindComponentsRemoved(ItemInfoMatcher.ofItemIds(removedIds, false));
                }
            }
        });
    }

    @Override
    public void onPackageRemoved(String packageName, UserHandle user) {
        onPackagesRemoved(user, packageName);
    }

    public void onPackagesRemoved(UserHandle user, String... packages) {
        int op = PackageUpdatedTask.OP_REMOVE;
        FileLog.d(TAG, "package removed received " + String.join("," + packages));
        enqueueModelUpdateTask(new PackageUpdatedTask(op, user, packages));
    }

    @Override
    public void onPackageAdded(String packageName, UserHandle user) {
        int op = PackageUpdatedTask.OP_ADD;
        enqueueModelUpdateTask(new PackageUpdatedTask(op, user, packageName));
    }

    @Override
    public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
        enqueueModelUpdateTask(new PackageUpdatedTask(PackageUpdatedTask.OP_UPDATE, user, packageNames));
    }

    @Override
    public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
        if (!replacing) {
            enqueueModelUpdateTask(new PackageUpdatedTask(PackageUpdatedTask.OP_UNAVAILABLE, user, packageNames));
        }
    }

    @Override
    public void onPackagesSuspended(String[] packageNames, UserHandle user) {
        enqueueModelUpdateTask(new PackageUpdatedTask(PackageUpdatedTask.OP_SUSPEND, user, packageNames));
    }

    @Override
    public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
        enqueueModelUpdateTask(new PackageUpdatedTask(PackageUpdatedTask.OP_UNSUSPEND, user, packageNames));
    }

    @Override
    public void onShortcutsChanged(String packageName, List<ShortcutInfo> shortcuts, UserHandle user) {
        enqueueModelUpdateTask(new ShortcutsChangedTask(packageName, shortcuts, user, true));
    }

    public void updatePinnedShortcuts(String packageName, List<ShortcutInfo> shortcuts, UserHandle user) {
        enqueueModelUpdateTask(new ShortcutsChangedTask(packageName, shortcuts, user, false));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_RECEIVER)
            Log.d(TAG, "onReceive intent=" + intent);
        final String action = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            forceReload();
        } else if (Intent.ACTION_MANAGED_PROFILE_ADDED.equals(action) || Intent.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
            UserManagerCompat.getInstance(context).enableAndResetCache();
            forceReload();
        } else if (Intent.ACTION_MANAGED_PROFILE_AVAILABLE.equals(action) || Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE.equals(action) || Intent.ACTION_MANAGED_PROFILE_UNLOCKED.equals(action)) {
            UserHandle user = intent.getParcelableExtra(Intent.EXTRA_USER);
            if (user != null) {
                if (Intent.ACTION_MANAGED_PROFILE_AVAILABLE.equals(action) || Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE.equals(action)) {
                    enqueueModelUpdateTask(new PackageUpdatedTask(PackageUpdatedTask.OP_USER_AVAILABILITY_CHANGE, user));
                }
                if (Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE.equals(action) || Intent.ACTION_MANAGED_PROFILE_UNLOCKED.equals(action)) {
                    enqueueModelUpdateTask(new UserLockStateChangedTask(user));
                }
            }
        } else if (IS_DOGFOOD_BUILD && ACTION_FORCE_ROLOAD.equals(action)) {
            Launcher l = (Launcher) getCallback();
            l.reload();
        }
    }

    public void forceReload() {
        forceReload(-1);
    }

    public void forceReload(int synchronousBindPage) {
        synchronized (mLock) {
            stopLoader();
            mModelLoaded = false;
        }
        Callbacks callbacks = getCallback();
        if (callbacks != null) {
            if (synchronousBindPage < 0) {
                synchronousBindPage = callbacks.getCurrentWorkspaceScreen();
            }
            startLoader(synchronousBindPage);
        }
    }

    public boolean isCurrentCallbacks(Callbacks callbacks) {
        return (mCallbacks != null && mCallbacks.get() == callbacks);
    }

    public boolean startLoader(int synchronousBindPage) {
        InstallShortcutReceiver.enableInstallQueue(InstallShortcutReceiver.FLAG_LOADER_RUNNING);
        synchronized (mLock) {
            if (mCallbacks != null && mCallbacks.get() != null) {
                final Callbacks oldCallbacks = mCallbacks.get();
                MAIN_EXECUTOR.execute(oldCallbacks::clearPendingBinds);
                stopLoader();
                LoaderResults loaderResults = new LoaderResults(mApp, sBgDataModel, mBgAllAppsList, synchronousBindPage, mCallbacks);
                if (mModelLoaded && !mIsLoaderTaskRunning) {
                    loaderResults.bindWorkspace();
                    loaderResults.bindAllApps();
                    loaderResults.bindDeepShortcuts();
                    loaderResults.bindWidgets();
                    return true;
                } else {
                    startLoaderForResults(loaderResults);
                }
            }
        }
        return false;
    }

    public void stopLoader() {
        synchronized (mLock) {
            LoaderTask oldTask = mLoaderTask;
            mLoaderTask = null;
            if (oldTask != null) {
                oldTask.stopLocked();
            }
        }
    }

    public void startLoaderForResults(LoaderResults results) {
        synchronized (mLock) {
            stopLoader();
            mLoaderTask = new LoaderTask(mApp, mBgAllAppsList, sBgDataModel, results);
            MODEL_EXECUTOR.post(mLoaderTask);
        }
    }

    public void startLoaderForResultsIfNotLoaded(LoaderResults results) {
        synchronized (mLock) {
            if (!isModelLoaded()) {
                Log.d(TAG, "Workspace not loaded, loading now");
                startLoaderForResults(results);
            }
        }
    }

    public void onInstallSessionCreated(final PackageInstallInfo sessionInfo) {
        enqueueModelUpdateTask(new BaseModelUpdateTask() {

            @Override
            public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
                apps.addPromiseApp(app.getContext(), sessionInfo);
                bindApplicationsIfNeeded();
            }
        });
    }

    public class LoaderTransaction implements AutoCloseable {

        private final LoaderTask mTask;

        private LoaderTransaction(LoaderTask task) throws CancellationException {
            synchronized (mLock) {
                if (mLoaderTask != task) {
                    throw new CancellationException("Loader already stopped");
                }
                mTask = task;
                mIsLoaderTaskRunning = true;
                mModelLoaded = false;
            }
        }

        public void commit() {
            synchronized (mLock) {
                mModelLoaded = true;
            }
        }

        @Override
        public void close() {
            synchronized (mLock) {
                if (mLoaderTask == mTask) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }
    }

    public LoaderTransaction beginLoader(LoaderTask task) throws CancellationException {
        return new LoaderTransaction(task);
    }

    public void refreshShortcutsIfRequired() {
        MODEL_EXECUTOR.getHandler().removeCallbacks(mShortcutPermissionCheckRunnable);
        MODEL_EXECUTOR.post(mShortcutPermissionCheckRunnable);
    }

    public void onPackageIconsUpdated(HashSet<String> updatedPackages, UserHandle user) {
        enqueueModelUpdateTask(new CacheDataUpdatedTask(CacheDataUpdatedTask.OP_CACHE_UPDATE, user, updatedPackages));
    }

    public void onWidgetLabelsUpdated(HashSet<String> updatedPackages, UserHandle user) {
        enqueueModelUpdateTask(new BaseModelUpdateTask() {

            @Override
            public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
                dataModel.widgetsModel.onPackageIconsUpdated(updatedPackages, user, app);
                bindUpdatedWidgets(dataModel);
            }
        });
    }

    public void enqueueModelUpdateTask(ModelUpdateTask task) {
        task.init(mApp, this, sBgDataModel, mBgAllAppsList, MAIN_EXECUTOR);
        MODEL_EXECUTOR.execute(task);
    }

    public interface CallbackTask {

        void execute(Callbacks callbacks);
    }

    public interface ModelUpdateTask extends Runnable {

        void init(LauncherAppState app, LauncherModel model, BgDataModel dataModel, AllAppsList allAppsList, Executor uiExecutor);
    }

    public void updateAndBindWorkspaceItem(WorkspaceItemInfo si, ShortcutInfo info) {
        updateAndBindWorkspaceItem(() -> {
            si.updateFromDeepShortcutInfo(info, mApp.getContext());
            LauncherIcons li = LauncherIcons.obtain(mApp.getContext());
            si.applyFrom(li.createShortcutIcon(info));
            li.recycle();
            return si;
        });
    }

    public void updateAndBindWorkspaceItem(final Supplier<WorkspaceItemInfo> itemProvider) {
        enqueueModelUpdateTask(new BaseModelUpdateTask() {

            @Override
            public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
                WorkspaceItemInfo info = itemProvider.get();
                getModelWriter().updateItemInDatabase(info);
                ArrayList<WorkspaceItemInfo> update = new ArrayList<>();
                update.add(info);
                bindUpdatedWorkspaceItems(update);
            }
        });
    }

    public void refreshAndBindWidgetsAndShortcuts(@Nullable final PackageUserKey packageUser) {
        enqueueModelUpdateTask(new BaseModelUpdateTask() {

            @Override
            public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
                dataModel.widgetsModel.update(app, packageUser);
                bindUpdatedWidgets(dataModel);
            }
        });
    }

    public void dumpState(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (args.length > 0 && TextUtils.equals(args[0], "--all")) {
            writer.println(prefix + "All apps list: size=" + mBgAllAppsList.data.size());
            for (AppInfo info : mBgAllAppsList.data) {
                writer.println(prefix + "   title=\"" + info.title + "\" iconBitmap=" + info.iconBitmap + " componentName=" + info.componentName.getPackageName());
            }
        }
        sBgDataModel.dump(prefix, fd, writer, args);
    }

    public Callbacks getCallback() {
        return mCallbacks != null ? mCallbacks.get() : null;
    }
}
