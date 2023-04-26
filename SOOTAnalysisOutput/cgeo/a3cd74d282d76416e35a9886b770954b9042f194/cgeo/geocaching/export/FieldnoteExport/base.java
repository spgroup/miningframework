package cgeo.geocaching.export;

import cgeo.geocaching.LogEntry;
import cgeo.geocaching.R;
import cgeo.geocaching.cgCache;
import cgeo.geocaching.cgeoapplication;
import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.activity.Progress;
import cgeo.geocaching.connector.gc.Login;
import cgeo.geocaching.enumerations.StatusCode;
import cgeo.geocaching.network.Network;
import cgeo.geocaching.network.Parameters;
import cgeo.geocaching.utils.Log;
import org.apache.commons.lang3.StringUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class FieldnoteExport extends AbstractExport {

    private static final File exportLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/field-notes");

    private static final SimpleDateFormat fieldNoteDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    protected FieldnoteExport() {
        super(getString(R.string.export_fieldnotes));
    }

    private class ExportOptionsDialog extends AlertDialog {

        public ExportOptionsDialog(final List<cgCache> caches, final Activity activity) {
            super(activity);
            View layout = activity.getLayoutInflater().inflate(R.layout.fieldnote_export_dialog, null);
            setView(layout);
            final CheckBox uploadOption = (CheckBox) layout.findViewById(R.id.upload);
            final CheckBox onlyNewOption = (CheckBox) layout.findViewById(R.id.onlynew);
            uploadOption.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onlyNewOption.setEnabled(uploadOption.isChecked());
                }
            });
            layout.findViewById(R.id.export).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    new ExportTask(caches, activity, uploadOption.isChecked(), onlyNewOption.isChecked()).execute((Void) null);
                }
            });
        }
    }

    @Override
    public void export(final List<cgCache> caches, final Activity activity) {
        if (null == activity) {
            new ExportTask(caches, null, false, false).execute((Void) null);
        } else {
            new ExportOptionsDialog(caches, activity).show();
        }
    }

    private class ExportTask extends AsyncTask<Void, Integer, Boolean> {

        private final List<cgCache> caches;

        private final Activity activity;

        private final boolean upload;

        private final boolean onlyNew;

        private final Progress progress = new Progress();

        private File exportFile;

        private static final int STATUS_UPLOAD = -1;

        public ExportTask(final List<cgCache> caches, final Activity activity, final boolean upload, final boolean onlyNew) {
            this.caches = caches;
            this.activity = activity;
            this.upload = upload;
            this.onlyNew = onlyNew;
        }

        @Override
        protected void onPreExecute() {
            if (null != activity) {
                progress.show(activity, getString(R.string.export) + ": " + getName(), getString(R.string.export_fieldnotes_creating), true, null);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final StringBuilder fieldNoteBuffer = new StringBuilder();
            final cgeoapplication app = cgeoapplication.getInstance();
            try {
                int i = 0;
                for (cgCache cache : caches) {
                    if (cache.isLogOffline()) {
                        appendFieldNote(fieldNoteBuffer, cache, app.loadLogOffline(cache.getGeocode()));
                        publishProgress(++i);
                    }
                }
            } catch (Exception e) {
                Log.e("FieldnoteExport.ExportTask generation", e);
                return false;
            }
            fieldNoteBuffer.append('\n');
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                exportLocation.mkdirs();
                SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                exportFile = new File(exportLocation.toString() + '/' + fileNameDateFormat.format(new Date()) + ".txt");
                OutputStream os;
                Writer fw = null;
                try {
                    os = new FileOutputStream(exportFile);
                    fw = new OutputStreamWriter(os, "UTF-16");
                    fw.write(fieldNoteBuffer.toString());
                } catch (IOException e) {
                    Log.e("FieldnoteExport.ExportTask export", e);
                    return false;
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            Log.e("FieldnoteExport.ExportTask export", e);
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
            if (upload) {
                publishProgress(STATUS_UPLOAD);
                final String uri = "http://www.geocaching.com/my/uploadfieldnotes.aspx";
                if (!Login.isActualLoginStatus()) {
                    final StatusCode loginState = Login.login();
                    if (loginState != StatusCode.NO_ERROR) {
                        Log.e("FieldnoteExport.ExportTask upload: Login failed");
                    }
                }
                String page = Network.getResponseData(Network.getRequest(uri));
                if (!Login.getLoginStatus(page)) {
                    final StatusCode loginState = Login.login();
                    if (loginState == StatusCode.NO_ERROR) {
                        page = Network.getResponseData(Network.getRequest(uri));
                    } else {
                        Log.e("FieldnoteExport.ExportTask upload: No login (error: " + loginState + ')');
                        return false;
                    }
                }
                final String[] viewstates = Login.getViewstates(page);
                final Parameters uploadParams = new Parameters("__EVENTTARGET", "", "__EVENTARGUMENT", "", "ctl00$ContentBody$btnUpload", "Upload Field Note");
                if (onlyNew) {
                    uploadParams.put("ctl00$ContentBody$chkSuppressDate", "on");
                }
                Login.putViewstates(uploadParams, viewstates);
                Network.getResponseData(Network.postRequest(uri, uploadParams, "ctl00$ContentBody$FieldNoteLoader", "text/plain", exportFile));
                if (StringUtils.isBlank(page)) {
                    Log.e("FieldnoteExport.ExportTask upload: No data from server");
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (null != activity) {
                progress.dismiss();
                if (result) {
                    ActivityMixin.showToast(activity, getName() + ' ' + getString(R.string.export_exportedto) + ": " + exportFile.toString());
                    if (upload) {
                        ActivityMixin.showToast(activity, getString(R.string.export_fieldnotes_upload_success));
                    }
                } else {
                    ActivityMixin.showToast(activity, getString(R.string.export_failed));
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... status) {
            if (null != activity) {
                if (STATUS_UPLOAD == status[0]) {
                    progress.setMessage(getString(R.string.export_fieldnotes_uploading));
                } else {
                    progress.setMessage(getString(R.string.export_fieldnotes_creating) + " (" + status[0] + ')');
                }
            }
        }
    }

    static void appendFieldNote(final StringBuilder fieldNoteBuffer, final cgCache cache, final LogEntry log) {
        fieldNoteBuffer.append(cache.getGeocode()).append(',').append(fieldNoteDateFormat.format(new Date(log.date))).append(',').append(StringUtils.capitalize(log.type.type)).append(",\"").append(StringUtils.replaceChars(log.log, '"', '\'')).append("\"\n");
    }
}
