package cgeo.geocaching.maps.mapsforge.v6;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.R;
import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.maps.mapsforge.v6.layers.ITileLayer;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.storage.ContentStorage;
import cgeo.geocaching.storage.ContentStorage.FileInformation;
import cgeo.geocaching.storage.Folder;
import cgeo.geocaching.storage.FolderUtils;
import cgeo.geocaching.storage.LocalStorage;
import cgeo.geocaching.storage.PersistableFolder;
import cgeo.geocaching.storage.extension.OneTimeDialogs;
import cgeo.geocaching.ui.dialog.Dialogs;
import cgeo.geocaching.utils.FileUtils;
import cgeo.geocaching.utils.Formatter;
import cgeo.geocaching.utils.LocalizationUtils;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.TextUtils;
import cgeo.geocaching.utils.UriUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.rendertheme.ContentRenderTheme;
import org.mapsforge.map.android.rendertheme.ContentResolverResourceProvider;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.rendertheme.ZipRenderTheme;
import org.mapsforge.map.rendertheme.ZipXmlThemeResourceProvider;

public class RenderThemeHelper implements XmlRenderThemeMenuCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final PersistableFolder MAP_THEMES_FOLDER = PersistableFolder.OFFLINE_MAP_THEMES;

    private static final File MAP_THEMES_INTERNAL_FOLDER = LocalStorage.getMapThemeInternalSyncDir();

    private static final String ZIP_THEME_SEPARATOR = ":";

    private static final int ZIP_FILE_SIZE_LIMIT = 5 * 1024 * 1024;

    private static final int ZIP_RESOURCE_READ_LIMIT = 1024 * 1024;

    private static final long FILESYNC_MAX_FILESIZE = 5 * 1024 * 1024;

    private static final Object availableThemesMutex = new Object();

    private static final List<ThemeData> availableThemes = new ArrayList<>();

    private static final Object cachedZipMutex = new Object();

    private final Activity activity;

    private final SharedPreferences sharedPreferences;

    private XmlRenderThemeStyleMenu themeStyleMenu;

    private String prefThemeOptionMapStyle = "";

    private static String cachedZipProviderFilename = null;

    private static ZipXmlThemeResourceProvider cachedZipProvider = null;

    private static MapThemeFolderSynchronizer syncTask = null;

    static {
        try {
            recalculateAvailableThemes();
        } catch (Exception e) {
            Log.e("Error initializing RenderThemeHelper", e);
        }
    }

    private static class ThemeData {

        public final String id;

        public final String userDisplayableName;

        public final FileInformation fileInfo;

        public final Folder containingFolder;

        private ThemeData(final String id, final String userDisplayableName, final FileInformation fileInfo, final Folder containingFolder) {
            this.id = id;
            this.userDisplayableName = userDisplayableName;
            this.fileInfo = fileInfo;
            this.containingFolder = containingFolder;
        }
    }

    public RenderThemeHelper(final Activity activity) {
        this.activity = activity;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void reapplyMapTheme(final ITileLayer tileLayer, final TileCache tileCache) {
        if (tileLayer == null || tileLayer.getTileLayer() == null) {
            return;
        }
        if (!tileLayer.hasThemes()) {
            tileLayer.getTileLayer().requestRedraw();
            return;
        }
        final TileRendererLayer rendererLayer = (TileRendererLayer) tileLayer.getTileLayer();
        ThemeData selectedTheme = setSelectedMapThemeInternal(Settings.getSelectedMapRenderTheme());
        if (selectedTheme == null) {
            rendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        } else {
            try {
                final XmlRenderTheme xmlRenderTheme = createThemeFor(selectedTheme);
                org.mapsforge.map.rendertheme.rule.RenderThemeHandler.getRenderTheme(AndroidGraphicFactory.INSTANCE, new DisplayModel(), xmlRenderTheme);
                rendererLayer.setXmlRenderTheme(xmlRenderTheme);
            } catch (final IOException e) {
                Log.w("Failed to set render theme", e);
                ActivityMixin.showApplicationToast(LocalizationUtils.getString(R.string.err_rendertheme_file_unreadable));
                rendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                selectedTheme = null;
            } catch (final Exception e) {
                Log.w("render theme invalid", e);
                ActivityMixin.showApplicationToast(LocalizationUtils.getString(R.string.err_rendertheme_invalid));
                rendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                selectedTheme = null;
            }
        }
        setSelectedTheme(selectedTheme);
        if (tileCache != null) {
            tileCache.purge();
        }
        rendererLayer.requestRedraw();
    }

    private XmlRenderTheme createThemeFor(@NonNull final ThemeData theme) throws IOException {
        final String[] themeIdTokens = theme.id.split(ZIP_THEME_SEPARATOR);
        final boolean isZipTheme = themeIdTokens.length == 2;
        if (theme.fileInfo == null || theme.fileInfo.uri == null || theme.containingFolder == null) {
            return null;
        }
        XmlRenderTheme xmlRenderTheme = null;
        try {
            if (!isZipTheme) {
                if (UriUtils.isFileUri(theme.fileInfo.uri)) {
                    xmlRenderTheme = new ExternalRenderTheme(UriUtils.toFile(theme.fileInfo.uri), this);
                } else {
                    Dialogs.basicOneTimeMessage(activity, OneTimeDialogs.DialogType.MAP_THEME_FIX_SLOWNESS);
                    xmlRenderTheme = new ContentRenderTheme(getContentResolver(), theme.fileInfo.uri, this);
                    xmlRenderTheme.setResourceProvider(new ContentResolverResourceProvider(getContentResolver(), ContentStorage.get().getUriForFolder(theme.containingFolder), true));
                }
            } else {
                synchronized (cachedZipMutex) {
                    if (cachedZipProvider == null || !themeIdTokens[0].equals(cachedZipProviderFilename)) {
                        if (theme.fileInfo.size > ZIP_FILE_SIZE_LIMIT) {
                            cachedZipProvider = null;
                            cachedZipProviderFilename = null;
                        } else {
                            cachedZipProvider = new ZipXmlThemeResourceProvider(new ZipInputStream(ContentStorage.get().openForRead(theme.fileInfo.uri)), ZIP_RESOURCE_READ_LIMIT);
                            cachedZipProviderFilename = themeIdTokens[0];
                        }
                    }
                    xmlRenderTheme = cachedZipProvider == null ? null : new ZipRenderTheme(themeIdTokens[1], cachedZipProvider, this);
                }
            }
        } catch (Exception ex) {
            Log.w("Problem loading Theme [" + theme.id + "]'" + theme.fileInfo.uri + "'", ex);
            xmlRenderTheme = null;
            cachedZipProvider = null;
            cachedZipProviderFilename = null;
        }
        return xmlRenderTheme;
    }

    public void selectMapTheme(final ITileLayer tileLayer, final TileCache tileCache) {
        final String currentThemeId = Settings.getSelectedMapRenderTheme();
        final boolean debugMode = Settings.isDebug();
        final List<String> names = new ArrayList<>();
        names.add(LocalizationUtils.getString(R.string.switch_default));
        int currentItem = 0;
        int idx = 1;
        final List<ThemeData> selectableAvThemes = getAvailableThemes();
        for (final ThemeData theme : selectableAvThemes) {
            names.add(theme.userDisplayableName + (debugMode ? " (" + theme.id + ")" : ""));
            if (StringUtils.equals(currentThemeId, theme.id)) {
                currentItem = idx;
            }
            idx++;
        }
        final AlertDialog.Builder builder = Dialogs.newBuilder(activity);
        String title = activity.getString(R.string.map_theme_select);
        if (debugMode) {
            title = title + " (debug mode, sync = " + (isThemeSynchronizationActive() ? "ON" : "off") + ")";
        }
        builder.setTitle(title);
        builder.setSingleChoiceItems(names.toArray(new String[0]), currentItem, (dialog, newItem) -> {
            setSelectedTheme(newItem > 0 ? selectableAvThemes.get(newItem - 1) : null);
            reapplyMapTheme(tileLayer, tileCache);
            dialog.cancel();
        });
        builder.show();
    }

    public void selectMapThemeOptions() {
        final Intent intent = new Intent(activity, RenderThemeSettings.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        if (themeStyleMenu != null) {
            intent.putExtra(RenderThemeSettings.RENDERTHEME_MENU, themeStyleMenu);
        }
        activity.startActivity(intent);
    }

    public boolean themeOptionsAvailable() {
        return !StringUtils.isBlank(Settings.getSelectedMapRenderTheme());
    }

    @Override
    public Set<String> getCategories(final XmlRenderThemeStyleMenu menu) {
        themeStyleMenu = menu;
        prefThemeOptionMapStyle = menu.getId();
        final String id = this.sharedPreferences.getString(themeStyleMenu.getId(), themeStyleMenu.getDefaultValue());
        final XmlRenderThemeStyleLayer baseLayer = themeStyleMenu.getLayer(id);
        if (baseLayer == null) {
            Log.w("Invalid style " + id);
            return null;
        }
        final Set<String> result = baseLayer.getCategories();
        for (final XmlRenderThemeStyleLayer overlay : baseLayer.getOverlays()) {
            if (this.sharedPreferences.getBoolean(overlay.getId(), overlay.isEnabled())) {
                result.addAll(overlay.getCategories());
            }
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s) {
        if (StringUtils.equals(s, prefThemeOptionMapStyle)) {
            AndroidUtil.restartActivity(activity);
        }
    }

    public void onDestroy() {
        if (this.sharedPreferences != null) {
            this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    public static boolean setSelectedMapThemeDirect(final String themeIdCandidate) {
        return setSelectedMapThemeInternal(themeIdCandidate) != null;
    }

    private static ThemeData setSelectedMapThemeInternal(final String themeIdCandidate) {
        ThemeData selectedTheme = null;
        final List<ThemeData> avThemes = getAvailableThemes();
        for (ThemeData avTheme : avThemes) {
            if (avTheme.id.equals(themeIdCandidate)) {
                selectedTheme = avTheme;
                break;
            }
        }
        if (selectedTheme == null) {
            for (ThemeData avTheme : avThemes) {
                final String avThemeId = avTheme.id;
                if (themeIdCandidate.endsWith("/" + avThemeId)) {
                    selectedTheme = avTheme;
                    break;
                }
                if (avThemeId.startsWith(themeIdCandidate + ZIP_THEME_SEPARATOR)) {
                    selectedTheme = avTheme;
                    break;
                }
            }
        }
        setSelectedTheme(selectedTheme);
        return selectedTheme;
    }

    private static void setSelectedTheme(final ThemeData theme) {
        Settings.setSelectedMapRenderTheme(theme == null ? StringUtils.EMPTY : theme.id);
    }

    public static void resynchronizeOrDeleteMapThemeFolder() {
        MapThemeFolderSynchronizer.requestResynchronization(MAP_THEMES_FOLDER.getFolder(), MAP_THEMES_INTERNAL_FOLDER, isThemeSynchronizationActive(), result -> {
            if (result == null || result.result != FolderUtils.ProcessResult.OK || result.filesModified > 0) {
                recalculateAvailableThemes();
            }
        });
    }

    private static void recalculateAvailableThemes() {
        final List<ThemeData> newAvailableThemes = new ArrayList<>();
        addAvailableThemes(isThemeSynchronizationActive() ? Folder.fromFile(MAP_THEMES_INTERNAL_FOLDER) : MAP_THEMES_FOLDER.getFolder(), newAvailableThemes, "");
        Collections.sort(newAvailableThemes, (t1, t2) -> TextUtils.COLLATOR.compare(t1.userDisplayableName, t2.userDisplayableName));
        synchronized (availableThemesMutex) {
            availableThemes.clear();
            availableThemes.addAll(newAvailableThemes);
        }
        synchronized (cachedZipMutex) {
            cachedZipProvider = null;
            cachedZipProviderFilename = null;
        }
    }

    private static List<ThemeData> getAvailableThemes() {
        synchronized (availableThemesMutex) {
            return new ArrayList<>(availableThemes);
        }
    }

    private static void addAvailableThemes(@NonNull final Folder dir, final List<ThemeData> themes, final String prefix) {
        for (FileInformation candidate : Objects.requireNonNull(ContentStorage.get().list(dir))) {
            if (candidate.isDirectory) {
                addAvailableThemes(candidate.dirLocation, themes, prefix + candidate.name + "/");
            } else if (candidate.name.endsWith(".xml")) {
                final String themeId = prefix + candidate.name;
                themes.add(new ThemeData(themeId, toUserDisplayableName(candidate, null), candidate, dir));
            } else if (candidate.name.endsWith(".zip") && candidate.size <= ZIP_FILE_SIZE_LIMIT) {
                try (InputStream is = ContentStorage.get().openForRead(candidate.uri)) {
                    for (String zipXmlTheme : ZipXmlThemeResourceProvider.scanXmlThemes(new ZipInputStream(is))) {
                        final String themeId = prefix + candidate.name + ZIP_THEME_SEPARATOR + zipXmlTheme;
                        themes.add(new ThemeData(themeId, toUserDisplayableName(candidate, zipXmlTheme), candidate, dir));
                    }
                } catch (IOException ioe) {
                    Log.w("Map Theme ZIP '" + candidate + "' could not be read", ioe);
                } catch (Exception e) {
                    Log.w("Problem opening map Theme ZIP '" + candidate + "'", e);
                }
            }
        }
    }

    private static String toUserDisplayableName(final FileInformation file, final String zipPath) {
        String userDisplay = StringUtils.removeEnd(file.name, ".xml");
        if (zipPath != null) {
            final int idx = zipPath.lastIndexOf("/");
            userDisplay = userDisplay + "/" + StringUtils.removeEnd(idx < 0 ? zipPath : zipPath.substring(idx + 1), ".xml");
        }
        return userDisplay;
    }

    private static class MapThemeFolderSynchronizer extends AsyncTask<Void, Void, FolderUtils.FolderProcessResult> {

        private enum AfterSyncRequest {

            EXIT_NORMAL, REDO, ABORT_DELETE
        }

        private static final Object syncTaskMutex = new Object();

        private final Folder source;

        private final File target;

        private final Consumer<FolderUtils.FolderProcessResult> callback;

        private final AtomicBoolean cancelFlag = new AtomicBoolean(false);

        private final Object requestRedoMutex = new Object();

        private boolean taskIsDone = false;

        private AfterSyncRequest afterSyncRequest = AfterSyncRequest.EXIT_NORMAL;

        private long startTime = System.currentTimeMillis();

        public static void requestResynchronization(final Folder source, final File target, final boolean doSync, final Consumer<FolderUtils.FolderProcessResult> callback) {
            synchronized (syncTaskMutex) {
                if (syncTask == null || !syncTask.requestAfter(doSync ? MapThemeFolderSynchronizer.AfterSyncRequest.REDO : MapThemeFolderSynchronizer.AfterSyncRequest.ABORT_DELETE)) {
                    if (doSync) {
                        Log.i("[MapThemeFolderSync] start synchronization " + source + " -> " + target);
                        syncTask = new MapThemeFolderSynchronizer(source, target, callback);
                        syncTask.execute();
                    } else {
                        syncTask = null;
                        FileUtils.deleteDirectory(target);
                        if (callback != null) {
                            callback.accept(null);
                        }
                    }
                }
            }
        }

        private MapThemeFolderSynchronizer(final Folder source, final File target, final Consumer<FolderUtils.FolderProcessResult> callback) {
            this.source = source;
            this.target = target;
            this.callback = callback;
        }

        public boolean requestAfter(final AfterSyncRequest afterSyncRequest) {
            synchronized (requestRedoMutex) {
                if (taskIsDone) {
                    return false;
                }
                Log.i("[MapThemeFolderSync] Requesting '" + afterSyncRequest + "' " + source + " -> " + target);
                cancelFlag.set(true);
                this.afterSyncRequest = afterSyncRequest;
                startTime = System.currentTimeMillis();
                return true;
            }
        }

        @Override
        protected FolderUtils.FolderProcessResult doInBackground(final Void[] params) {
            Log.i("[MapThemeFolderSync] start synchronization " + source + " -> " + target);
            while (true) {
                final FolderUtils.FolderProcessResult result = FolderUtils.get().synchronizeFolder(source, target, MapThemeFolderSynchronizer::shouldBeSynced, cancelFlag, null);
                synchronized (requestRedoMutex) {
                    switch(afterSyncRequest) {
                        case EXIT_NORMAL:
                            taskIsDone = true;
                            return result;
                        case ABORT_DELETE:
                            FileUtils.deleteDirectory(target);
                            return result;
                        case REDO:
                            Log.i("[MapThemeFolderSync] redo synchronization " + source + " -> " + target);
                            cancelFlag.set(false);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(final FolderUtils.FolderProcessResult result) {
            Log.i("[MapThemeFolderSync] Finished synchronization (state=" + afterSyncRequest + ")");
            if (result.filesModified > 0) {
                showToast(R.string.mapthemes_foldersync_finished_toast, LocalizationUtils.getString(R.string.persistablefolder_offline_maps_themes), Formatter.formatDuration(System.currentTimeMillis() - startTime), result.filesModified, LocalizationUtils.getPlural(R.plurals.file_count, result.filesInSource, "file(s)"));
            }
            if (callback != null) {
                callback.accept(afterSyncRequest == AfterSyncRequest.ABORT_DELETE ? null : result);
            }
            Log.i("[MapThemeFolderSync] Finished synchronization callback");
        }

        private static boolean shouldBeSynced(final FileInformation fileInfo) {
            return fileInfo != null && !fileInfo.name.endsWith(".map") && fileInfo.size <= FILESYNC_MAX_FILESIZE;
        }

        private static void showToast(final int resId, final Object... params) {
            final ImmutablePair<String, String> msgs = LocalizationUtils.getMultiPurposeString(resId, "RenderTheme", params);
            ActivityMixin.showApplicationToast(msgs.left);
            Log.iForce("[RenderThemeHelper.ThemeFolderSyncTask]" + msgs.right);
        }
    }

    private static ContentResolver getContentResolver() {
        return CgeoApplication.getInstance().getContentResolver();
    }

    public static boolean isThemeSynchronizationActive() {
        return Settings.getSyncMapRenderThemeFolder();
    }

    public static boolean changeSyncSetting(final Activity activity, final boolean doSync, final Consumer<Boolean> callback) {
        if (doSync) {
            final FolderUtils.FolderInfo themeFolderInfo = FolderUtils.get().getFolderInfo(PersistableFolder.OFFLINE_MAP_THEMES.getFolder(), -1);
            final String folderName = MAP_THEMES_FOLDER.getFolder().toUserDisplayableString();
            final ImmutableTriple<String, String, String> folderInfoStrings = themeFolderInfo.getUserDisplayableFolderInfoStrings();
            Dialogs.newBuilder(activity).setTitle(R.string.init_renderthemefolder_synctolocal_dialog_title).setMessage(LocalizationUtils.getString(R.string.init_renderthemefolder_synctolocal_dialog_message, folderName, folderInfoStrings.left, folderInfoStrings.middle, folderInfoStrings.right)).setPositiveButton(android.R.string.ok, (d, c) -> {
                d.dismiss();
                resynchronizeOrDeleteMapThemeFolder();
                callback.accept(true);
            }).setNegativeButton(android.R.string.cancel, (d, c) -> {
                d.dismiss();
                callback.accept(false);
                resynchronizeOrDeleteMapThemeFolder();
            }).create().show();
        } else {
            Settings.setSyncMapRenderThemeFolder(false);
            resynchronizeOrDeleteMapThemeFolder();
            callback.accept(false);
        }
        return true;
    }
}
