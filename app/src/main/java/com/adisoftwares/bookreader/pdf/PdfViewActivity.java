package com.adisoftwares.bookreader.pdf;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.adisoftwares.bookreader.database.BookContract;
import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.ChoosePDFActivity;
import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReflowAdapter;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.PrintDialogActivity;
import com.artifex.mupdfdemo.ReaderView;
import com.artifex.mupdfdemo.SearchTask;
import com.artifex.mupdfdemo.SearchTaskResult;

import java.io.InputStream;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

//created by Artifex Software and modified a lot by Aditya Thanekar
//I have actually made a lot of modifications in this class. Such as I have added support for bookmarks, and much much more.
class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class PdfViewActivity extends AppCompatActivity implements FilePicker.FilePickerSupport {
    /* The core rendering instance */
    enum TopBarMode {
        Main, Search, Annot, Delete, More, Accept
    }

    enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText}

    private final int OUTLINE_REQUEST = 0;
    private final int PRINT_REQUEST = 1;
    private final int FILEPICK_REQUEST = 2;
    private final int PROOF_REQUEST = 3;
    private MuPDFCore core;
    private String mFileName;
    private MuPDFReaderView mDocView;
    private View mButtonsView;
    private boolean mButtonsVisible = true;
    private EditText mPasswordView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private TextView mFilenameView;
    @BindView(R.id.seekbar)
    SeekBar mPageSlider;
    private int mPageSliderRes;
    @BindView(R.id.page_no)
    TextView mPageNumberView;
    @BindView(R.id.pdfContainer)
    FrameLayout pdfContainer;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.outlineButton)
    ImageButton mOutlineButton;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private SearchTask mSearchTask;
    private AlertDialog.Builder mAlertBuilder;
    private boolean mLinkHighlight = false;
    private final Handler mHandler = new Handler();
    private boolean mAlertsActive = false;
    private boolean mReflow = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private FilePicker mFilePicker;
    private Menu optionsMenu;

    private String mProofFile;
    private boolean mSepEnabled[][];
    boolean bookmarked;

    static private AlertDialog.Builder gAlertBuilder;

    private String path;

    private ActionMode copyActionMode;

    private ShareActionProvider mShareActionProvider;

    private Unbinder unbinder;

    private TextSelectionData selectionCoordinates;

    static public AlertDialog.Builder getAlertBuilder() {
        return gAlertBuilder;
    }

    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = new String(lastSlashPos == -1
                ? path
                : path.substring(lastSlashPos + 1));
        try {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        } catch (java.lang.OutOfMemoryError e) {
            //  out of memory is not an Exception, so we catch it separately.
            System.out.println(e);
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[], String magic) {
        try {
            core = new MuPDFCore(this, buffer, magic);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    //  determine whether the current activity is a proofing activity.
//    public boolean isProofing() {
//        String format = core.fileFormat();
//        return (format.equals("GPROOF"));
//    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlertBuilder = new AlertDialog.Builder(this);
        gAlertBuilder = mAlertBuilder;  //  keep a static copy of this that other classes can use

        if (core == null) {
            core = (MuPDFCore) getLastNonConfigurationInstance();

            if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.filename))) {
                mFileName = savedInstanceState.getString(getString(R.string.filename));
            }
        }
        if (core == null) {
            Intent intent = getIntent();
            byte buffer[] = null;

            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri.toString().startsWith(getString(R.string.uri_start))) {
                    String reason = null;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        int len = is.available();
                        buffer = new byte[len];
                        is.read(buffer, 0, len);
                        is.close();
                    } catch (java.lang.OutOfMemoryError e) {
                        reason = e.toString();
                    } catch (Exception e) {
                        // Handle view requests from the Transformer Prime's file manager
                        // Hopefully other file managers will use this same scheme, if not
                        // using explicit paths.
                        // I'm hoping that this case below is no longer needed...but it's
                        // hard to test as the file manager seems to have changed in 4.x.
                        try {
                            Cursor cursor = getContentResolver().query(uri, new String[]{getString(R.string._data)}, null, null, null);
                            if (cursor.moveToFirst()) {
                                String str = cursor.getString(0);
                                if (str == null) {
                                } else {
                                    uri = Uri.parse(str);
                                }
                            }
                        } catch (Exception e2) {
                            reason = e2.toString();
                        }
                    }
                    if (reason != null) {
                        buffer = null;
                        Resources res = getResources();
                        AlertDialog alert = mAlertBuilder.create();
                        setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
                        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        alert.show();
                        return;
                    }
                }
                if (buffer != null) {
                    core = openBuffer(buffer, intent.getType());
                } else {
                    String path = Uri.decode(uri.getEncodedPath());
                    if (path == null) {
                        path = uri.toString();
                    }
                    core = openFile(path);
                }
                SearchTaskResult.set(null);
            }
            if (core != null && core.needsPassword()) {
                requestPassword(savedInstanceState);
                return;
            }
            if (core != null && core.countPages() == 0) {
                core = null;
            }
        }
        if (core == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            return;
        }

        createUI(savedInstanceState);

        //  hide the proof button if this file can't be proofed
//        if (!core.canProof()) {
//            mProofButton.setVisibility(View.INVISIBLE);
//        }
//
//        if (isProofing()) {
//
//            //  start the activity with a new array
//            mSepEnabled = null;
//
//            //  show the separations button
//            mSepsButton.setVisibility(View.VISIBLE);
//
//            //  hide some other buttons
//            mLinkButton.setVisibility(View.INVISIBLE);
//            mReflowButton.setVisibility(View.INVISIBLE);
//            mOutlineButton.setVisibility(View.INVISIBLE);
//            mSearchButton.setVisibility(View.INVISIBLE);
//            mMoreButton.setVisibility(View.INVISIBLE);
//        } else {
//            //  hide the separations button
//            mSepsButton.setVisibility(View.INVISIBLE);
//        }

    }

    public void requestPassword(final Bundle savedInstanceState) {
        mPasswordView = new EditText(this);
        mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog alert = mAlertBuilder.create();
        alert.setTitle(R.string.enter_password);
        alert.setView(mPasswordView);
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (core.authenticatePassword(mPasswordView.getText().toString())) {
                            createUI(savedInstanceState);
                        } else {
                            requestPassword(savedInstanceState);
                        }
                    }
                });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_view_activity_menu, menu);
        optionsMenu = menu;
        menu.findItem(R.id.bookmark).setIcon(checkBookmarked() ? R.drawable.bookmark : R.drawable.bookmark_border);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(1, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.print:
                printDoc();
                return true;
            case R.id.copy:
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                showInfo(getString(R.string.select_text));
                CopyActionModeCallback callback = new CopyActionModeCallback();
                copyActionMode = startSupportActionMode(callback);
                copyActionMode.setTitle(R.string.copy);
                return true;
            case R.id.reflow:
                toggleReflow();
                return true;
            case R.id.bookmark:
                if (checkBookmarked()) {
                    removeBookmark();
                    bookmarked = false;
                } else {
                    addBookmark();
                    bookmarked = true;
                }

                String message = bookmarked ? getString(R.string.bookmark_add) : getString(R.string.bookmark_remove);
                Toast.makeText(PdfViewActivity.this, message, Toast.LENGTH_SHORT).show();
                item.setIcon(bookmarked ? R.drawable.bookmark : R.drawable.bookmark_border);

                return true;

            case android.R.id.home:
                finish();
                return true;
            case R.id.search:

        }
        return super.onOptionsItemSelected(item);
    }

    //This method is used to check if the page is bookmarked or not
    private boolean checkBookmarked() {
        ContentResolver resolver = getContentResolver();
        String projection[] = {BookContract.BookmarkEntry.COLUMN_PATH, BookContract.BookmarkEntry.COLUMN_PAGE_NO};
        String selection = BookContract.BookmarkEntry.COLUMN_PATH + " = ? AND " + BookContract.BookmarkEntry.COLUMN_PAGE_NO + " = ?";
        String[] selectionArgs = new String[]{path, String.valueOf(mDocView.getDisplayedViewIndex())};
        //cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        Cursor cursor = resolver.query(BookContract.BookmarkEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst())
            return true;
        else
            return false;
    }

    //This method is used to add bookmark
    private void addBookmark() {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(BookContract.BookmarkEntry.COLUMN_PAGE_NO, mDocView.getDisplayedViewIndex());
        values.put(BookContract.BookmarkEntry.COLUMN_PATH, path);
        values.put(BookContract.BookmarkEntry.COLUMN_FILE_NAME, Utility.getFileNameFromUrl(path));
        values.put(BookContract.BookmarkEntry.COLUMN_TIME, Utility.getCurrentTime());
        getContentResolver().insert(BookContract.BookmarkEntry.CONTENT_URI, values);
    }

    private void addRecent() {
        if (core != null) {
            ContentResolver resolver = getContentResolver();
            String projection[] = {BookContract.RecentsEntry.COLUMN_PATH};
            String selection = BookContract.RecentsEntry.COLUMN_PATH + " = ?";
            String[] selectionArgs = new String[]{path};
            //cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
            Cursor cursor = resolver.query(BookContract.RecentsEntry.CONTENT_URI, projection, selection, selectionArgs, null);

            if (!cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(BookContract.RecentsEntry.COLUMN_PATH, path);
                values.put(BookContract.RecentsEntry.COLUMN_FILE_NAME, Utility.getFileNameFromUrl(path));
                values.put(BookContract.RecentsEntry.COLUMN_ADD_TIME, Utility.getCurrentTime());
                resolver.insert(BookContract.RecentsEntry.CONTENT_URI, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(BookContract.RecentsEntry.COLUMN_ADD_TIME, Utility.getCurrentTime());
                String where = BookContract.RecentsEntry.COLUMN_PATH + " = ?";
                String selectionArguments[] = {path};
                resolver.update(BookContract.RecentsEntry.CONTENT_URI, values, where, selectionArguments);
            }
        }
    }

    //This method is used to remove the bookmark
    private void removeBookmark() {
        ContentResolver resolver = getContentResolver();
        String where = BookContract.BookmarkEntry.COLUMN_PATH + " = ? AND " + BookContract.BookmarkEntry.COLUMN_PAGE_NO + " = ?";
        String selectionArgs[] = {path, String.valueOf(mDocView.getDisplayedViewIndex())};
        resolver.delete(BookContract.BookmarkEntry.CONTENT_URI, where, selectionArgs);
    }

    public void createUI(Bundle savedInstanceState) {
        if (core == null)
            return;

        // Now create the UI.

        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                mDocView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView.resetupChildren();
            }
        };

        setContentView(R.layout.activity_pdf_view);
        unbinder = ButterKnife.bind(PdfViewActivity.this);

        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.buttons_hidden)))
            mButtonsVisible = savedInstanceState.getBoolean(getString(R.string.buttons_hidden));

        if (!mButtonsVisible) {
            Log.d("Aditya", "hidden");
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    toolbar.setTranslationY(-toolbar.getHeight());
                }
            });
            bottomSheet.post(new Runnable() {
                @Override
                public void run() {
                    bottomSheet.setTranslationY(bottomSheet.getHeight());
                }
            });
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // First create the document view
        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;
                if (optionsMenu != null)
                    optionsMenu.findItem(R.id.bookmark).setIcon(checkBookmarked() ? R.drawable.bookmark : R.drawable.bookmark_border);

                mPageNumberView.setText(String.format("%d / %d", i + 1,
                        core.countPages()));
                mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    show();
                } else {
                    hide();
                }
                mButtonsVisible = !mButtonsVisible;
            }

            @Override
            protected void onDocMotion() {
                hide();
            }

            @Override
            protected void onHit(Hit item) {
                switch (mTopBarMode) {
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                        if (pageView != null)
                            pageView.deselectAnnotation();
                        break;
                }
            }
        };
        mDocView.setTextSelectedListener(new MuPDFReaderView.OnTextSelectedListener() {
            @Override
            public void onTextSelected(float x1, float y1, float x2, float y2) {
                selectionCoordinates = new TextSelectionData(x1, y1, x2, y2);
            }
        });
        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        mDocView.setDisplayedViewIndex(prefs.getInt(getString(R.string.page) + mFileName, 0));

        if (savedInstanceState != null && savedInstanceState.getBoolean(getString(R.string.reflow_mode), false))
            reflowModeSet(true);

        pdfContainer.addView(mDocView);
        toolbar.bringToFront();
        bottomSheet.bringToFront();

        path = getIntent().getData().toString();
        toolbar.setTitle(Utility.getFileNameFromUrl(path));

        // Set up the page slider
        int smax = Math.max(core.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        // Activate the seekbar
        mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
            }
        });

        mOutlineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PdfViewActivity.this, OutlineActivity.class);
                intent.putExtra(getString(R.string.pdf_core), core);
                intent.putExtra(getString(R.string.pdf_file_path), path);
                intent.putExtra(getString(R.string.pdf_page_no), mDocView.getDisplayedViewIndex());
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(PdfViewActivity.this, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                startActivityForResult(intent, OUTLINE_REQUEST, bndlanimation);
            }
        });
//        if (isProofing()) {
//            //  go to the current page
//            int currentPage = getIntent().getIntExtra("startingPage", 0);
//            mDocView.setDisplayedViewIndex(currentPage);
//        }
//            ((MuPDFPageView)mDocView.getDisplayedView()).selectText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                    mDocView.setDisplayedViewIndex(data.getIntExtra(getString(R.string.page_no_selected), 0));
                break;
            case PRINT_REQUEST:
                if (resultCode == RESULT_CANCELED)
                    showInfo(getString(R.string.print_failed));
                break;
//            case FILEPICK_REQUEST:
//                if (mFilePicker != null && resultCode == RESULT_OK)
//                    mFilePicker.onPick(data.getData());
//            case PROOF_REQUEST:
//                //  we're returning from a proofing activity
//
//                if (mProofFile != null) {
//                    core.endProof(mProofFile);
//                    mProofFile = null;
//                }
//
//                //  return the top bar to default
//                mTopBarMode = TopBarMode.Main;
//                mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

//    public Object onRetainNonConfigurationInstance()
//    {
//        MuPDFCore mycore = core;
//        core = null;
//        return mycore;
//    }

    private void reflowModeSet(boolean reflow) {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
//        mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
//        setButtonEnabled(mAnnotButton, !reflow);
//        setButtonEnabled(mSearchButton, !reflow);
////        if (reflow) setLinkHighlight(false);
//        setButtonEnabled(mLinkButton, !reflow);
//        setButtonEnabled(mMoreButton, !reflow);
        mDocView.refresh(mReflow);
    }

    private void toggleReflow() {
        reflowModeSet(!mReflow);
        showInfo(mReflow ? getString(R.string.entering_reflow_mode) : getString(R.string.leaving_reflow_mode));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileName != null && mDocView != null) {
            outState.putString(getString(R.string.filename), mFileName);

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt(getString(R.string.page) + mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }

        if (!mButtonsVisible)
            outState.putBoolean(getString(R.string.buttons_hidden), false);
        else
            outState.putBoolean(getString(R.string.buttons_hidden), true);

        if (mReflow)
            outState.putBoolean(getString(R.string.reflow_mode), true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_book_info), Context.MODE_PRIVATE);
        preferences.edit().putString(getString(R.string.preference_last_read_book_path), path).commit();

        addRecent();

        if (mSearchTask != null)
            mSearchTask.stop();

        if (mFileName != null && mDocView != null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt(getString(R.string.page) + mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }
    }

    public void onDestroy() {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
    }

    private void show() {
        showToolbar();
        showBottomSheet();
    }

    private void hide() {
        hideToolbar();
        hideBottomSheet();
    }

    private void hideToolbar() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void hideBottomSheet() {
        bottomSheet.animate().translationY(bottomSheet.getHeight()).setInterpolator(new DecelerateInterpolator(2));
    }

    private void showToolbar() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private void showBottomSheet() {
        bottomSheet.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2));
    }

    private void updatePageNumView(int index) {
        if (core == null)
            return;
        mPageNumberView.setText(String.format("%d / %d", index + 1, core.countPages()));
    }

    private void printDoc() {
        if (!core.fileFormat().startsWith(getString(R.string.start_pdf))) {
            showInfo(getString(R.string.format_currently_not_supported));
            return;
        }

        Intent myIntent = getIntent();
        Uri docUri = myIntent != null ? myIntent.getData() : null;

        if (docUri == null) {
            showInfo(getString(R.string.print_failed));
        }

        if (docUri.getScheme() == null)
            docUri = Uri.parse(getString(R.string.file_content_uri) + docUri.toString());

        Intent printIntent = new Intent(this, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, getString(R.string.pdf_mime_type));
        printIntent.putExtra(getString(R.string.title), mFileName);
        startActivityForResult(printIntent, PRINT_REQUEST);
    }

    private void showInfo(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void OnPrintButtonClick(View v) {
        printDoc();
    }

    //  start a proof activity with the given resolution.
//    public void proofWithResolution(int resolution) {
//        mProofFile = core.startProof(resolution);
//        Uri uri = Uri.parse("file://" + mProofFile);
//        Intent intent = new Intent(this, MuPDFActivity.class);
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setData(uri);
//        // add the current page so it can be found when the activity is running
//        intent.putExtra("startingPage", mDocView.getDisplayedViewIndex());
//        startActivityForResult(intent, PROOF_REQUEST);
//    }

//    public void OnProofButtonClick(final View v) {
//        //  set up the menu or resolutions.
//        final PopupMenu popup = new PopupMenu(this, v);
//        popup.getMenu().add(0, 1, 0, "Select a resolution:");
//        popup.getMenu().add(0, 72, 0, "72");
//        popup.getMenu().add(0, 96, 0, "96");
//        popup.getMenu().add(0, 150, 0, "150");
//        popup.getMenu().add(0, 300, 0, "300");
//        popup.getMenu().add(0, 600, 0, "600");
//        popup.getMenu().add(0, 1200, 0, "1200");
//        popup.getMenu().add(0, 2400, 0, "2400");
//
//        //  prevent the first item from being dismissed.
//        //  is there not a better way to do this?  It requires minimum API 14
//        MenuItem item = popup.getMenu().getItem(0);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//        item.setActionView(new View(v.getContext()));
//        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                return false;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                return false;
//            }
//        });
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                int id = item.getItemId();
//                if (id != 1) {
//                    //  it's a resolution.  The id is also the resolution value
//                    proofWithResolution(id);
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        popup.show();
//    }

//    public void OnSepsButtonClick(final View v) {
//        if (isProofing()) {
//
//            //  get the current page
//            final int currentPage = mDocView.getDisplayedViewIndex();
//
//            //  buid a popup menu based on the given separations
//            final PopupMenu menu = new PopupMenu(this, v);
//
//            //  This makes the popup menu display icons, which by default it does not do.
//            //  I worry that this relies on the internals of PopupMenu, which could change.
//            try {
//                Field[] fields = menu.getClass().getDeclaredFields();
//                for (Field field : fields) {
//                    if ("mPopup".equals(field.getName())) {
//                        field.setAccessible(true);
//                        Object menuPopupHelper = field.get(menu);
//                        Class<?> classPopupHelper = Class.forName(menuPopupHelper
//                                .getClass().getName());
//                        Method setForceIcons = classPopupHelper.getMethod(
//                                "setForceShowIcon", boolean.class);
//                        setForceIcons.invoke(menuPopupHelper, true);
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            //  get the maximum number of seps on any page.
//            //  We use this to dimension an array further down
//            int maxSeps = 0;
//            int numPages = core.countPages();
//            for (int page = 0; page < numPages; page++) {
//                int numSeps = core.getNumSepsOnPage(page);
//                if (numSeps > maxSeps)
//                    maxSeps = numSeps;
//            }
//
//            //  if this is the first time, create the "enabled" array
//            if (mSepEnabled == null) {
//                mSepEnabled = new boolean[numPages][maxSeps];
//                for (int page = 0; page < numPages; page++) {
//                    for (int i = 0; i < maxSeps; i++)
//                        mSepEnabled[page][i] = true;
//                }
//            }
//
//            //  count the seps on this page
//            int numSeps = core.getNumSepsOnPage(currentPage);
//
//            //  for each sep,
//            for (int i = 0; i < numSeps; i++) {
//
////				//  Robin use this to skip separations
////				if (i==12)
////					break;
//
//                //  get the name
//                Separation sep = core.getSep(currentPage, i);
//                String name = sep.name;
//
//                //  make a checkable menu item with that name
//                //  and the separation index as the id
//                MenuItem item = menu.getMenu().add(0, i, 0, name + "    ");
//                item.setCheckable(true);
//
//                //  set an icon that's the right color
//                int iconSize = 48;
//                int alpha = (sep.rgba >> 24) & 0xFF;
//                int red = (sep.rgba >> 16) & 0xFF;
//                int green = (sep.rgba >> 8) & 0xFF;
//                int blue = (sep.rgba >> 0) & 0xFF;
//                int color = (alpha << 24) | (red << 16) | (green << 8) | (blue << 0);
//
//                ShapeDrawable swatch = new ShapeDrawable(new RectShape());
//                swatch.setIntrinsicHeight(iconSize);
//                swatch.setIntrinsicWidth(iconSize);
//                swatch.setBounds(new Rect(0, 0, iconSize, iconSize));
//                swatch.getPaint().setColor(color);
//                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//                item.setIcon(swatch);
//
//                //  check it (or not)
//                item.setChecked(mSepEnabled[currentPage][i]);
//
//                //  establishing a menu item listener
//                item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        //  someone tapped a menu item.  get the ID
//                        int sep = item.getItemId();
//
//                        //  toggle the sep
//                        mSepEnabled[currentPage][sep] = !mSepEnabled[currentPage][sep];
//                        item.setChecked(mSepEnabled[currentPage][sep]);
//                        core.controlSepOnPage(currentPage, sep, !mSepEnabled[currentPage][sep]);
//
//                        //  prevent the menu from being dismissed by these items
//                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//                        item.setActionView(new View(v.getContext()));
//                        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//                            @Override
//                            public boolean onMenuItemActionExpand(MenuItem item) {
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onMenuItemActionCollapse(MenuItem item) {
//                                return false;
//                            }
//                        });
//                        return false;
//                    }
//                });
//
//                //  tell core to enable or disable each sep as appropriate
//                //  but don't refresh the page yet.
//                core.controlSepOnPage(currentPage, i, !mSepEnabled[currentPage][i]);
//            }
//
//            //  add one for done
//            MenuItem itemDone = menu.getMenu().add(0, 0, 0, "Done");
//            itemDone.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    //  refresh the view
//                    mDocView.refresh(false);
//                    return true;
//                }
//            });
//
//            //  show the menu
//            menu.show();
//        }
//
//    }

    private void search(int direction, String searchText) {
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(searchText, direction, displayPage, searchPage);
    }

    @Override
    protected void onStart() {
        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (core != null && core.hasChanges()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE)
                        core.save();

                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.app_name);
            alert.setMessage(getString(R.string.document_has_changes_save_them_));
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
            alert.show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
        Intent intent = new Intent(this, ChoosePDFActivity.class);
        intent.setAction(ChoosePDFActivity.PICK_KEY_FILE);
        startActivityForResult(intent, FILEPICK_REQUEST);
    }

    public class CopyActionModeCallback implements ActionMode.Callback {

        private Intent mShareIntent;
        private String selectedText;

        @Override
        public boolean onCreateActionMode(ActionMode mode, final Menu menu) {
            getMenuInflater().inflate(R.menu.copy, menu);

            mDocView.setTextSelectedListener(new MuPDFReaderView.OnTextSelectedListener() {
                @Override
                public void onTextSelected(float x1, float y1, float x2, float y2) {
                    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
                    mShareIntent = new Intent();
                    mShareIntent.setAction(Intent.ACTION_SEND);
                    mShareIntent.setType(getString(R.string.text_plain));
                    String selection = ((MuPDFView) mDocView.getDisplayedView()).getSelection();
                    mShareIntent.putExtra(Intent.EXTRA_TEXT, selection);
                    mShareActionProvider.setShareIntent(mShareIntent);
                }
            });
            MenuItem item = menu.findItem(R.id.action_share);

            // Get its ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            // Connect the dots: give the ShareActionProvider its Share Intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(mShareIntent);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.accept:
                    MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                    boolean success = false;
                    if (pageView != null)
                        success = pageView.copySelection();
//                    mTopBarMode = TopBarMode.More;
                    showInfo(success ? getString(R.string.copied_to_clipboard) : getString(R.string.no_text_selected));
                    mDocView.setMode(MuPDFReaderView.Mode.Viewing);
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mDocView.setMode(MuPDFReaderView.Mode.Viewing);
        }
    }

}
