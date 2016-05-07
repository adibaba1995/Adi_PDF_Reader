package com.adisoftwares.bookreader.file_chooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adisoftwares.bookreader.NavigationViewActivity;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.pdf.PdfViewActivity;
import com.adisoftwares.bookreader.view.ObservableListView;
import com.adisoftwares.bookreader.view.ObservableScrollViewCallbacks;
import com.adisoftwares.bookreader.view.ScrollState;
import com.adisoftwares.bookreader.view.Scrollable;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class DirectoryFragment<S extends Scrollable> extends Fragment implements ObservableScrollViewCallbacks {

    private View fragmentView;
    private boolean receiverRegistered = false;
    private File currentDir;

    private ObservableListView listView;
    private ListAdapter listAdapter;
    private TextView emptyView;
    private Toolbar toolbar;

    private static String title_ = "";
    private ArrayList<ListItem> items;
    private ArrayList<HistoryEntry> history;
    private HashMap<String, ListItem> selectedFiles = new HashMap<String, ListItem>();
    private long sizeLimit = 1024 * 1024 * 1024;

    private String[] chhosefileType = {".pdf"};


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (scrollY < 0) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollY >= 0) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(toolbar) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(toolbar) == -toolbar.getHeight();
    }

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-toolbar.getHeight());
    }

    private void moveToolbar(float toTranslationY) {
        if (ViewHelper.getTranslationY(toolbar) == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(toolbar), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                ViewHelper.setTranslationY(toolbar, translationY);
                ViewHelper.setTranslationY((View) listView, translationY);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ((View) listView).getLayoutParams();
                lp.height = (int) -translationY + getScreenHeight() - lp.topMargin;
                ((View) listView).requestLayout();
            }
        });
        animator.start();
    }

    protected int getScreenHeight() {
        return getActivity().findViewById(android.R.id.content).getHeight();
    }

    private class HistoryEntry implements Serializable {
        int scrollItem, scrollOffset;
        File dir;
        String title;
    }

    public boolean onBackPressed_() {
        if (history.size() > 0) {
            HistoryEntry he = history.remove(history.size() - 1);
            title_ = he.title;
            updateName(title_);
            if (he.dir != null) {
                listFiles(he.dir);
            } else {
                listRoots();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
            }
            return false;
        } else {
            return true;
        }
    }

    private void updateName(String title_) {
        if (toolbar != null) {
            toolbar.setSubtitle(title_);
        }
    }

    public void onFragmentDestroy() {
        try {
            if (receiverRegistered) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (currentDir == null) {
                            listRoots();
                        } else {
                            listFiles(currentDir);
                        }
                    } catch (Exception e) {
                        Log.e("tmessages", e.toString());
                    }
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                listView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    };

    protected ObservableListView createScrollable(View fragmentView) {
        ObservableListView listView = (ObservableListView) fragmentView.findViewById(R.id.scrollable);

        return listView;
    }

    private class ListItem implements Serializable {
        int icon;
        String title;
        String subtitle = "";
        String ext = "";
        String thumb;
        File file;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title_ = savedInstanceState.getString(getString(R.string.file_picker_title));
            items = (ArrayList<ListItem>) savedInstanceState.getSerializable(getString(R.string.file_picker_list));
            history = (ArrayList<HistoryEntry>) savedInstanceState.getSerializable(getString(R.string.file_picker_history_entry));
        } else {
            items = new ArrayList<>();
            history = new ArrayList<>();
        }
        if (!receiverRegistered) {
            receiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            getActivity().registerReceiver(receiver, filter);
        }
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.document_select_layout,
                    container, false);

            toolbar = (Toolbar) fragmentView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.app_name);
            ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);

            listAdapter = new ListAdapter(getActivity());
            emptyView = (TextView) fragmentView
                    .findViewById(R.id.searchEmptyView);
            emptyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            listView = (ObservableListView) fragmentView.findViewById(R.id.listView);
            listView.setEmptyView(emptyView);
            listView.setAdapter(listAdapter);
            listView.setScrollViewCallbacks(this);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int i, long l) {
                    boolean flag = false;
                    if (i < 0 || i >= items.size()) {
                        return;
                    }
                    ListItem item = items.get(i);
                    File file = item.file;
                    if (file == null) {
                        HistoryEntry he = history.remove(history.size() - 1);
                        title_ = he.title;
                        updateName(title_);
                        if (he.dir != null) {
                            listFiles(he.dir);
                        } else {
                            listRoots();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            listView.setSelectionFromTop(he.scrollItem,
                                    he.scrollOffset);
                        }
                    } else if (file.isDirectory()) {
                        HistoryEntry he = new HistoryEntry();
                        he.scrollItem = listView.getFirstVisiblePosition();
                        he.scrollOffset = listView.getChildAt(0).getTop();
                        he.dir = currentDir;
                        he.title = title_.toString();
                        updateName(title_);
                        if (!listFiles(file)) {
                            return;
                        }
                        history.add(he);
                        title_ = item.title;
                        updateName(title_);
                        listView.setSelection(0);
                    } else {
                        if (!file.canRead()) {
                            showErrorBox("AccessError");
                            return;
                        }
                        if (sizeLimit != 0) {
                            if (file.length() > sizeLimit) {
                                showErrorBox("FileUploadLimit");
                                return;
                            }
                        }
                        if (file.length() == 0) {
                            return;
                        }
                        for (int j = 0; j < chhosefileType.length; j++) {
                            if (file.toString().contains(chhosefileType[j])) {
                                String fileName = file.getAbsolutePath();
                                Intent intent;
                                if (fileName.endsWith(".pdf")) {
                                    Uri uri = Uri.parse(fileName);
                                    intent = new Intent(getActivity(), PdfViewActivity.class);
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(uri);

                                                   /*intent = new Intent(getActivity(), PdfViewActivity.class);
                                                   intent.putExtra(PdfViewActivity.EXTRA_FILE_PATH, booksList.get(position).getPath());*/
                                    startActivity(intent);
                                }
                                flag = true;
                                break;
                            }
                        }
//                        if (file.toString().contains(chhosefileType[0]) ||
//                                file.toString().contains(chhosefileType[1]) ||
//                                file.toString().contains(chhosefileType[2]) ||
//                                file.toString().contains(chhosefileType[3]) ||
//                                file.toString().contains(chhosefileType[4])) {
//                            if (delegate != null) {
//                                ArrayList<String> files = new ArrayList<String>();
//                                files.add(file.getAbsolutePath());
//                                delegate.didSelectFiles(DirectoryFragment.this, files);
//                            }
//                        }
                        if (!flag) {
                            showErrorBox("Choose correct file.");
                            return;
                        }

                    }
                }
            });

            if (savedInstanceState == null)
                listRoots();
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.file_picker_title), title_);
        outState.putSerializable(getString(R.string.file_picker_list), items);
        outState.putSerializable(getString(R.string.file_picker_history_entry), history);
    }

    private void listRoots() {
        currentDir = null;
        items.clear();
        String extStorage = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        ListItem ext = new ListItem();
        if (Build.VERSION.SDK_INT < 9
                || Environment.isExternalStorageRemovable()) {
            ext.title = "SdCard";
        } else {
            ext.title = "InternalStorage";
        }
        ext.icon = Build.VERSION.SDK_INT < 9
                || Environment.isExternalStorageRemovable() ? R.drawable.ic_external_storage
                : R.drawable.ic_storage;
        ext.subtitle = getRootSubtitle(extStorage);
        ext.file = Environment.getExternalStorageDirectory();
        items.add(ext);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/proc/mounts"));
            String line;
            HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();
            ArrayList<String> result = new ArrayList<String>();
            String extDevice = null;
            while ((line = reader.readLine()) != null) {
                if ((!line.contains("/mnt") && !line.contains("/storage") && !line
                        .contains("/sdcard"))
                        || line.contains("asec")
                        || line.contains("tmpfs") || line.contains("none")) {
                    continue;
                }
                String[] info = line.split(" ");
                if (!aliases.containsKey(info[0])) {
                    aliases.put(info[0], new ArrayList<String>());
                }
                aliases.get(info[0]).add(info[1]);
                if (info[1].equals(extStorage)) {
                    extDevice = info[0];
                }
                result.add(info[1]);
            }
            reader.close();
            if (extDevice != null) {
                result.removeAll(aliases.get(extDevice));
                for (String path : result) {
                    try {
                        ListItem item = new ListItem();
                        if (path.toLowerCase().contains("sd")) {
                            ext.title = "SdCard";
                        } else {
                            ext.title = "ExternalStorage";
                        }
                        item.icon = R.drawable.ic_external_storage;
                        item.subtitle = getRootSubtitle(path);
                        item.file = new File(path);
                        items.add(item);
                    } catch (Exception e) {
                        Log.e("tmessages", e.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        ListItem fs = new ListItem();
        fs.title = "/";
        fs.subtitle = "SystemRoot";
        fs.icon = R.drawable.folder;
        fs.file = new File("/");
        items.add(fs);

        // try {
        // File telegramPath = new
        // File(Environment.getExternalStorageDirectory(), "Telegram");
        // if (telegramPath.exists()) {
        // fs = new ListItem();
        // fs.title = "Telegram";
        // fs.subtitle = telegramPath.toString();
        // fs.icon = R.drawable.ic_directory;
        // fs.file = telegramPath;
        // items.add(fs);
        // }
        // } catch (Exception e) {
        // FileLog.e("tmessages", e);
        // }

        // AndroidUtilities.clearDrawableAnimation(listView);
        // scrolling = true;
        listAdapter.notifyDataSetChanged();
    }

    private boolean listFiles(File dir) {
        if (!dir.canRead()) {
            if (dir.getAbsolutePath().startsWith(
                    Environment.getExternalStorageDirectory().toString())
                    || dir.getAbsolutePath().startsWith("/sdcard")
                    || dir.getAbsolutePath().startsWith("/mnt/sdcard")) {
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)
                        && !Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    currentDir = dir;
                    items.clear();
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_SHARED.equals(state)) {
                        emptyView.setText("UsbActive");
                    } else {
                        emptyView.setText("NotMounted");
                    }
                    clearDrawableAnimation(listView);
                    // scrolling = true;
                    listAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            showErrorBox("AccessError");
            return false;
        }
        emptyView.setText("NoFiles");
        File[] files = null;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            showErrorBox(e.getLocalizedMessage());
            return false;
        }
        if (files == null) {
            showErrorBox("UnknownError");
            return false;
        }
        currentDir = dir;
        items.clear();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() != rhs.isDirectory()) {
                    return lhs.isDirectory() ? -1 : 1;
                }
                return lhs.getName().compareToIgnoreCase(rhs.getName());
                /*
                 * long lm = lhs.lastModified(); long rm = lhs.lastModified();
				 * if (lm == rm) { return 0; } else if (lm > rm) { return -1; }
				 * else { return 1; }
				 */
            }
        });
        for (File file : files) {
            if (!file.isDirectory() && !(file.getAbsolutePath().endsWith(".pdf")))
                continue;
            if (file.getName().startsWith(".")) {
                continue;
            }
            ListItem item = new ListItem();
            item.title = file.getName();
            item.file = file;
            if (file.isDirectory()) {
                item.icon = R.drawable.folder;
                item.subtitle = "Folder";
            } else {
                String fname = file.getName();
                String[] sp = fname.split("\\.");
                item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                item.subtitle = formatFileSize(file.length());
                fname = fname.toLowerCase();
                if (fname.endsWith(".jpg") || fname.endsWith(".png")
                        || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                    item.thumb = file.getAbsolutePath();
                }
            }
            items.add(item);
        }
        ListItem item = new ListItem();
        item.title = "..";
        item.subtitle = "Folder";
        item.icon = R.drawable.folder;
        item.file = null;
        items.add(0, item);
        clearDrawableAnimation(listView);
        // scrolling = true;
        listAdapter.notifyDataSetChanged();
        return true;
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable = null;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    public void showErrorBox(String error) {
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.app_name))
                .setMessage(error).setPositiveButton("OK", null).show();
    }

    private String getRootSubtitle(String path) {
        StatFs stat = new StatFs(path);
        long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        long free = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        if (total == 0) {
            return "";
        }
        return "Free " + formatFileSize(free) + " of " + formatFileSize(total);
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int pos) {
            return items.get(pos).subtitle.length() > 0 ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextDetailDocumentsCell(mContext);
            }
            TextDetailDocumentsCell textDetailCell = (TextDetailDocumentsCell) convertView;
            ListItem item = items.get(position);
            if (item.icon != 0) {
                ((TextDetailDocumentsCell) convertView)
                        .setTextAndValueAndTypeAndThumb(item.title,
                                item.subtitle, null, null, item.icon);
            } else {
                String type = item.ext.toUpperCase().substring(0,
                        Math.min(item.ext.length(), 4));
                ((TextDetailDocumentsCell) convertView)
                        .setTextAndValueAndTypeAndThumb(item.title,
                                item.subtitle, type, item.thumb, 0);
            }
            // if (item.file != null && actionBar.isActionModeShowed()) {
            // textDetailCell.setChecked(selectedFiles.containsKey(item.file.toString()),
            // !scrolling);
            // } else {
            // textDetailCell.setChecked(false, !scrolling);
            // }
            return convertView;
        }
    }

    public void finishFragment() {

    }

}
