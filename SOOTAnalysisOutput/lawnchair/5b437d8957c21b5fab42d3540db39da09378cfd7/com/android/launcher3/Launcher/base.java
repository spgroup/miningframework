package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.PagedView.PageSwitchListener;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Launcher extends Activity implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks, View.OnTouchListener, PageSwitchListener, LauncherProviderChangeListener {

    static final String TAG = "Launcher";

    static final boolean LOGD = false;

    static final boolean PROFILE_STARTUP = false;

    static final boolean DEBUG_WIDGETS = false;

    static final boolean DEBUG_STRICT_MODE = false;

    static final boolean DEBUG_RESUME_TIME = false;

    static final boolean DEBUG_DUMP_LOG = false;

    static final boolean ENABLE_DEBUG_INTENTS = false;

    private static final int REQUEST_CREATE_SHORTCUT = 1;

    private static final int REQUEST_CREATE_APPWIDGET = 5;

    private static final int REQUEST_PICK_SHORTCUT = 7;

    private static final int REQUEST_PICK_APPWIDGET = 9;

    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;

    private static final int REQUEST_RECONFIGURE_APPWIDGET = 12;

    protected static final int REQUEST_LAST = 100;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int SCREEN_COUNT = 5;

    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";

    static final String DISABLE_ALL_APPS_PROPERTY = "launcher_noallapps";

    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION = "com.android.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";

    private static final String RUNTIME_STATE = "launcher.state";

    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";

    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";

    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";

    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";

    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";

    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";

    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";

    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";

    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_ID = "launcher.add_widget_id";

    private static final String RUNTIME_STATE_VIEW_IDS = "launcher.view_ids";

    static final String INTRO_SCREEN_DISMISSED = "launcher.intro_screen_dismissed";

    static final String FIRST_RUN_ACTIVITY_DISPLAYED = "launcher.first_run_activity_displayed";

    static final String FIRST_LOAD_COMPLETE = "launcher.first_load_complete";

    static final String ACTION_FIRST_LOAD_COMPLETE = "com.android.launcher3.action.FIRST_LOAD_COMPLETE";

    public static final String SHOW_WEIGHT_WATCHER = "debug.show_mem";

    public static final boolean SHOW_WEIGHT_WATCHER_DEFAULT = false;

    private static final String QSB_WIDGET_ID = "qsb_widget_id";

    private static final String QSB_WIDGET_PROVIDER = "qsb_widget_provider";

    public static final String USER_HAS_MIGRATED = "launcher.user_migrated_from_old_data";

    private enum State {

        NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED
    }

    private State mState = State.WORKSPACE;

    private AnimatorSet mStateAnimation;

    private boolean mIsSafeModeEnabled;

    LauncherOverlayCallbacks mLauncherOverlayCallbacks = new LauncherOverlayCallbacksImpl();

    LauncherOverlay mLauncherOverlay;

    InsettableFrameLayout mLauncherOverlayContainer;

    static final int APPWIDGET_HOST_ID = 1024;

    public static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;

    private static final int ON_ACTIVITY_RESULT_ANIMATION_DELAY = 500;

    private static final int ACTIVITY_START_DELAY = 1000;

    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static int NEW_APPS_PAGE_MOVE_DELAY = 500;

    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;

    private static int NEW_APPS_ANIMATION_DELAY = 500;

    private static final int SINGLE_FRAME_DELAY = 16;

    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();

    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private Workspace mWorkspace;

    private View mLauncherView;

    private View mPageIndicators;

    private DragLayer mDragLayer;

    private DragController mDragController;

    private View mWeightWatcher;

    private AppWidgetManagerCompat mAppWidgetManager;

    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();

    private LauncherAppWidgetProviderInfo mPendingAddWidgetInfo;

    private int mPendingAddWidgetId = -1;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;

    private ViewGroup mOverviewPanel;

    private View mAllAppsButton;

    private SearchDropTargetBar mSearchDropTargetBar;

    private AppsCustomizeTabHost mAppsCustomizeTabHost;

    private AppsCustomizePagedView mAppsCustomizeContent;

    private boolean mAutoAdvanceRunning = false;

    private AppWidgetHostView mQsb;

    private Bundle mSavedState;

    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;

    private boolean mRestoring;

    private boolean mWaitingForResult;

    private boolean mOnResumeNeedsLoad;

    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();

    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;

    private IconCache mIconCache;

    private boolean mUserPresent = true;

    private boolean mVisible = false;

    private boolean mHasFocus = false;

    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private View.OnTouchListener mHapticFeedbackTouchListener;

    public static final int BUILD_LAYER = 0;

    public static final int BUILD_AND_SET_LAYER = 1;

    private final int ADVANCE_MSG = 1;

    private final int mAdvanceInterval = 20000;

    private final int mAdvanceStagger = 250;

    private long mAutoAdvanceSentTime;

    private long mAutoAdvanceTimeLeft = -1;

    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<View, AppWidgetProviderInfo>();

    private final int mRestoreScreenOrientationDelay = 500;

    private Drawable mWorkspaceBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

    private static final boolean DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE = false;

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();

    static Date sDateStamp = new Date();

    static DateFormat sDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    static long sRunStart = System.currentTimeMillis();

    static final String CORRUPTION_EMAIL_SENT_KEY = "corruptionEmailSent";

    private SharedPreferences mSharedPrefs;

    private ImageView mFolderIconImageView;

    private Bitmap mFolderIconBitmap;

    private Canvas mFolderIconCanvas;

    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;

    protected static HashMap<String, CustomAppWidget> sCustomAppWidgets = new HashMap<String, CustomAppWidget>();

    private static final boolean ENABLE_CUSTOM_WIDGET_TEST = false;

    static {
        if (ENABLE_CUSTOM_WIDGET_TEST) {
            sCustomAppWidgets.put(DummyWidget.class.getName(), new DummyWidget());
        }
    }

    private static Method sClipRevealMethod = null;

    static {
        Class<?> activityOptionsClass = ActivityOptions.class;
        try {
            sClipRevealMethod = activityOptionsClass.getDeclaredMethod("makeClipRevealAnimation", View.class, int.class, int.class, int.class, int.class);
        } catch (Exception e) {
        }
    }

    private Runnable mBuildLayersRunnable = new Runnable() {

        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };

    private static PendingAddArguments sPendingAddItem;

    private static class PendingAddArguments {

        int requestCode;

        Intent intent;

        long container;

        long screenId;

        int cellX;

        int cellY;

        int appWidgetId;
    }

    private Stats mStats;

    FocusIndicatorView mFocusHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnCreate();
        }
        super.onCreate(savedInstanceState);
        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherAppState.getLauncherProvider().setLauncherProviderChangeListener(this);
        DeviceProfile grid = app.initDynamicGrid(this);
        mSharedPrefs = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), Context.MODE_PRIVATE);
        mIsSafeModeEnabled = getPackageManager().isSafeMode();
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mIconCache.flushInvalidIcons(grid);
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();
        mStats = new Stats(this);
        mAppWidgetManager = AppWidgetManagerCompat.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        mPaused = false;
        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(Environment.getExternalStorageDirectory() + "/launcher");
        }
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        setupViews();
        grid.layout(this);
        registerContentObservers();
        lockAllApps();
        mSavedState = savedInstanceState;
        restoreState(mSavedState);
        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }
        if (!mRestoring) {
            if (DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE) {
                mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE);
            } else {
                mModel.startLoader(true, mWorkspace.getRestorePage());
            }
        }
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        unlockScreenOrientation(true);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onCreate(savedInstanceState);
            if (mLauncherCallbacks.hasLauncherOverlay()) {
                ViewStub stub = (ViewStub) findViewById(R.id.launcher_overlay_stub);
                mLauncherOverlayContainer = (InsettableFrameLayout) stub.inflate();
                mLauncherOverlay = mLauncherCallbacks.setLauncherOverlayView(mLauncherOverlayContainer, mLauncherOverlayCallbacks);
                mWorkspace.setLauncherOverlay(mLauncherOverlay);
            }
        }
        if (shouldShowIntroScreen()) {
            showIntroScreen();
        } else {
            showFirstRunActivity();
            showFirstRunClings();
        }
    }

    private LauncherCallbacks mLauncherCallbacks;

    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPostCreate(savedInstanceState);
        }
    }

    public boolean setLauncherCallbacks(LauncherCallbacks callbacks) {
        mLauncherCallbacks = callbacks;
        return true;
    }

    @Override
    public void onLauncherProviderChange() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onLauncherProviderChange();
        }
    }

    protected boolean hasCustomContentToLeft() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasCustomContentToLeft();
        }
        return false;
    }

    protected void populateCustomContentContainer() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.populateCustomContentContainer();
        }
    }

    protected void invalidateHasCustomContentToLeft() {
        if (mWorkspace == null || mWorkspace.getScreenOrder().isEmpty()) {
            return;
        }
        if (!mWorkspace.hasCustomContent() && hasCustomContentToLeft()) {
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        } else if (mWorkspace.hasCustomContent() && !hasCustomContentToLeft()) {
            mWorkspace.removeCustomContentPage();
        }
    }

    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {

                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();
                }
            }.execute();
            return;
        }
        final Configuration configuration = getResources().getConfiguration();
        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();
        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;
        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;
        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;
        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;
            mIconCache.flush();
            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new AsyncTask<Void, Void, Void>() {

                public Void doInBackground(Void... args) {
                    writeConfiguration(Launcher.this, localeConfiguration);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private static class LocaleConfiguration {

        public String locale;

        public int mcc = -1;

        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(LauncherFiles.LAUNCHER_PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(LauncherFiles.LAUNCHER_PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            context.getFileStreamPath(LauncherFiles.LAUNCHER_PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Stats getStats() {
        return mStats;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    boolean isDraggingEnabled() {
        return !mModel.isLoadingWorkspace();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        } else {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1;
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    public int getViewIdForItem(ItemInfo info) {
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }

    private long completeAdd(PendingAddArguments args) {
        long screenId = args.screenId;
        if (args.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            screenId = ensurePendingDropLayoutExists(args.screenId);
        }
        switch(args.requestCode) {
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, screenId, args.cellX, args.cellY);
                break;
            case REQUEST_CREATE_APPWIDGET:
                completeAddAppWidget(args.appWidgetId, args.container, screenId, null, null);
                break;
            case REQUEST_RECONFIGURE_APPWIDGET:
                completeRestoreAppWidget(args.appWidgetId);
                break;
        }
        resetAddInfo();
        return screenId;
    }

    private void handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        setWaitingForResult(false);
        final int pendingAddWidgetId = mPendingAddWidgetId;
        mPendingAddWidgetId = -1;
        Runnable exitSpringLoaded = new Runnable() {

            @Override
            public void run() {
                exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
            }
        };
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            final int appWidgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded, ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
            }
            return;
        } else if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == RESULT_OK && mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(false);
            }
            return;
        }
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET);
        final boolean workspaceLocked = isWorkspaceLocked();
        if (isWidgetDrop) {
            final int appWidgetId;
            int widgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (widgetId < 0) {
                appWidgetId = pendingAddWidgetId;
            } else {
                appWidgetId = widgetId;
            }
            final int result;
            if (appWidgetId < 0 || resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not " + "returned from the widget configuration activity.");
                result = RESULT_CANCELED;
                completeTwoStageWidgetDrop(result, appWidgetId);
                final Runnable onComplete = new Runnable() {

                    @Override
                    public void run() {
                        exitSpringLoadedDragModeDelayed(false, 0, null);
                    }
                };
                if (workspaceLocked) {
                    mWorkspace.postDelayed(onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
                } else {
                    mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                }
            } else {
                if (!workspaceLocked) {
                    if (mPendingAddInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        mPendingAddInfo.screenId = ensurePendingDropLayoutExists(mPendingAddInfo.screenId);
                    }
                    final CellLayout dropLayout = mWorkspace.getScreenWithId(mPendingAddInfo.screenId);
                    dropLayout.setDropPending(true);
                    final Runnable onComplete = new Runnable() {

                        @Override
                        public void run() {
                            completeTwoStageWidgetDrop(resultCode, appWidgetId);
                            dropLayout.setDropPending(false);
                        }
                    };
                    mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                } else {
                    PendingAddArguments args = preparePendingAddArgs(requestCode, data, appWidgetId, mPendingAddInfo);
                    sPendingAddItem = args;
                }
            }
            return;
        }
        if (requestCode == REQUEST_RECONFIGURE_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                PendingAddArguments args = preparePendingAddArgs(requestCode, data, pendingAddWidgetId, mPendingAddInfo);
                if (workspaceLocked) {
                    sPendingAddItem = args;
                } else {
                    completeAdd(args);
                }
            }
            return;
        }
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = preparePendingAddArgs(requestCode, data, -1, mPendingAddInfo);
            if (isWorkspaceLocked()) {
                sPendingAddItem = args;
            } else {
                completeAdd(args);
                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded, ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            }
        } else if (resultCode == RESULT_CANCELED) {
            mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded, ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
        }
        mDragLayer.clearAnimatedView();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        handleActivityResult(requestCode, resultCode, data);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onActivityResult(requestCode, resultCode, data);
        }
    }

    private PendingAddArguments preparePendingAddArgs(int requestCode, Intent data, int appWidgetId, ItemInfo info) {
        PendingAddArguments args = new PendingAddArguments();
        args.requestCode = requestCode;
        args.intent = data;
        args.container = info.container;
        args.screenId = info.screenId;
        args.cellX = info.cellX;
        args.cellY = info.cellY;
        args.appWidgetId = appWidgetId;
        return args;
    }

    private long ensurePendingDropLayoutExists(long screenId) {
        CellLayout dropLayout = (CellLayout) mWorkspace.getScreenWithId(screenId);
        if (dropLayout == null) {
            mWorkspace.addExtraEmptyScreen();
            return mWorkspace.commitExtraEmptyScreen();
        } else {
            return screenId;
        }
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout = (CellLayout) mWorkspace.getScreenWithId(mPendingAddInfo.screenId);
        Runnable onCompleteRunnable = null;
        int animationType = 0;
        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId, mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {

                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container, mPendingAddInfo.screenId, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout, (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable, animationType, boundWidget, true);
        } else if (onCompleteRunnable != null) {
            onCompleteRunnable.run();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirstFrameAnimatorHelper.setIsVisible(false);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirstFrameAnimatorHelper.setIsVisible(true);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onStart();
        }
    }

    @Override
    protected void onResume() {
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
            Log.v(TAG, "Launcher.onResume()");
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.preOnResume();
        }
        super.onResume();
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false, mAppsCustomizeContent.getContentType(), false);
        }
        mOnResumeState = State.NONE;
        setWorkspaceBackground(mState == State.WORKSPACE);
        mPaused = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            setWorkspaceLoading(true);
            mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE);
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }
        if (mBindOnResumeCallbacks.size() > 0) {
            long startTimeCallbacks = 0;
            if (DEBUG_RESUME_TIME) {
                startTimeCallbacks = System.currentTimeMillis();
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(true);
            }
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(false);
            }
            mBindOnResumeCallbacks.clear();
            if (DEBUG_RESUME_TIME) {
                Log.d(TAG, "Time spent processing callbacks in onResume: " + (System.currentTimeMillis() - startTimeCallbacks));
            }
        }
        if (mOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mOnResumeCallbacks.size(); i++) {
                mOnResumeCallbacks.get(i).run();
            }
            mOnResumeCallbacks.clear();
        }
        if (mWaitingForResume != null) {
            mWaitingForResume.setStayPressed(false);
        }
        getWorkspace().reinflateWidgetsIfNecessary();
        InstallShortcutReceiver.disableAndFlushInstallQueue(this);
        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onResume: " + (System.currentTimeMillis() - startTime));
        }
        if (mWorkspace.getCustomContentCallbacks() != null) {
            if (mWorkspace.isOnOrMovingToCustomContent()) {
                mWorkspace.getCustomContentCallbacks().onShow(true);
            }
        }
        mWorkspace.updateInteractionForState();
        mWorkspace.onResume();
        PackageInstallerCompat.getInstance(this).onResume();
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onResume();
        }
    }

    @Override
    protected void onPause() {
        InstallShortcutReceiver.enableInstallQueue();
        PackageInstallerCompat.getInstance(this).onPause();
        super.onPause();
        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();
        if (mWorkspace.getCustomContentCallbacks() != null) {
            mWorkspace.getCustomContentCallbacks().onHide();
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPause();
        }
    }

    public interface CustomContentCallbacks {

        public void onShow(boolean fromResume);

        public void onHide();

        public void onScrollProgressChanged(float progress);

        boolean isScrollingAllowed();
    }

    public interface LauncherOverlay {

        public void onScrollInteractionBegin();

        public void onScrollInteractionEnd();

        public void onScrollChange(int progress, boolean rtl);

        public void onScrollSettled();

        public void forceExitFullImmersion();
    }

    public interface LauncherOverlayCallbacks {

        public boolean canEnterFullImmersion();

        public boolean enterFullImmersion();

        public void exitFullImmersion();
    }

    class LauncherOverlayCallbacksImpl implements LauncherOverlayCallbacks {

        @Override
        public boolean canEnterFullImmersion() {
            return mState == State.WORKSPACE;
        }

        @Override
        public boolean enterFullImmersion() {
            if (mState == State.WORKSPACE) {
                mDragLayer.setBlockTouch(true);
                return true;
            }
            return false;
        }

        @Override
        public void exitFullImmersion() {
            mDragLayer.setBlockTouch(false);
        }
    }

    protected boolean hasSettings() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasSettings();
        }
        return false;
    }

    public void addToCustomContentPage(View customContent, CustomContentCallbacks callbacks, String description) {
        mWorkspace.addToCustomContentPage(customContent, callbacks, description);
    }

    public int getTopOffsetForCustomContent() {
        return mWorkspace.getPaddingTop();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
        }
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHasFocus = hasFocus;
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onWindowFocusChanged(hasFocus);
        }
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb, keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                return onSearchRequested();
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }
        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }
        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }
        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, PagedView.INVALID_RESTORE_PAGE);
        if (currentScreen != PagedView.INVALID_RESTORE_PAGE) {
            mWorkspace.setRestorePage(currentScreen);
        }
        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final long pendingAddScreen = savedState.getLong(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screenId = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mPendingAddWidgetId = savedState.getInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID);
            setWaitingForResult(true);
            mRestoring = true;
        }
        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(mAppsCustomizeContent.getCurrentPage());
            }
            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
        mItemIdToViewId = (HashMap<Integer, Integer>) savedState.getSerializable(RUNTIME_STATE_VIEW_IDS);
    }

    private void setupViews() {
        final DragController dragController = mDragController;
        mLauncherView = findViewById(R.id.launcher);
        mFocusHandler = (FocusIndicatorView) findViewById(R.id.focus_indicator);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mWorkspace.setPageSwitchListener(this);
        mPageIndicators = mDragLayer.findViewById(R.id.page_indicator);
        mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);
        mDragLayer.setup(this, dragController);
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mHotseat.setup(this);
            mHotseat.setOnLongClickListener(this);
        }
        mOverviewPanel = (ViewGroup) findViewById(R.id.overview_panel);
        View widgetButton = findViewById(R.id.widget_button);
        widgetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!mWorkspace.isSwitchingState()) {
                    onClickAddWidgetButton(arg0);
                }
            }
        });
        widgetButton.setOnTouchListener(getHapticFeedbackTouchListener());
        View wallpaperButton = findViewById(R.id.wallpaper_button);
        wallpaperButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!mWorkspace.isSwitchingState()) {
                    onClickWallpaperPicker(arg0);
                }
            }
        });
        wallpaperButton.setOnTouchListener(getHapticFeedbackTouchListener());
        View settingsButton = findViewById(R.id.settings_button);
        if (hasSettings()) {
            settingsButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!mWorkspace.isSwitchingState()) {
                        onClickSettingsButton(arg0);
                    }
                }
            });
            settingsButton.setOnTouchListener(getHapticFeedbackTouchListener());
        } else {
            settingsButton.setVisibility(View.GONE);
        }
        mOverviewPanel.setAlpha(0f);
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);
        mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer.findViewById(R.id.search_drop_target_bar);
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView) mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
            mSearchDropTargetBar.setQsbSearchBar(getQsbBar());
        }
        if (getResources().getBoolean(R.bool.debug_memory_enabled)) {
            Log.v(TAG, "adding WeightWatcher");
            mWeightWatcher = new WeightWatcher(this);
            mWeightWatcher.setAlpha(0.5f);
            ((FrameLayout) mLauncherView).addView(mWeightWatcher, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
            boolean show = shouldShowWeightWatcher();
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setAllAppsButton(View allAppsButton) {
        mAllAppsButton = allAppsButton;
    }

    public View getAllAppsButton() {
        return mAllAppsButton;
    }

    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application, (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    public View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        favorite.setOnClickListener(this);
        favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    private void completeAddShortcut(Intent data, long container, long screenId, int cellX, int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screenId);
        boolean foundCellSpan = false;
        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data);
        if (info == null) {
            return;
        }
        final View view = createShortcut(info);
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0, true, null, null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject, true)) {
                return;
            }
        } else if (touchXY != null) {
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }
        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }
        LauncherModel.addItemToDatabase(this, info, container, screenId, cellXY[0], cellXY[1], false);
        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screenId, cellXY[0], cellXY[1], 1, 1, isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth, int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(requiredWidth, requiredHeight, null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth, info.minResizeHeight);
    }

    private void completeAddAppWidget(int appWidgetId, long container, long screenId, AppWidgetHostView hostView, LauncherAppWidgetProviderInfo appWidgetInfo) {
        ItemInfo info = mPendingAddInfo;
        if (appWidgetInfo == null) {
            appWidgetInfo = LauncherAppWidgetProviderInfo.fromProviderInfo(this, mAppWidgetManager.getAppWidgetInfo(appWidgetId));
        }
        if (appWidgetInfo.isCustomWidget) {
            appWidgetId = LauncherAppWidgetInfo.CUSTOM_WIDGET_ID;
        }
        LauncherAppWidgetInfo launcherInfo;
        launcherInfo = new LauncherAppWidgetInfo(appWidgetId, appWidgetInfo.provider);
        launcherInfo.spanX = info.spanX;
        launcherInfo.spanY = info.spanY;
        launcherInfo.minSpanX = info.minSpanX;
        launcherInfo.minSpanY = info.minSpanY;
        launcherInfo.user = mAppWidgetManager.getUser(appWidgetInfo);
        LauncherModel.addItemToDatabase(this, launcherInfo, container, screenId, info.cellX, info.cellY, false);
        if (!mRestoring) {
            if (hostView == null) {
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            } else {
                launcherInfo.hostView = hostView;
            }
            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            launcherInfo.notifyWidgetSizeChanged(this);
            mWorkspace.addInScreen(launcherInfo.hostView, container, screenId, info.cellX, info.cellY, launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();
                if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    showWorkspace(false);
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.DELETE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE, LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE);
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.MIGRATE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE, LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE | LauncherModel.LOADER_FLAG_MIGRATE_SHORTCUTS);
            } else if (LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED.equals(action) || LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
                getModel().forceReload();
            }
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED);
        if (ENABLE_DEBUG_INTENTS) {
            filter.addAction(DebugIntents.DELETE_DATABASE);
            filter.addAction(DebugIntents.MIGRATE_DATABASE);
        }
        registerReceiver(mReceiver, filter);
        FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        setupTransparentSystemBarsForLmp();
        mAttached = true;
        mVisible = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTransparentSystemBarsForLmp() {
        if (Utilities.isLmpOrAbove()) {
            Window window = getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                observer.addOnDrawListener(new ViewTreeObserver.OnDrawListener() {

                    private boolean mStarted = false;

                    public void onDraw() {
                        if (mStarted)
                            return;
                        mStarted = true;
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                        final ViewTreeObserver.OnDrawListener listener = this;
                        mWorkspace.post(new Runnable() {

                            public void run() {
                                if (mWorkspace != null && mWorkspace.getViewTreeObserver() != null) {
                                    mWorkspace.getViewTreeObserver().removeOnDrawListener(listener);
                                }
                            }
                        });
                        return;
                    }
                });
            }
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval - (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0);
            }
        }
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key : mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                        postDelayed(new Runnable() {

                            public void run() {
                                ((Advanceable) v).advance();
                            }
                        }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1)
            return;
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    public ArrayList<AppInfo> getAllAppsList() {
        return mAppsCustomizeContent.getApps();
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public ViewGroup getOverviewPanel() {
        return mOverviewPanel;
    }

    public SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    protected SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();
        setWaitingForResult(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
        }
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            closeSystemDialogs();
            final boolean alreadyOnHome = mHasFocus && ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            if (mWorkspace == null) {
                return;
            }
            Folder openFolder = mWorkspace.getOpenFolder();
            mWorkspace.exitWidgetResizeMode();
            boolean moveToDefaultScreen = mLauncherCallbacks != null ? mLauncherCallbacks.shouldMoveToDefaultScreenOnHomeIntent() : true;
            if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() && openFolder == null && moveToDefaultScreen) {
                mWorkspace.moveToDefaultScreen(true);
            }
            closeFolder();
            exitSpringLoadedDragMode();
            if (alreadyOnHome) {
                showWorkspace(true);
            } else {
                mOnResumeState = State.WORKSPACE;
            }
            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                mAppsCustomizeTabHost.reset();
            }
            if (mLauncherCallbacks != null) {
                mLauncherCallbacks.onHomeIntent();
            }
        }
        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onNewIntent: " + (System.currentTimeMillis() - startTime));
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onNewIntent(intent);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        for (int page : mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mWorkspace.getChildCount() > 0) {
            outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentPageOffsetFromCustomContent());
        }
        super.onSaveInstanceState(outState);
        outState.putInt(RUNTIME_STATE, mState.ordinal());
        closeFolder();
        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screenId > -1 && mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putLong(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screenId);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID, mPendingAddWidgetId);
        }
        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }
        if (mAppsCustomizeTabHost != null) {
            AppsCustomizePagedView.ContentType type = mAppsCustomizeContent.getContentType();
            String currentTabTag = mAppsCustomizeTabHost.getTabTagForContentType(type);
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
        outState.putSerializable(RUNTIME_STATE_VIEW_IDS, mItemIdToViewId);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);
        LauncherAppState app = (LauncherAppState.getInstance());
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
            app.setLauncher(null);
        }
        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;
        mWidgetsToAdvance.clear();
        TextKeyListener.getInstance().release();
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }
        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllWorkspaceScreens();
        mWorkspace = null;
        mDragController = null;
        LauncherAnimUtils.onDestroyActivity();
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onDestroy();
        }
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            setWaitingForResult(true);
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
        showWorkspace(true);
        if (initialQuery == null) {
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString("source", "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }
        boolean clearTextImmediately = startSearch(initialQuery, selectInitialQuery, appSearchData, sourceBounds);
        if (clearTextImmediately) {
            clearTypedText();
        }
    }

    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        if (mLauncherCallbacks != null && mLauncherCallbacks.providesSearch()) {
            return mLauncherCallbacks.startSearch(initialQuery, selectInitialQuery, appSearchData, sourceBounds);
        }
        startGlobalSearch(initialQuery, selectInitialQuery, appSearchData, sourceBounds);
        return false;
    }

    private void startGlobalSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    public boolean isOnCustomContent() {
        return mWorkspace.isOnOrMovingToCustomContent();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!isOnCustomContent()) {
            closeFolder();
            mWorkspace.exitWidgetResizeMode();
            if (!mWorkspace.isInOverviewMode()) {
                showOverviewMode(true);
            } else {
                showWorkspace(true);
            }
        }
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    public boolean isWorkspaceLoading() {
        return mWorkspaceLoading;
    }

    private void setWorkspaceLoading(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWorkspaceLoading = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setWaitingForResult(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWaitingForResult = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    protected void onWorkspaceLockedChanged() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onWorkspaceLockedChanged();
        }
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screenId = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info, final AppWidgetHostView boundWidget, final LauncherAppWidgetProviderInfo appWidgetInfo) {
        addAppWidgetImpl(appWidgetId, info, boundWidget, appWidgetInfo, 0);
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info, final AppWidgetHostView boundWidget, final LauncherAppWidgetProviderInfo appWidgetInfo, int delay) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;
            mPendingAddWidgetId = appWidgetId;
            mAppWidgetManager.startConfigActivity(appWidgetInfo, appWidgetId, this, mAppWidgetHost, REQUEST_CREATE_APPWIDGET);
        } else {
            Runnable onComplete = new Runnable() {

                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed(true, EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
                }
            };
            completeAddAppWidget(appWidgetId, info.container, info.screenId, boundWidget, appWidgetInfo);
            mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete, delay, false);
        }
    }

    protected void moveToCustomContentScreen(boolean animate) {
        closeFolder();
        mWorkspace.moveToCustomContentScreen(animate);
    }

    public void addPendingItem(PendingAddItemInfo info, long container, long screenId, int[] cell, int spanX, int spanY) {
        switch(info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET:
            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                int[] span = new int[2];
                span[0] = spanX;
                span[1] = spanY;
                addAppWidgetFromDrop((PendingAddWidgetInfo) info, container, screenId, cell, span);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                processShortcutFromDrop(info.componentName, container, screenId, cell);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
    }

    private void processShortcutFromDrop(ComponentName componentName, long container, long screenId, int[] cell) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screenId = screenId;
        mPendingAddInfo.dropPos = null;
        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    private void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, long screenId, int[] cell, int[] span) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screenId = info.screenId = screenId;
        mPendingAddInfo.dropPos = null;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;
        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }
        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;
            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.info, options);
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                mAppWidgetManager.getUser(mPendingAddWidgetInfo).addToIntent(intent, AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
        Utilities.startActivityForResultSafely(this, intent, REQUEST_CREATE_SHORTCUT);
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final long screenId, int cellX, int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screenId, cellX, cellY, false);
        sFolders.put(folderInfo.id, folderInfo);
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        mWorkspace.addInScreen(newFolder, container, screenId, cellX, cellY, 1, 1, isWorkspaceLocked());
        CellLayout parent = mWorkspace.getParentCellLayoutForView(newFolder);
        parent.getShortcutsAndWidgets().measureChild(newFolder);
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }

    protected ComponentName getWallpaperPickerComponent() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getWallpaperPickerComponent();
        }
        return new ComponentName(getPackageName(), LauncherWallpaperPickerActivity.class.getName());
    }

    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch(event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (Utilities.isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch(event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (mLauncherCallbacks != null && mLauncherCallbacks.handleBackPressed()) {
            return;
        }
        if (isAllAppsVisible()) {
            if (mAppsCustomizeContent.getContentType() == AppsCustomizePagedView.ContentType.Applications) {
                showWorkspace(true);
            } else {
                showOverviewMode(true);
            }
        } else if (mWorkspace.isInOverviewMode()) {
            mWorkspace.exitOverviewMode(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else {
            mWorkspace.exitWidgetResizeMode();
            mWorkspace.showOutlinesTemporarily();
        }
    }

    private void onAppWidgetReset() {
        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    public void onClick(View v) {
        if (v.getWindowToken() == null) {
            return;
        }
        if (!mWorkspace.isFinishedSwitchingState()) {
            return;
        }
        if (v instanceof Workspace) {
            if (mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(true);
            }
            return;
        }
        if (v instanceof CellLayout) {
            if (mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(mWorkspace.indexOfChild(v), true);
            }
        }
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            onClickAppShortcut(v);
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                onClickFolderIcon(v);
            }
        } else if (v == mAllAppsButton) {
            onClickAllAppsButton(v);
        } else if (tag instanceof AppInfo) {
            startAppShortcutOrInfoActivity(v);
        } else if (tag instanceof LauncherAppWidgetInfo) {
            if (v instanceof PendingAppWidgetHostView) {
                onClickPendingWidget((PendingAppWidgetHostView) v);
            }
        }
    }

    public void onClickPagedViewIcon(View v) {
        startAppShortcutOrInfoActivity(v);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickPagedViewIcon(v);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void onClickPendingWidget(final PendingAppWidgetHostView v) {
        if (mIsSafeModeEnabled) {
            Toast.makeText(this, R.string.safemode_widget_error, Toast.LENGTH_SHORT).show();
            return;
        }
        final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
        if (v.isReadyForClickSetup()) {
            int widgetId = info.appWidgetId;
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (appWidgetInfo != null) {
                mPendingAddWidgetInfo = LauncherAppWidgetProviderInfo.fromProviderInfo(this, appWidgetInfo);
                mPendingAddInfo.copyFrom(info);
                mPendingAddWidgetId = widgetId;
                AppWidgetManagerCompat.getInstance(this).startConfigActivity(appWidgetInfo, info.appWidgetId, this, mAppWidgetHost, REQUEST_RECONFIGURE_APPWIDGET);
            }
        } else if (info.installProgress < 0) {
            final String packageName = info.providerName.getPackageName();
            showBrokenAppInstallDialog(packageName, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
                }
            });
        } else {
            final String packageName = info.providerName.getPackageName();
            startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
        }
    }

    protected void onClickAllAppsButton(View v) {
        if (LOGD)
            Log.d(TAG, "onClickAllAppsButton");
        if (isAllAppsVisible()) {
            showWorkspace(true);
        } else {
            showAllApps(true, AppsCustomizePagedView.ContentType.Applications, false);
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickAllAppsButton(v);
        }
    }

    private void showBrokenAppInstallDialog(final String packageName, DialogInterface.OnClickListener onSearchClickListener) {
        new AlertDialog.Builder(this).setTitle(R.string.abandoned_promises_title).setMessage(R.string.abandoned_promise_explanation).setPositiveButton(R.string.abandoned_search, onSearchClickListener).setNeutralButton(R.string.abandoned_clean_this, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                final UserHandleCompat user = UserHandleCompat.myUserHandle();
                mWorkspace.removeAbandonedPromise(packageName, user);
            }
        }).create().show();
        return;
    }

    protected void onClickAppShortcut(final View v) {
        if (LOGD)
            Log.d(TAG, "onClickAppShortcut");
        Object tag = v.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }
        final ShortcutInfo shortcut = (ShortcutInfo) tag;
        if (shortcut.isDisabled != 0) {
            int error = R.string.activity_not_available;
            if ((shortcut.isDisabled & ShortcutInfo.FLAG_DISABLED_SAFEMODE) != 0) {
                error = R.string.safemode_shortcut_error;
            }
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }
        final Intent intent = shortcut.intent;
        if (intent.getComponent() != null) {
            final String shortcutClass = intent.getComponent().getClassName();
            if (shortcutClass.equals(MemoryDumpActivity.class.getName())) {
                MemoryDumpActivity.startDump(this);
                return;
            } else if (shortcutClass.equals(ToggleWeightWatcher.class.getName())) {
                toggleShowWeightWatcher();
                return;
            }
        }
        if ((v instanceof BubbleTextView) && shortcut.isPromise() && !shortcut.hasStatusFlag(ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE)) {
            showBrokenAppInstallDialog(shortcut.getTargetComponent().getPackageName(), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    startAppShortcutOrInfoActivity(v);
                }
            });
            return;
        }
        startAppShortcutOrInfoActivity(v);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickAppShortcut(v);
        }
    }

    private void startAppShortcutOrInfoActivity(View v) {
        Object tag = v.getTag();
        final ShortcutInfo shortcut;
        final Intent intent;
        if (tag instanceof ShortcutInfo) {
            shortcut = (ShortcutInfo) tag;
            intent = shortcut.intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1] + v.getHeight()));
        } else if (tag instanceof AppInfo) {
            shortcut = null;
            intent = ((AppInfo) tag).intent;
        } else {
            throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
        }
        boolean success = startActivitySafely(v, intent, tag);
        mStats.recordLaunch(intent, shortcut);
        if (success && v instanceof BubbleTextView) {
            mWaitingForResume = (BubbleTextView) v;
            mWaitingForResume.setStayPressed(true);
        }
    }

    protected void onClickFolderIcon(View v) {
        if (LOGD)
            Log.d(TAG, "onClickFolder");
        if (!(v instanceof FolderIcon)) {
            throw new IllegalArgumentException("Input must be a FolderIcon");
        }
        FolderIcon folderIcon = (FolderIcon) v;
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: " + info.screenId + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }
        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            closeFolder();
            openFolder(folderIcon);
        } else {
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    closeFolder();
                    openFolder(folderIcon);
                }
            }
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickFolderIcon(v);
        }
    }

    protected void onClickAddWidgetButton(View view) {
        if (LOGD)
            Log.d(TAG, "onClickAddWidgetButton");
        if (mIsSafeModeEnabled) {
            Toast.makeText(this, R.string.safemode_widget_error, Toast.LENGTH_SHORT).show();
        } else {
            showAllApps(true, AppsCustomizePagedView.ContentType.Widgets, true);
            if (mLauncherCallbacks != null) {
                mLauncherCallbacks.onClickAddWidgetButton(view);
            }
        }
    }

    protected void onClickWallpaperPicker(View v) {
        if (LOGD)
            Log.d(TAG, "onClickWallpaperPicker");
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.setComponent(getWallpaperPickerComponent());
        startActivityForResult(pickWallpaper, REQUEST_PICK_WALLPAPER);
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickWallpaperPicker(v);
        }
    }

    protected void onClickSettingsButton(View v) {
        if (LOGD)
            Log.d(TAG, "onClickSettingsButton");
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onClickSettingsButton(v);
        }
    }

    public void onTouchDownAllAppsButton(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void performHapticFeedbackOnTouchDown(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public View.OnTouchListener getHapticFeedbackTouchListener() {
        if (mHapticFeedbackTouchListener == null) {
            mHapticFeedbackTouchListener = new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    return false;
                }
            };
        }
        return mHapticFeedbackTouchListener;
    }

    public void onDragStarted(View view) {
        if (isOnCustomContent()) {
            moveWorkspaceToDefaultScreen();
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onDragStarted(view);
        }
    }

    protected void onInteractionEnd() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onInteractionEnd();
        }
    }

    protected void onInteractionBegin() {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onInteractionBegin();
        }
    }

    void startApplicationDetailsActivity(ComponentName componentName, UserHandleCompat user) {
        try {
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            launcherApps.showAppDetailsForProfile(componentName, user);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have permission to launch settings");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch settings");
        }
    }

    boolean startApplicationUninstallActivity(ComponentName componentName, int flags, UserHandleCompat user) {
        if ((flags & AppInfo.DOWNLOADED_FLAG) == 0) {
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (user != null) {
                user.addToIntent(intent, Intent.EXTRA_USER);
            }
            startActivity(intent);
            return true;
        }
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            boolean useLaunchAnimation = (v != null) && !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            UserManagerCompat userManager = UserManagerCompat.getInstance(this);
            UserHandleCompat user = null;
            if (intent.hasExtra(AppInfo.EXTRA_PROFILE)) {
                long serialNumber = intent.getLongExtra(AppInfo.EXTRA_PROFILE, -1);
                user = userManager.getUserForSerialNumber(serialNumber);
            }
            Bundle optsBundle = null;
            if (useLaunchAnimation) {
                ActivityOptions opts = null;
                if (sClipRevealMethod != null) {
                    int left = 0, top = 0;
                    int width = v.getMeasuredWidth(), height = v.getMeasuredHeight();
                    if (v instanceof TextView) {
                        TextView tv = (TextView) v;
                        Drawable[] drawables = tv.getCompoundDrawables();
                        if (drawables != null && drawables[1] != null) {
                            Rect bounds = drawables[1].getBounds();
                            left = (width - bounds.width()) / 2;
                            top = tv.getPaddingTop();
                            width = bounds.width();
                            height = bounds.height();
                        }
                    }
                    try {
                        opts = (ActivityOptions) sClipRevealMethod.invoke(null, v, left, top, width, height);
                    } catch (IllegalAccessException e) {
                        Log.d(TAG, "Could not call makeClipRevealAnimation: " + e);
                        sClipRevealMethod = null;
                    } catch (InvocationTargetException e) {
                        Log.d(TAG, "Could not call makeClipRevealAnimation: " + e);
                        sClipRevealMethod = null;
                    }
                }
                if (opts == null) {
                    opts = Utilities.isLmpOrAbove() ? ActivityOptions.makeCustomAnimation(this, R.anim.task_open_enter, R.anim.no_anim) : ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
                }
                optsBundle = opts.toBundle();
            }
            if (user == null || user.equals(UserHandleCompat.myUserHandle())) {
                startActivity(intent, optsBundle);
            } else {
                launcherApps.startActivityForProfile(intent.getComponent(), user, intent.getSourceBounds(), optsBundle);
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity. " + "tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        if (mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width || mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }
        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);
        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null)
            return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);
        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha, scaleX, scaleY);
        if (Utilities.isLmpOrAbove()) {
            oa.setInterpolator(new LogDecelerateInterpolator(100, 0));
        }
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null)
            return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        final CellLayout cl = (CellLayout) fi.getParent().getParent();
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha, scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;
        info.opened = true;
        if (folder.getParent() == null) {
            mDragLayer.addView(folder);
            mDragController.addDropTarget((DropTarget) folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" + folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
        folder.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    public void closeFolder() {
        Folder folder = mWorkspace != null ? mWorkspace.getOpenFolder() : null;
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    public boolean onLongClick(View v) {
        if (!isDraggingEnabled())
            return false;
        if (isWorkspaceLocked())
            return false;
        if (mState != State.WORKSPACE)
            return false;
        if (v instanceof Workspace) {
            if (!mWorkspace.isInOverviewMode()) {
                if (mWorkspace.enterOverviewMode()) {
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        CellLayout.CellInfo longClickCellInfo = null;
        View itemUnderLongClick = null;
        if (v.getTag() instanceof ItemInfo) {
            ItemInfo info = (ItemInfo) v.getTag();
            longClickCellInfo = new CellLayout.CellInfo(v, info);
            ;
            itemUnderLongClick = longClickCellInfo.cell;
            resetAddInfo();
        }
        final boolean inHotseat = isHotseatLayout(v);
        boolean allowLongPress = inHotseat || mWorkspace.allowLongPress();
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                if (mWorkspace.isInOverviewMode()) {
                    mWorkspace.startReordering(v);
                } else {
                    mWorkspace.enterOverviewMode();
                }
            } else {
                final boolean isAllAppsButton = inHotseat && isAllAppsButtonRank(mHotseat.getOrderInHotseat(longClickCellInfo.cellX, longClickCellInfo.cellY));
                if (!(itemUnderLongClick instanceof Folder || isAllAppsButton)) {
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null && (layout instanceof CellLayout) && (layout == mHotseat.getLayout());
    }

    CellLayout getCellLayout(long container, long screenId) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getScreenWithId(screenId);
        }
    }

    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    private void setWorkspaceBackground(boolean workspace) {
        mLauncherView.setBackground(workspace ? mWorkspaceBackgroundDrawable : null);
    }

    protected void changeWallpaperVisiblity(boolean visible) {
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();
        showAppsCustomizeHelper(animated, springLoaded, contentType);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded, final AppsCustomizePagedView.ContentType contentType) {
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        boolean material = Utilities.isLmpOrAbove();
        final Resources res = getResources();
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeRevealTime);
        final int itemsAlphaStagger = res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
        final HashMap<View, Integer> layerViews = new HashMap<View, Integer>();
        Workspace.State workspaceState = contentType == AppsCustomizePagedView.ContentType.Widgets ? Workspace.State.OVERVIEW_HIDDEN : Workspace.State.NORMAL_HIDDEN;
        Animator workspaceAnim = mWorkspace.getChangeStateAnimation(workspaceState, animated, layerViews);
        if (!LauncherAppState.isDisableAllApps() || contentType == AppsCustomizePagedView.ContentType.Widgets) {
            mAppsCustomizeTabHost.setContentTypeImmediate(contentType);
        }
        boolean initialized = getAllAppsButton() != null;
        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            final AppsCustomizePagedView content = (AppsCustomizePagedView) toView.findViewById(R.id.apps_customize_pane_content);
            final View page = content.getPageAt(content.getCurrentPage());
            final View revealView = toView.findViewById(R.id.fake_page);
            final boolean isWidgetTray = contentType == AppsCustomizePagedView.ContentType.Widgets;
            if (isWidgetTray) {
                revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
            } else {
                revealView.setBackground(res.getDrawable(R.drawable.quantum_panel));
            }
            content.setPageBackgroundsVisible(false);
            revealView.setVisibility(View.VISIBLE);
            revealView.setAlpha(0);
            int width = revealView.getMeasuredWidth();
            int height = revealView.getMeasuredHeight();
            float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);
            revealView.setTranslationY(0);
            revealView.setTranslationX(0);
            int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView, getAllAppsButton(), null);
            float alpha = 0;
            float xDrift = 0;
            float yDrift = 0;
            if (material) {
                alpha = isWidgetTray ? 0.3f : 1f;
                yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
            } else {
                yDrift = 2 * height / 3;
                xDrift = 0;
            }
            final float initAlpha = alpha;
            layerViews.put(revealView, BUILD_AND_SET_LAYER);
            PropertyValuesHolder panelAlpha = PropertyValuesHolder.ofFloat("alpha", initAlpha, 1f);
            PropertyValuesHolder panelDriftY = PropertyValuesHolder.ofFloat("translationY", yDrift, 0);
            PropertyValuesHolder panelDriftX = PropertyValuesHolder.ofFloat("translationX", xDrift, 0);
            ObjectAnimator panelAlphaAndDrift = ObjectAnimator.ofPropertyValuesHolder(revealView, panelAlpha, panelDriftY, panelDriftX);
            panelAlphaAndDrift.setDuration(revealDuration);
            panelAlphaAndDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));
            mStateAnimation.play(panelAlphaAndDrift);
            if (page != null) {
                page.setVisibility(View.VISIBLE);
                layerViews.put(page, BUILD_AND_SET_LAYER);
                ObjectAnimator pageDrift = ObjectAnimator.ofFloat(page, "translationY", yDrift, 0);
                page.setTranslationY(yDrift);
                pageDrift.setDuration(revealDuration);
                pageDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));
                pageDrift.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(pageDrift);
                page.setAlpha(0f);
                ObjectAnimator itemsAlpha = ObjectAnimator.ofFloat(page, "alpha", 0f, 1f);
                itemsAlpha.setDuration(revealDuration);
                itemsAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
                itemsAlpha.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(itemsAlpha);
            }
            View pageIndicators = toView.findViewById(R.id.apps_customize_page_indicator);
            pageIndicators.setAlpha(0.01f);
            ObjectAnimator indicatorsAlpha = ObjectAnimator.ofFloat(pageIndicators, "alpha", 1f);
            indicatorsAlpha.setDuration(revealDuration);
            mStateAnimation.play(indicatorsAlpha);
            if (material) {
                final View allApps = getAllAppsButton();
                int allAppsButtonSize = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                float startRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                Animator reveal = ViewAnimationUtils.createCircularReveal(revealView, width / 2, height / 2, startRadius, revealRadius);
                reveal.setDuration(revealDuration);
                reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
                reveal.addListener(new AnimatorListenerAdapter() {

                    public void onAnimationStart(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.INVISIBLE);
                        }
                    }

                    public void onAnimationEnd(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.VISIBLE);
                        }
                    }
                });
                mStateAnimation.play(reveal);
            }
            mStateAnimation.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);
                    revealView.setVisibility(View.INVISIBLE);
                    for (View v : layerViews.keySet()) {
                        if (layerViews.get(v) == BUILD_AND_SET_LAYER) {
                            v.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }
                    content.setPageBackgroundsVisible(true);
                    if (mSearchDropTargetBar != null) {
                        mSearchDropTargetBar.hideSearchBar(false);
                    }
                    mStateAnimation = null;
                }
            });
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {

                public void run() {
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);
                    revealView.setAlpha(initAlpha);
                    for (View v : layerViews.keySet()) {
                        if (layerViews.get(v) == BUILD_AND_SET_LAYER) {
                            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        }
                    }
                    if (Utilities.isLmpOrAbove()) {
                        for (View v : layerViews.keySet()) {
                            if (Utilities.isViewAttachedToWindow(v))
                                v.buildLayer();
                        }
                    }
                    mStateAnimation.start();
                }
            };
            toView.bringToFront();
            toView.setVisibility(View.VISIBLE);
            toView.post(startAnimRunnable);
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();
            if (!springLoaded && !LauncherAppState.getInstance().isScreenLarge()) {
                if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
        }
    }

    private void hideAppsCustomizeHelper(Workspace.State toState, final boolean animated, final boolean springLoaded, final Runnable onCompleteRunnable) {
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        boolean material = Utilities.isLmpOrAbove();
        Resources res = getResources();
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeConcealTime);
        final int itemsAlphaStagger = res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;
        final HashMap<View, Integer> layerViews = new HashMap<View, Integer>();
        if (toState == Workspace.State.NORMAL) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(toState, animated, layerViews);
        } else if (toState == Workspace.State.SPRING_LOADED || toState == Workspace.State.OVERVIEW) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(toState, animated, layerViews);
        }
        boolean initialized = getAllAppsButton() != null;
        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }
            final AppsCustomizePagedView content = (AppsCustomizePagedView) fromView.findViewById(R.id.apps_customize_pane_content);
            final View page = content.getPageAt(content.getNextPage());
            int count = content.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = content.getChildAt(i);
                if (child != page) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
            final View revealView = fromView.findViewById(R.id.fake_page);
            if (fromView.getVisibility() == View.VISIBLE) {
                AppsCustomizePagedView.ContentType contentType = content.getContentType();
                final boolean isWidgetTray = contentType == AppsCustomizePagedView.ContentType.Widgets;
                if (isWidgetTray) {
                    revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
                } else {
                    revealView.setBackground(res.getDrawable(R.drawable.quantum_panel));
                }
                int width = revealView.getMeasuredWidth();
                int height = revealView.getMeasuredHeight();
                float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);
                revealView.setVisibility(View.VISIBLE);
                content.setPageBackgroundsVisible(false);
                final View allAppsButton = getAllAppsButton();
                revealView.setTranslationY(0);
                int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView, allAppsButton, null);
                float xDrift = 0;
                float yDrift = 0;
                if (material) {
                    yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                    xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
                } else {
                    yDrift = 2 * height / 3;
                    xDrift = 0;
                }
                layerViews.put(revealView, BUILD_AND_SET_LAYER);
                TimeInterpolator decelerateInterpolator = material ? new LogDecelerateInterpolator(100, 0) : new DecelerateInterpolator(1f);
                ObjectAnimator panelDriftY = LauncherAnimUtils.ofFloat(revealView, "translationY", 0, yDrift);
                panelDriftY.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftY.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftY.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftY);
                ObjectAnimator panelDriftX = LauncherAnimUtils.ofFloat(revealView, "translationX", 0, xDrift);
                panelDriftX.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftX.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftX.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftX);
                if (isWidgetTray || !material) {
                    float finalAlpha = material ? 0.4f : 0f;
                    revealView.setAlpha(1f);
                    ObjectAnimator panelAlpha = LauncherAnimUtils.ofFloat(revealView, "alpha", 1f, finalAlpha);
                    panelAlpha.setDuration(material ? revealDuration : 150);
                    panelAlpha.setInterpolator(decelerateInterpolator);
                    panelAlpha.setStartDelay(material ? 0 : itemsAlphaStagger + SINGLE_FRAME_DELAY);
                    mStateAnimation.play(panelAlpha);
                }
                if (page != null) {
                    layerViews.put(page, BUILD_AND_SET_LAYER);
                    ObjectAnimator pageDrift = LauncherAnimUtils.ofFloat(page, "translationY", 0, yDrift);
                    page.setTranslationY(0);
                    pageDrift.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                    pageDrift.setInterpolator(decelerateInterpolator);
                    pageDrift.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                    mStateAnimation.play(pageDrift);
                    page.setAlpha(1f);
                    ObjectAnimator itemsAlpha = LauncherAnimUtils.ofFloat(page, "alpha", 1f, 0f);
                    itemsAlpha.setDuration(100);
                    itemsAlpha.setInterpolator(decelerateInterpolator);
                    mStateAnimation.play(itemsAlpha);
                }
                View pageIndicators = fromView.findViewById(R.id.apps_customize_page_indicator);
                pageIndicators.setAlpha(1f);
                ObjectAnimator indicatorsAlpha = LauncherAnimUtils.ofFloat(pageIndicators, "alpha", 0f);
                indicatorsAlpha.setDuration(revealDuration);
                indicatorsAlpha.setInterpolator(new DecelerateInterpolator(1.5f));
                mStateAnimation.play(indicatorsAlpha);
                width = revealView.getMeasuredWidth();
                if (material) {
                    if (!isWidgetTray) {
                        allAppsButton.setVisibility(View.INVISIBLE);
                    }
                    int allAppsButtonSize = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                    float finalRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                    Animator reveal = LauncherAnimUtils.createCircularReveal(revealView, width / 2, height / 2, revealRadius, finalRadius);
                    reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
                    reveal.setDuration(revealDuration);
                    reveal.setStartDelay(itemsAlphaStagger);
                    reveal.addListener(new AnimatorListenerAdapter() {

                        public void onAnimationEnd(Animator animation) {
                            revealView.setVisibility(View.INVISIBLE);
                            if (!isWidgetTray) {
                                allAppsButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    mStateAnimation.play(reveal);
                }
                dispatchOnLauncherTransitionPrepare(fromView, animated, true);
                dispatchOnLauncherTransitionPrepare(toView, animated, true);
                mAppsCustomizeContent.stopScrolling();
            }
            mStateAnimation.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                    for (View v : layerViews.keySet()) {
                        if (layerViews.get(v) == BUILD_AND_SET_LAYER) {
                            v.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }
                    content.setPageBackgroundsVisible(true);
                    int count = content.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = content.getChildAt(i);
                        child.setVisibility(View.VISIBLE);
                    }
                    if (page != null) {
                        page.setTranslationX(0);
                        page.setTranslationY(0);
                        page.setAlpha(1);
                    }
                    content.setCurrentPage(content.getNextPage());
                    mAppsCustomizeContent.updateCurrentPageScroll();
                    mStateAnimation = null;
                }
            });
            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {

                public void run() {
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);
                    for (View v : layerViews.keySet()) {
                        if (layerViews.get(v) == BUILD_AND_SET_LAYER) {
                            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        }
                    }
                    if (Utilities.isLmpOrAbove()) {
                        for (View v : layerViews.keySet()) {
                            if (Utilities.isViewAttachedToWindow(v))
                                v.buildLayer();
                        }
                    }
                    mStateAnimation.start();
                }
            };
            fromView.post(startAnimRunnable);
        } else {
            fromView.setVisibility(View.GONE);
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            SQLiteDatabase.releaseMemory();
            if (mAppsCustomizeTabHost != null) {
                mAppsCustomizeTabHost.trimMemory();
            }
        }
    }

    protected void showWorkspace(boolean animated) {
        showWorkspace(animated, null);
    }

    protected void showWorkspace() {
        showWorkspace(true);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (mState != State.WORKSPACE || mWorkspace.getState() != Workspace.State.NORMAL) {
            boolean wasInSpringLoadedMode = (mState != State.WORKSPACE);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(Workspace.State.NORMAL, animated, false, onCompleteRunnable);
            if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(animated && wasInSpringLoadedMode);
            }
            if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }
        }
        mState = State.WORKSPACE;
        mUserPresent = true;
        updateRunning();
        getWindow().getDecorView().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        onWorkspaceShown(animated);
    }

    void showOverviewMode(boolean animated) {
        mWorkspace.setVisibility(View.VISIBLE);
        hideAppsCustomizeHelper(Workspace.State.OVERVIEW, animated, false, null);
        mState = State.WORKSPACE;
        onWorkspaceShown(animated);
    }

    public void onWorkspaceShown(boolean animated) {
    }

    void showAllApps(boolean animated, AppsCustomizePagedView.ContentType contentType, boolean resetPageToZero) {
        if (mState != State.WORKSPACE)
            return;
        if (resetPageToZero) {
            mAppsCustomizeTabHost.reset();
        }
        showAppsCustomizeHelper(animated, false, contentType);
        mAppsCustomizeTabHost.post(new Runnable() {

            @Override
            public void run() {
                mAppsCustomizeTabHost.requestFocus();
            }
        });
        mState = State.APPS_CUSTOMIZE;
        mUserPresent = false;
        updateRunning();
        closeFolder();
        getWindow().getDecorView().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(Workspace.State.SPRING_LOADED, true, true, null);
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, int delay, final Runnable onCompleteRunnable) {
        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED)
            return;
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (successfulDrop) {
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, delay);
    }

    void exitSpringLoadedDragMode() {
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
    }

    void lockAllApps() {
    }

    void unlockAllApps() {
    }

    protected void disableVoiceButtonProxy(boolean disable) {
    }

    public View getQsbBar() {
        if (mLauncherCallbacks != null && mLauncherCallbacks.providesSearch()) {
            return mLauncherCallbacks.getQsbBar();
        }
        if (mQsb == null) {
            AppWidgetProviderInfo searchProvider = Utilities.getSearchWidgetProvider(this);
            if (searchProvider == null) {
                return null;
            }
            Bundle opts = new Bundle();
            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX);
            SharedPreferences sp = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), MODE_PRIVATE);
            int widgetId = sp.getInt(QSB_WIDGET_ID, -1);
            AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (!searchProvider.provider.flattenToString().equals(sp.getString(QSB_WIDGET_PROVIDER, null)) || (widgetInfo == null) || !widgetInfo.provider.equals(searchProvider.provider)) {
                if (widgetId > -1) {
                    mAppWidgetHost.deleteAppWidgetId(widgetId);
                    widgetId = -1;
                }
                widgetId = mAppWidgetHost.allocateAppWidgetId();
                if (!AppWidgetManagerCompat.getInstance(this).bindAppWidgetIdIfAllowed(widgetId, searchProvider, opts)) {
                    mAppWidgetHost.deleteAppWidgetId(widgetId);
                    widgetId = -1;
                }
                sp.edit().putInt(QSB_WIDGET_ID, widgetId).putString(QSB_WIDGET_PROVIDER, searchProvider.provider.flattenToString()).commit();
            }
            if (widgetId != -1) {
                mQsb = mAppWidgetHost.createView(this, widgetId, searchProvider);
                mQsb.updateAppWidgetOptions(opts);
                mQsb.setPadding(0, 0, 0, 0);
                mSearchDropTargetBar.addView(mQsb);
            }
        }
        return mQsb;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(mAppsCustomizeTabHost.getContentTag());
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    private class AppWidgetResetObserver extends ContentObserver {

        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    public void addOnResumeCallback(Runnable run) {
        mOnResumeCallbacks.add(run);
    }

    public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }

    public void startBinding() {
        setWorkspaceLoading(true);
        mBindOnResumeCallbacks.clear();
        mWorkspace.clearDropTargets();
        mWorkspace.removeAllWorkspaceScreens();
        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            mHotseat.resetLayout();
        }
    }

    @Override
    public void bindScreens(ArrayList<Long> orderedScreenIds) {
        bindAddScreens(orderedScreenIds);
        if (orderedScreenIds.size() == 0) {
            mWorkspace.addExtraEmptyScreen();
        }
        if (hasCustomContentToLeft()) {
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        }
    }

    @Override
    public void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        Launcher.addDumpLog(TAG, "11683562 - bindAddScreens()", true);
        Launcher.addDumpLog(TAG, "11683562 -   orderedScreenIds: " + TextUtils.join(", ", orderedScreenIds), true);
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(orderedScreenIds.get(i));
        }
    }

    @Override
    public void bindAddPendingItem(final PendingAddItemInfo info, final long container, final long screenId, final int[] cell, final int spanX, final int spanY) {
        showWorkspace(true, new Runnable() {

            @Override
            public void run() {
                mWorkspace.snapToPage(mWorkspace.getPageIndexForScreenId(screenId));
                addPendingItem(info, container, screenId, cell, spanX, spanY);
            }
        });
    }

    private boolean shouldShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, SHOW_WEIGHT_WATCHER_DEFAULT);
        return show;
    }

    private void toggleShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, true);
        show = !show;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SHOW_WEIGHT_WATCHER, show);
        editor.commit();
        if (mWeightWatcher != null) {
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void bindAppsAdded(final ArrayList<Long> newScreens, final ArrayList<ItemInfo> addNotAnimated, final ArrayList<ItemInfo> addAnimated, final ArrayList<AppInfo> addedApps) {
        Runnable r = new Runnable() {

            public void run() {
                bindAppsAdded(newScreens, addNotAnimated, addAnimated, addedApps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (newScreens != null) {
            bindAddScreens(newScreens);
        }
        if (addNotAnimated != null && !addNotAnimated.isEmpty()) {
            bindItems(addNotAnimated, 0, addNotAnimated.size(), false);
        }
        if (addAnimated != null && !addAnimated.isEmpty()) {
            bindItems(addAnimated, 0, addAnimated.size(), true);
        }
        mWorkspace.removeExtraEmptyScreen(false, false);
        if (!LauncherAppState.isDisableAllApps() && addedApps != null && mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(addedApps);
        }
    }

    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end, final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {

            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        final Collection<Animator> bounceAnims = new ArrayList<Animator>();
        final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation();
        Workspace workspace = mWorkspace;
        long newShortcutsScreenId = -1;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT && mHotseat == null) {
                continue;
            }
            switch(item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);
                    if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        CellLayout cl = mWorkspace.getScreenWithId(item.screenId);
                        if (cl != null && cl.isOccupied(item.cellX, item.cellY)) {
                            View v = cl.getChildAt(item.cellX, item.cellY);
                            Object tag = v.getTag();
                            String desc = "Collision while binding workspace item: " + item + ". Collides with " + tag;
                            if (LauncherAppState.isDogfoodBuild()) {
                                throw (new RuntimeException(desc));
                            } else {
                                Log.d(TAG, desc);
                            }
                        }
                    }
                    workspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX, item.cellY, 1, 1);
                    if (animateIcons) {
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0f);
                        shortcut.setScaleY(0f);
                        bounceAnims.add(createNewAppBounceAnimation(shortcut, i));
                        newShortcutsScreenId = item.screenId;
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this, (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()), (FolderInfo) item, mIconCache);
                    workspace.addInScreenFromBind(newFolder, item.container, item.screenId, item.cellX, item.cellY, 1, 1);
                    break;
                default:
                    throw new RuntimeException("Invalid Item Type");
            }
        }
        if (animateIcons) {
            if (newShortcutsScreenId > -1) {
                long currentScreenId = mWorkspace.getScreenIdForPageIndex(mWorkspace.getNextPage());
                final int newScreenIndex = mWorkspace.getPageIndexForScreenId(newShortcutsScreenId);
                final Runnable startBounceAnimRunnable = new Runnable() {

                    public void run() {
                        anim.playTogether(bounceAnims);
                        anim.start();
                    }
                };
                if (newShortcutsScreenId != currentScreenId) {
                    mWorkspace.postDelayed(new Runnable() {

                        public void run() {
                            if (mWorkspace != null) {
                                mWorkspace.snapToPage(newScreenIndex);
                                mWorkspace.postDelayed(startBounceAnimRunnable, NEW_APPS_ANIMATION_DELAY);
                            }
                        }
                    }, NEW_APPS_PAGE_MOVE_DELAY);
                } else {
                    mWorkspace.postDelayed(startBounceAnimRunnable, NEW_APPS_ANIMATION_DELAY);
                }
            }
        }
        workspace.requestLayout();
    }

    public void bindFolders(final HashMap<Long, FolderInfo> folders) {
        Runnable r = new Runnable() {

            public void run() {
                bindFolders(folders);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        sFolders.clear();
        sFolders.putAll(folders);
    }

    public void bindAppWidget(final LauncherAppWidgetInfo item) {
        Runnable r = new Runnable() {

            public void run() {
                bindAppWidget(item);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;
        LauncherAppWidgetProviderInfo appWidgetInfo = LauncherModel.getProviderInfo(this, item.providerName);
        if (!mIsSafeModeEnabled && ((item.restoreStatus & LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY) == 0) && ((item.restoreStatus & LauncherAppWidgetInfo.FLAG_ID_NOT_VALID) != 0)) {
            if (appWidgetInfo == null) {
                if (DEBUG_WIDGETS) {
                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId + " belongs to component " + item.providerName + ", as the povider is null");
                }
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }
            PendingAddWidgetInfo pendingInfo = new PendingAddWidgetInfo(appWidgetInfo, null);
            pendingInfo.spanX = item.spanX;
            pendingInfo.spanY = item.spanY;
            pendingInfo.minSpanX = item.minSpanX;
            pendingInfo.minSpanY = item.minSpanY;
            Bundle options = AppsCustomizePagedView.getDefaultOptionsForWidget(this, pendingInfo);
            int newWidgetId = mAppWidgetHost.allocateAppWidgetId();
            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(newWidgetId, appWidgetInfo, options);
            if (!success) {
                mAppWidgetHost.deleteAppWidgetId(newWidgetId);
                if (DEBUG_WIDGETS) {
                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId + " belongs to component " + item.providerName + ", as the launcher is unable to bing a new widget id");
                }
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }
            item.appWidgetId = newWidgetId;
            item.restoreStatus = (appWidgetInfo.configure == null) ? LauncherAppWidgetInfo.RESTORE_COMPLETED : LauncherAppWidgetInfo.FLAG_UI_NOT_READY;
            LauncherModel.updateItemInDatabase(this, item);
        }
        if (!mIsSafeModeEnabled && item.restoreStatus == LauncherAppWidgetInfo.RESTORE_COMPLETED) {
            final int appWidgetId = item.appWidgetId;
            if (DEBUG_WIDGETS) {
                Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
            }
            item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        } else {
            appWidgetInfo = null;
            PendingAppWidgetHostView view = new PendingAppWidgetHostView(this, item, mIsSafeModeEnabled);
            view.updateIcon(mIconCache);
            item.hostView = view;
            item.hostView.updateAppWidget(null);
            item.hostView.setOnClickListener(this);
        }
        item.hostView.setTag(item);
        item.onBindAppWidget(this);
        workspace.addInScreen(item.hostView, item.container, item.screenId, item.cellX, item.cellY, item.spanX, item.spanY, false);
        if (!item.isCustomWidget()) {
            addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);
        }
        workspace.requestLayout();
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id=" + item.appWidgetId + " in " + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    private void completeRestoreAppWidget(final int appWidgetId) {
        LauncherAppWidgetHostView view = mWorkspace.getWidgetForAppWidgetId(appWidgetId);
        if ((view == null) || !(view instanceof PendingAppWidgetHostView)) {
            Log.e(TAG, "Widget update called, when the widget no longer exists.");
            return;
        }
        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) view.getTag();
        info.restoreStatus = LauncherAppWidgetInfo.RESTORE_COMPLETED;
        mWorkspace.reinflateWidgetsIfNecessary();
        LauncherModel.updateItemInDatabase(this, info);
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    public void finishBindingItems() {
        Runnable r = new Runnable() {

            public void run() {
                finishBindingItems();
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }
        mWorkspace.restoreInstanceStateForRemainingPages();
        setWorkspaceLoading(false);
        sendLoadingCompleteBroadcastIfNecessary();
        if (sPendingAddItem != null) {
            final long screenId = completeAdd(sPendingAddItem);
            mWorkspace.post(new Runnable() {

                @Override
                public void run() {
                    mWorkspace.snapToScreenId(screenId);
                }
            });
            sPendingAddItem = null;
        }
        PackageInstallerCompat.getInstance(this).onFinishBind();
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.finishBindingItems(false);
        }
    }

    private void sendLoadingCompleteBroadcastIfNecessary() {
        if (!mSharedPrefs.getBoolean(FIRST_LOAD_COMPLETE, false)) {
            String permission = getResources().getString(R.string.receive_first_load_broadcast_permission);
            Intent intent = new Intent(ACTION_FIRST_LOAD_COMPLETE);
            sendBroadcast(intent, permission);
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putBoolean(FIRST_LOAD_COMPLETE, true);
            editor.apply();
        }
    }

    public boolean isAllAppsButtonRank(int rank) {
        if (mHotseat != null) {
            return mHotseat.isAllAppsButtonRank(rank);
        }
        return false;
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    private ValueAnimator createNewAppBounceAnimation(View v, int i) {
        ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v, PropertyValuesHolder.ofFloat("alpha", 1f), PropertyValuesHolder.ofFloat("scaleX", 1f), PropertyValuesHolder.ofFloat("scaleY", 1f));
        bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
        bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
        bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
        return bounceAnim;
    }

    public boolean useVerticalBarLayout() {
        return LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().isVerticalBarLayout();
    }

    protected Rect getSearchBarBounds() {
        return LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getSearchBarBounds();
    }

    public void bindSearchablesChanged() {
        if (mSearchDropTargetBar == null) {
            return;
        }
        if (mQsb != null) {
            mSearchDropTargetBar.removeView(mQsb);
            mQsb = null;
        }
        mSearchDropTargetBar.setQsbSearchBar(getQsbBar());
    }

    public void bindAllApplications(final ArrayList<AppInfo> apps) {
        if (LauncherAppState.isDisableAllApps()) {
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(this));
            }
        } else {
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setApps(apps);
                mAppsCustomizeContent.onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(this));
            }
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.bindAllApplications(apps);
        }
    }

    public void bindAppsUpdated(final ArrayList<AppInfo> apps) {
        Runnable r = new Runnable() {

            public void run() {
                bindAppsUpdated(apps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (!LauncherAppState.isDisableAllApps() && mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
    }

    @Override
    public void bindWidgetsRestored(final ArrayList<LauncherAppWidgetInfo> widgets) {
        Runnable r = new Runnable() {

            public void run() {
                bindWidgetsRestored(widgets);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        mWorkspace.widgetsRestored(widgets);
    }

    @Override
    public void bindShortcutsChanged(final ArrayList<ShortcutInfo> updated, final ArrayList<ShortcutInfo> removed, final UserHandleCompat user) {
        Runnable r = new Runnable() {

            public void run() {
                bindShortcutsChanged(updated, removed, user);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (!updated.isEmpty()) {
            mWorkspace.updateShortcuts(updated);
        }
        if (!removed.isEmpty()) {
            HashSet<ComponentName> removedComponents = new HashSet<ComponentName>();
            for (ShortcutInfo si : removed) {
                removedComponents.add(si.getTargetComponent());
            }
            mWorkspace.removeItemsByComponentName(removedComponents, user);
            mDragController.onAppsRemoved(new ArrayList<String>(), removedComponents);
        }
    }

    @Override
    public void updatePackageState(ArrayList<PackageInstallInfo> installInfo) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageState(installInfo);
        }
    }

    @Override
    public void updatePackageBadge(String packageName) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageBadge(packageName, UserHandleCompat.myUserHandle());
        }
    }

    @Override
    public void bindComponentsRemoved(final ArrayList<String> packageNames, final ArrayList<AppInfo> appInfos, final UserHandleCompat user, final int reason) {
        Runnable r = new Runnable() {

            public void run() {
                bindComponentsRemoved(packageNames, appInfos, user, reason);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (reason == 0) {
            HashSet<ComponentName> removedComponents = new HashSet<ComponentName>();
            for (AppInfo info : appInfos) {
                removedComponents.add(info.componentName);
            }
            if (!packageNames.isEmpty()) {
                mWorkspace.removeItemsByPackageName(packageNames, user);
            }
            if (!removedComponents.isEmpty()) {
                mWorkspace.removeItemsByComponentName(removedComponents, user);
            }
            mDragController.onAppsRemoved(packageNames, removedComponents);
        } else {
            mWorkspace.disableShortcutsByPackageName(packageNames, user, reason);
        }
        if (!LauncherAppState.isDisableAllApps() && mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(appInfos);
        }
    }

    private ArrayList<Object> mWidgetsAndShortcuts;

    private Runnable mBindPackagesUpdatedRunnable = new Runnable() {

        public void run() {
            bindPackagesUpdated(mWidgetsAndShortcuts);
            mWidgetsAndShortcuts = null;
        }
    };

    public void bindPackagesUpdated(final ArrayList<Object> widgetsAndShortcuts) {
        if (waitUntilResume(mBindPackagesUpdatedRunnable, true)) {
            mWidgetsAndShortcuts = widgetsAndShortcuts;
            return;
        }
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated(widgetsAndShortcuts);
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch(d.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                naturalOri = configOri;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
                break;
        }
        int[] oriMap = { ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE };
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public void lockScreenOrientation() {
        if (Utilities.isRotationEnabled(this)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources().getConfiguration().orientation));
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }
        }
    }

    public void unlockScreenOrientation(boolean immediate) {
        if (Utilities.isRotationEnabled(this)) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }
    }

    protected boolean isLauncherPreinstalled() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.isLauncherPreinstalled();
        }
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getComponentName().getPackageName(), 0);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            } else {
                return false;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean overrideWallpaperDimensions() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.overrideWallpaperDimensions();
        }
        return true;
    }

    protected boolean hasFirstRunActivity() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasFirstRunActivity();
        }
        return false;
    }

    protected Intent getFirstRunActivity() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getFirstRunActivity();
        }
        return null;
    }

    private boolean shouldRunFirstRunActivity() {
        return !ActivityManager.isRunningInTestHarness() && !mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    protected boolean hasRunFirstRunActivity() {
        return mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    public boolean showFirstRunActivity() {
        if (shouldRunFirstRunActivity() && hasFirstRunActivity()) {
            Intent firstRunIntent = getFirstRunActivity();
            if (firstRunIntent != null) {
                startActivity(firstRunIntent);
                markFirstRunActivityShown();
                return true;
            }
        }
        return false;
    }

    private void markFirstRunActivityShown() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, true);
        editor.apply();
    }

    protected boolean hasDismissableIntroScreen() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.hasDismissableIntroScreen();
        }
        return false;
    }

    protected View getIntroScreen() {
        if (mLauncherCallbacks != null) {
            return mLauncherCallbacks.getIntroScreen();
        }
        return null;
    }

    private boolean shouldShowIntroScreen() {
        return hasDismissableIntroScreen() && !mSharedPrefs.getBoolean(INTRO_SCREEN_DISMISSED, false);
    }

    protected void showIntroScreen() {
        View introScreen = getIntroScreen();
        changeWallpaperVisiblity(false);
        if (introScreen != null) {
            mDragLayer.showOverlayView(introScreen);
        }
        if (mLauncherOverlayContainer != null) {
            mLauncherOverlayContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void dismissIntroScreen() {
        markIntroScreenDismissed();
        if (showFirstRunActivity()) {
            mWorkspace.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mDragLayer.dismissOverlayView();
                    if (mLauncherOverlayContainer != null) {
                        mLauncherOverlayContainer.setVisibility(View.VISIBLE);
                    }
                    showFirstRunClings();
                }
            }, ACTIVITY_START_DELAY);
        } else {
            mDragLayer.dismissOverlayView();
            if (mLauncherOverlayContainer != null) {
                mLauncherOverlayContainer.setVisibility(View.VISIBLE);
            }
            showFirstRunClings();
        }
        changeWallpaperVisiblity(true);
    }

    private void markIntroScreenDismissed() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(INTRO_SCREEN_DISMISSED, true);
        editor.apply();
    }

    private void showFirstRunClings() {
        LauncherClings launcherClings = new LauncherClings(this);
        if (launcherClings.shouldShowFirstRunOrMigrationClings()) {
            if (mModel.canMigrateFromOldLauncherDb(this)) {
                launcherClings.showMigrationCling();
            } else {
                launcherClings.showLongPressCling(true);
            }
        }
    }

    void showWorkspaceSearchAndHotseat() {
        if (mWorkspace != null)
            mWorkspace.setAlpha(1f);
        if (mHotseat != null)
            mHotseat.setAlpha(1f);
        if (mPageIndicators != null)
            mPageIndicators.setAlpha(1f);
        if (mSearchDropTargetBar != null)
            mSearchDropTargetBar.showSearchBar(false);
    }

    void hideWorkspaceSearchAndHotseat() {
        if (mWorkspace != null)
            mWorkspace.setAlpha(0f);
        if (mHotseat != null)
            mHotseat.setAlpha(0f);
        if (mPageIndicators != null)
            mPageIndicators.setAlpha(0f);
        if (mSearchDropTargetBar != null)
            mSearchDropTargetBar.hideSearchBar(false);
    }

    public ItemInfo createAppDragInfo(Intent appLaunchIntent) {
        final UserHandleCompat myUser = UserHandleCompat.myUserHandle();
        LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
        LauncherActivityInfoCompat activityInfo = launcherApps.resolveActivity(appLaunchIntent, myUser);
        if (activityInfo == null) {
            return null;
        }
        return new AppInfo(this, activityInfo, myUser, mIconCache, null);
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption, Bitmap icon) {
        return createShortcutDragInfo(shortcutIntent, caption, icon, UserHandleCompat.myUserHandle());
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption, Bitmap icon, UserHandleCompat user) {
        UserManagerCompat userManager = UserManagerCompat.getInstance(this);
        CharSequence contentDescription = userManager.getBadgedLabelForUser(caption, user);
        return new ShortcutInfo(shortcutIntent, caption, contentDescription, icon, user);
    }

    protected void moveWorkspaceToDefaultScreen() {
        mWorkspace.moveToDefaultScreen(false);
    }

    public void startDrag(View dragView, ItemInfo dragInfo, DragSource source) {
        dragView.setTag(dragInfo);
        mWorkspace.onExternalDragStartedWithItem(dragView);
        mWorkspace.beginExternalDragShared(dragView, source);
    }

    @Override
    public void onPageSwitch(View newPage, int newPageIndex) {
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.onPageSwitch(newPage, newPageIndex);
        }
    }

    public void dumpState() {
        Log.d(TAG, "BEGIN launcher3 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher3 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        synchronized (sDumpLogs) {
            writer.println(" ");
            writer.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                writer.println("  " + sDumpLogs.get(i));
            }
        }
        if (mLauncherCallbacks != null) {
            mLauncherCallbacks.dump(prefix, fd, writer, args);
        }
    }

    public static void dumpDebugLogsToConsole() {
        if (DEBUG_DUMP_LOG) {
            synchronized (sDumpLogs) {
                Log.d(TAG, "");
                Log.d(TAG, "*********************");
                Log.d(TAG, "Launcher debug logs: ");
                for (int i = 0; i < sDumpLogs.size(); i++) {
                    Log.d(TAG, "  " + sDumpLogs.get(i));
                }
                Log.d(TAG, "*********************");
                Log.d(TAG, "");
            }
        }
    }

    public static void addDumpLog(String tag, String log, boolean debugLog) {
        addDumpLog(tag, log, null, debugLog);
    }

    public static void addDumpLog(String tag, String log, Exception e, boolean debugLog) {
        if (debugLog) {
            if (e != null) {
                Log.d(tag, log, e);
            } else {
                Log.d(tag, log);
            }
        }
        if (DEBUG_DUMP_LOG) {
            sDateStamp.setTime(System.currentTimeMillis());
            synchronized (sDumpLogs) {
                sDumpLogs.add(sDateFormat.format(sDateStamp) + ": " + tag + ", " + log + (e == null ? "" : (", Exception: " + e)));
            }
        }
    }

    public static CustomAppWidget getCustomAppWidget(String name) {
        return sCustomAppWidgets.get(name);
    }

    public static HashMap<String, CustomAppWidget> getCustomAppWidgets() {
        return sCustomAppWidgets;
    }

    public void dumpLogsToLocalData() {
        if (DEBUG_DUMP_LOG) {
            new AsyncTask<Void, Void, Void>() {

                public Void doInBackground(Void... args) {
                    boolean success = false;
                    sDateStamp.setTime(sRunStart);
                    String FILENAME = sDateStamp.getMonth() + "-" + sDateStamp.getDay() + "_" + sDateStamp.getHours() + "-" + sDateStamp.getMinutes() + "_" + sDateStamp.getSeconds() + ".txt";
                    FileOutputStream fos = null;
                    File outFile = null;
                    try {
                        outFile = new File(getFilesDir(), FILENAME);
                        outFile.createNewFile();
                        fos = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fos != null) {
                        PrintWriter writer = new PrintWriter(fos);
                        writer.println(" ");
                        writer.println("Debug logs: ");
                        synchronized (sDumpLogs) {
                            for (int i = 0; i < sDumpLogs.size(); i++) {
                                writer.println("  " + sDumpLogs.get(i));
                            }
                        }
                        writer.close();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                            success = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }
}

interface LauncherTransitionable {

    View getContent();

    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);

    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);

    void onLauncherTransitionStep(Launcher l, float t);

    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}

interface DebugIntents {

    static final String DELETE_DATABASE = "com.android.launcher3.action.DELETE_DATABASE";

    static final String MIGRATE_DATABASE = "com.android.launcher3.action.MIGRATE_DATABASE";
}
