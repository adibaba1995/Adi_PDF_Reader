package com.adisoftwares.bookreader.epub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.adisoftwares.bookreader.ImageViewerActivity;
import com.adisoftwares.bookreader.R;

import org.json.JSONException;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.SpineItem;
import org.readium.sdk.android.launcher.Constants;
import org.readium.sdk.android.launcher.ContainerHolder;
import org.readium.sdk.android.launcher.ViewerSettingsDialog;
import org.readium.sdk.android.launcher.model.OpenPageRequest;
import org.readium.sdk.android.launcher.model.Page;
import org.readium.sdk.android.launcher.model.PaginationInfo;
import org.readium.sdk.android.launcher.model.ReadiumJSApi;
import org.readium.sdk.android.launcher.model.ViewerSettings;
import org.readium.sdk.android.launcher.util.EpubServer;
import org.readium.sdk.android.launcher.util.HTMLUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import butterknife.Bind;

/**
 * Created by adityathanekar on 01/02/16.
 */
public class EpubFragment extends Fragment implements ViewerSettingsDialog.OnViewerSettingsChange, View.OnClickListener, View.OnTouchListener {

    private final boolean quiet = false;

    private static final String FILE_NAME = "com.adisoftwares.bookreader.epub.FILE_NAME";

    private static final String TAG = "WebViewActivity";
    private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
    private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";

    // Installs "hook" function so that top-level window (application) can later
    // inject the window.navigator.epubReadingSystem into this HTML document's
    // iframe

    private static final String CLICK_IMAGE_JAVASCRIPT = "document.addEventListener(\"click\", function(event) { var t = event.target; while (t && t !== this) { if (t.matches(\"img\")) { alert(t.src); } t = t.parentNode; } });";

    private static final String INJECT_EPUB_RSO_SCRIPT_1 = ""
            + "window.readium_set_epubReadingSystem = function (obj) {"
            + "\nwindow.navigator.epubReadingSystem = obj;"
            + "\nwindow.readium_set_epubReadingSystem = undefined;"
            + "\nvar el1 = document.getElementById(\"readium_epubReadingSystem_inject1\");"
            + "\nif (el1 && el1.parentNode) { el1.parentNode.removeChild(el1); }"
            + "\nvar el2 = document.getElementById(\"readium_epubReadingSystem_inject2\");"
            + "\nif (el2 && el2.parentNode) { el2.parentNode.removeChild(el2); }"
            + "\n};";

    // Iterate top-level iframes, inject global
    // window.navigator.epubReadingSystem if the expected hook function exists (
    // readium_set_epubReadingSystem() ).
    private static final String INJECT_EPUB_RSO_SCRIPT_2 = ""
            + "var epubRSInject =\nfunction(win) {"
            + "\nvar ret = '';"
            + "\nret += win.location.href;"
            + "\nret += ' ---- ';"
            +
            // "\nret += JSON.stringify(win.navigator.epubReadingSystem);" +
            // "\nret += ' ---- ';" +
            "\nif (win.frames)"
            + "\n{"
            + "\nfor (var i = 0; i < win.frames.length; i++)"
            + "\n{"
            + "\nvar iframe = win.frames[i];"
            + "\nret += ' IFRAME ';"
            + "\nif (iframe.readium_set_epubReadingSystem)"
            + "\n{"
            + "\nret += ' EPBRS ';"
            + "\niframe.readium_set_epubReadingSystem(window.navigator.epubReadingSystem);"
            + "\n}" + "\nret += epubRSInject(iframe);" + "\n}" + "\n}"
            + "\nreturn ret;" + "\n};" + "\nepubRSInject(window);";

    // Script tag to inject the "hook" function installer script, added to the
    // head of every epub iframe document
    private static final String INJECT_HEAD_EPUB_RSO_1 = ""
            + "<script id=\"readium_epubReadingSystem_inject1\" type=\"text/javascript\">\n"
            + "//<![CDATA[\n" + INJECT_EPUB_RSO_SCRIPT_1 + "\n" + "//]]>\n"
            + "</script>";
    // Script tag that generates an HTTP request to a fake script => triggers
    // push of window.navigator.epubReadingSystem into this HTML document's
    // iframe
    private static final String INJECT_HEAD_EPUB_RSO_2 = ""
            + "<script id=\"readium_epubReadingSystem_inject2\" type=\"text/javascript\" "
            + "src=\"/%d/readium_epubReadingSystem_inject.js\"> </script>";
    // Script tag to load the mathjax script payload, added to the head of epub
    // iframe documents, only if <math> tags are detected
    private static final String INJECT_HEAD_MATHJAX = "<script type=\"text/javascript\" src=\"/readium_MathJax.js\"> </script>";

    // Location of payloads in the asset folder
    private static final String PAYLOAD_MATHJAX_ASSET = "reader-payloads/MathJax.js";
    private static final String PAYLOAD_ANNOTATIONS_CSS_ASSET = "reader-payloads/annotations.css";

    float x,y;

    private final EpubServer.DataPreProcessor dataPreProcessor = new EpubServer.DataPreProcessor() {

        @Override
        public byte[] handle(byte[] data, String mime, String uriPath,
                             ManifestItem item) {
            if (mime == null
                    || (mime != "text/html" && mime != "application/xhtml+xml")) {
                return null;
            }

            if (!quiet)
                Log.d(TAG, "PRE-PROCESSED HTML: " + uriPath);

            String htmlText = new String(data, Charset.forName("UTF-8"));

            // String uuid = mPackage.getUrlSafeUniqueID();
            String newHtml = htmlText; // HTMLUtil.htmlByReplacingMediaURLsInHTML(htmlText,
            // cleanedUrl, uuid);
            // //"PackageUUID"

            // Set up the script tags to add to the head
            String tagsToInjectToHead = INJECT_HEAD_EPUB_RSO_1
                    // Slightly change fake script src url with an
                    // increasing count to prevent caching of the
                    // request
                    + String.format(INJECT_HEAD_EPUB_RSO_2,
                    ++mEpubRsoInjectCounter);
            // Checks for the existance of MathML => request
            // MathJax payload
            if (newHtml.contains("<math") || newHtml.contains("<m:math")) {
                tagsToInjectToHead += INJECT_HEAD_MATHJAX;
            }

            newHtml = HTMLUtil.htmlByInjectingIntoHead(newHtml,
                    tagsToInjectToHead);

            // Log.d(TAG, "HTML head inject: " + newHtml);

            return newHtml.getBytes();
        }
    };

    private Container mContainer;
    private Package mPackage;
    private OpenPageRequest mOpenPageRequestData;
    private ViewerSettings mViewerSettings;
    private ReadiumJSApi mReadiumJSApi;
    private EpubServer mServer;

    private boolean mIsMoAvailable;
    private boolean mIsMoPlaying;
    private int mEpubRsoInjectCounter = 0;

    private WebView mWebview;
    private TextView mPageInfo;
    Button mLeftButton;
    Button mRightButton;
    Toolbar toolbar;

    private float x1, x2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.epub_fragment, container, false);

        setHasOptionsMenu(true);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        mWebview = (WebView) rootView.findViewById(R.id.webview);
        mWebview.setOnTouchListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && 0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        /*
        mWebview.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url != null && url.startsWith("http://")) {
					view.getContext().startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				} else {
					return false;
				}
			}
		});*/

        mPageInfo = (TextView) rootView.findViewById(R.id.page_info);
        initWebView();

        openFirstPage();

        Intent intent = getActivity().getIntent();
        Bundle extras = getArguments();
        if (extras != null) {
            mContainer = ContainerHolder.getInstance().get(
                    extras.getLong(Constants.CONTAINER_ID));
            if (mContainer == null) {
                getActivity().finish();
                return null;
            }
            mPackage = mContainer.getDefaultPackage();

            String rootUrl = "http://" + EpubServer.HTTP_HOST + ":"
                    + EpubServer.HTTP_PORT + "/";
            mPackage.setRootUrls(rootUrl, null);

            /*try {
                mOpenPageRequestData = OpenPageRequest.fromJSON(extras
                        .getString(Constants.OPEN_PAGE_REQUEST_DATA));
            } catch (JSONException e) {
                Log.e(TAG,
                        "Constants.OPEN_PAGE_REQUEST_DATA must be a valid JSON object: "
                                + e.getMessage(), e);
            }*/
        }

        // No need, EpubServer already launchers its own thread
        // new AsyncTask<Void, Void, Void>() {
        // @Override
        // protected Void doInBackground(Void... params) {
        // //xxx
        // return null;
        // }
        // }.execute();

        mServer = new EpubServer(EpubServer.HTTP_HOST, EpubServer.HTTP_PORT,
                mPackage, quiet, dataPreProcessor);
        mServer.startServer();

        // Load the page skeleton
        mWebview.loadUrl(READER_SKELETON);
        mViewerSettings = new ViewerSettings(
                ViewerSettings.SyntheticSpreadMode.AUTO,
                ViewerSettings.ScrollMode.AUTO, 100, 20);

        mReadiumJSApi = new ReadiumJSApi(new ReadiumJSApi.JSLoader() {
            @Override
            public void loadJS(String javascript) {
                mWebview.loadUrl(javascript);
            }
        });

        openFirstPage();

        mLeftButton = (Button) rootView.findViewById(R.id.left);
        mLeftButton.setOnClickListener(this);
        mRightButton = (Button) rootView.findViewById(R.id.right);
        mRightButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServer.stop();
        mWebview.loadUrl(READER_SKELETON);
        ((ViewGroup) mWebview.getParent()).removeView(mWebview);
        mWebview.removeAllViews();
        mWebview.clearCache(true);
        mWebview.clearHistory();
        mWebview.destroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebview.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebview.onResume();
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private void initWebView() {
        mWebview.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        mWebview.setWebViewClient(new EpubWebViewClient());
        mWebview.setWebChromeClient(new EpubWebChromeClient());

        mWebview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
        mWebview.addJavascriptInterface(new JsInterface(getActivity()), "imageClick");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Aditya", "Menu item selected");
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.add_bookmark:
                if (!quiet)
                    Log.d(TAG, "Add a bookmark");
                mReadiumJSApi.bookmarkCurrentPage();
                return true;
            case R.id.settings:
                if (!quiet)
                    Log.d("Aditya", "Show settings");
                showSettings();
                return true;
            case R.id.mo_previous:
                mReadiumJSApi.previousMediaOverlay();
                return true;
            case R.id.mo_play:
                mReadiumJSApi.toggleMediaOverlay();
                return true;
            case R.id.mo_pause:
                mReadiumJSApi.toggleMediaOverlay();
                return true;
            case R.id.mo_next:
                mReadiumJSApi.nextMediaOverlay();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.left) {
            mReadiumJSApi.openPageLeft();
        } else if (v.getId() == R.id.right) {
            mReadiumJSApi.openPageRight();
        }
    }

    private void showSettings() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        DialogFragment dialog = new ViewerSettingsDialog(this, mViewerSettings);
        dialog.show(fm, "dialog");
        fragmentTransaction.commit();
    }

    @Override
    public void onViewerSettingsChange(ViewerSettings viewerSettings) {
        updateSettings(viewerSettings);
    }

    private void updateSettings(ViewerSettings viewerSettings) {
        mViewerSettings = viewerSettings;
        mReadiumJSApi.updateSettings(viewerSettings);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.epub_menu, menu);

        MenuItem mo_previous = menu.findItem(R.id.mo_previous);
        MenuItem mo_next = menu.findItem(R.id.mo_next);
        MenuItem mo_play = menu.findItem(R.id.mo_play);
        MenuItem mo_pause = menu.findItem(R.id.mo_pause);

        // show menu only when its reasonable

        mo_previous.setVisible(mIsMoAvailable);
        mo_next.setVisible(mIsMoAvailable);

        if (mIsMoAvailable) {
            mo_play.setVisible(!mIsMoPlaying);
            mo_pause.setVisible(mIsMoPlaying);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (deltaX < 0) {
                    //Right to left swipe
                    mReadiumJSApi.openPageRight();
                }else if(deltaX >0){
                    //Left to right swipe
                    mReadiumJSApi.openPageLeft();
                }
                break;
        }

        //In response to the picture on the web click event by wenview touch
        float density = getResources().getDisplayMetrics().density; //Screen density
        float touchX = event.getX() / density;  //Must be divided by the density of the screen
        float touchY = event.getY() / density;
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x = touchX;
            y = touchY;
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            float dx = Math.abs(touchX-x);
            float dy = Math.abs(touchY-y);
            if(dx<10.0/density&&dy<10.0/density){
                clickImage(touchX,touchY);
            }
        }
        return false;
    }

    private void clickImage(float touchX, float touchY) {
        //Through the touch position to get a picture of URL
        String js = "javascript:(function(){" +
                "var temp = document.elementFromPoint("+touchX+","+touchY+");"
                +"var doc = temp.contentDocument? temp.contentDocument: temp.contentWindow.document;"
                +"var  obj=doc.elementFromPoint("+touchX+","+touchY+");"
                +"if(obj != null){"+ " window.imageClick.click(obj.src);}" +
                "})()";

//        String js = "javascript:(function(){" +
//                "var temp = document.elementFromPoint("+touchX+", "+touchY+");"
//                +"if(temp.tagName.toLowerCase() === \"img\") {"
//                +"alert(temp.src); }"
//                +"})()";
        mWebview.loadUrl(js);
    }

    public final class EpubWebViewClient extends WebViewClient {

        private static final String HTTP = "http";
        private static final String UTF_8 = "utf-8";

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!quiet)
                Log.d(TAG, "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!quiet)
                Log.d(TAG, "onPageFinished: " + url);
            view.loadUrl("JavaScript:" + CLICK_IMAGE_JAVASCRIPT);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (!quiet)
                Log.d(TAG, "onLoadResource: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            if (!quiet)
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            view.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                public void performClick() throws Exception
                {
                    Log.d("LOGIN::", "Clicked");
                    Toast.makeText(getActivity(), "Login clicked", Toast.LENGTH_LONG).show();
                }
            }, "image");
            return false;
        }

        private void evaluateJavascript(final String script) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!quiet)
                        Log.d(TAG, "WebView evaluateJavascript: " + script + "");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                        if (!quiet)
                            Log.d(TAG, "WebView evaluateJavascript KitKat+ API");

                        mWebview.evaluateJavascript(script,
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String str) {
                                        if (!quiet)
                                            Log.d(TAG,
                                                    "WebView evaluateJavascript RETURN: "
                                                            + str);
                                    }
                                });
                    } else {

                        if (!quiet)
                            Log.d(TAG, "WebView loadUrl() API");

                        mWebview.loadUrl("javascript:var exec = function(){\n"
                                + script + "\n}; exec();");
                    }
                }
            });
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            if (!quiet)
                Log.d(TAG, "-------- shouldInterceptRequest: " + url);

            if (url != null && url != "undefined") {

                String localHttpUrlPrefix = "http://" + EpubServer.HTTP_HOST
                        + ":" + EpubServer.HTTP_PORT;
                boolean isLocalHttp = url.startsWith(localHttpUrlPrefix);

                // Uri uri = Uri.parse(url);
                // uri.getScheme()

                if (url.startsWith("http") && !isLocalHttp) {
                    if (!quiet)
                        Log.d(TAG, "HTTP (NOT LOCAL): " + url);
                    return super.shouldInterceptRequest(view, url);
                }

                String cleanedUrl = cleanResourceUrl(url, false);
                if (!quiet)
                    Log.d(TAG, url + " => " + cleanedUrl);

                if (cleanedUrl
                        .matches("\\/?\\d*\\/readium_epubReadingSystem_inject.js")) {
                    if (!quiet)
                        Log.d(TAG, "navigator.epubReadingSystem inject ...");

                    // Fake script requested, this is immediately invoked after
                    // epubReadingSystem hook is in place,
                    // => execute js on the reader.html context to push the
                    // global window.navigator.epubReadingSystem into the
                    // iframe(s)

                    evaluateJavascript(INJECT_EPUB_RSO_SCRIPT_2);

                    return new WebResourceResponse("text/javascript", UTF_8,
                            new ByteArrayInputStream(
                                    "(function(){})()".getBytes()));
                }

                if (cleanedUrl.matches("\\/?readium_MathJax.js")) {
                    if (!quiet)
                        Log.d(TAG, "MathJax.js inject ...");

                    InputStream is = null;
                    try {
                        is = getActivity().getAssets().open(PAYLOAD_MATHJAX_ASSET);
                    } catch (IOException e) {

                        Log.e(TAG, "MathJax.js asset fail!");

                        return new WebResourceResponse(null, UTF_8,
                                new ByteArrayInputStream("".getBytes()));
                    }

                    return new WebResourceResponse("text/javascript", UTF_8, is);
                }

                if (cleanedUrl.matches("\\/?readium_Annotations.css")) {
                    if (!quiet)
                        Log.d(TAG, "annotations.css inject ...");

                    InputStream is = null;
                    try {
                        is = getActivity().getAssets().open(PAYLOAD_ANNOTATIONS_CSS_ASSET);
                    } catch (IOException e) {

                        Log.e(TAG, "annotations.css asset fail!");

                        return new WebResourceResponse(null, UTF_8,
                                new ByteArrayInputStream("".getBytes()));
                    }

                    return new WebResourceResponse("text/css", UTF_8, is);
                }

                String mime = null;
                int dot = cleanedUrl.lastIndexOf('.');
                if (dot >= 0) {
                    mime = EpubServer.MIME_TYPES.get(cleanedUrl.substring(
                            dot + 1).toLowerCase());
                }
                if (mime == null) {
                    mime = "application/octet-stream";
                }

                ManifestItem item = mPackage.getManifestItem(cleanedUrl);
                String contentType = item != null ? item.getMediaType() : null;
                if (mime != "application/xhtml+xml"
                        && mime != "application/xml" // FORCE
                        && contentType != null && contentType.length() > 0) {
                    mime = contentType;
                }

                if (url.startsWith("file:")) {
                    if (item == null) {
                        Log.e(TAG, "NO MANIFEST ITEM ... " + url);
                        return super.shouldInterceptRequest(view, url);
                    }

                    String cleanedUrlWithQueryFragment = cleanResourceUrl(url,
                            true);
                    String httpUrl = "http://" + EpubServer.HTTP_HOST + ":"
                            + EpubServer.HTTP_PORT + "/"
                            + cleanedUrlWithQueryFragment;
                    Log.e(TAG, "FILE to HTTP REDIRECT: " + httpUrl);

                    try {
                        URLConnection c = new URL(httpUrl).openConnection();
                        ((HttpURLConnection) c).setUseCaches(false);
                        if (mime == "application/xhtml+xml"
                                || mime == "text/html") {
                            ((HttpURLConnection) c).setRequestProperty(
                                    "Accept-Ranges", "none");
                        }
                        InputStream is = c.getInputStream();
                        return new WebResourceResponse(mime, null, is);
                    } catch (Exception ex) {
                        Log.e(TAG,
                                "FAIL: " + httpUrl + " -- " + ex.getMessage(),
                                ex);
                    }
                }
                if (!quiet)
                    Log.d(TAG, "RESOURCE FETCH ... " + url);
                return super.shouldInterceptRequest(view, url);
            }

            Log.e(TAG, "NULL URL RESPONSE: " + url);
            return new WebResourceResponse(null, UTF_8,
                    new ByteArrayInputStream("".getBytes()));
        }
    }

    private String cleanResourceUrl(String url, boolean preserveQueryFragment) {

        String cleanUrl = null;

        String httpUrl = "http://" + EpubServer.HTTP_HOST + ":"
                + EpubServer.HTTP_PORT;
        if (url.startsWith(httpUrl)) {
            cleanUrl = url.replaceFirst(httpUrl, "");
        } else {
            cleanUrl = (url.startsWith(ASSET_PREFIX)) ? url.replaceFirst(
                    ASSET_PREFIX, "") : url.replaceFirst("file://", "");
        }

        String basePath = mPackage.getBasePath();
        if (basePath.charAt(0) != '/') {
            basePath = '/' + basePath;
        }
        if (cleanUrl.charAt(0) != '/') {
            cleanUrl = '/' + cleanUrl;
        }
        cleanUrl = (cleanUrl.startsWith(basePath)) ? cleanUrl.replaceFirst(
                basePath, "") : cleanUrl;

        if (cleanUrl.charAt(0) == '/') {
            cleanUrl = cleanUrl.substring(1);
        }

        if (!preserveQueryFragment) {
            int indexOfQ = cleanUrl.indexOf('?');
            if (indexOfQ >= 0) {
                cleanUrl = cleanUrl.substring(0, indexOfQ);
            }

            int indexOfSharp = cleanUrl.indexOf('#');
            if (indexOfSharp >= 0) {
                cleanUrl = cleanUrl.substring(0, indexOfSharp);
            }
        }

        return cleanUrl;
    }

    public class EpubWebChromeClient extends WebChromeClient implements
            MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (!quiet)
                Log.d(TAG, "here in on ShowCustomView: " + view);
            super.onShowCustomView(view, callback);
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                if (!quiet)
                    Log.d(TAG,
                            "frame.getFocusedChild(): "
                                    + frame.getFocusedChild());
                if (frame.getFocusedChild() instanceof VideoView) {
                    VideoView video = (VideoView) frame.getFocusedChild();
                    // frame.removeView(video);
                    // a.setContentView(video);
                    video.setOnCompletionListener(this);
                    video.setOnErrorListener(this);
                    video.start();
                }
            }
        }

        public void onCompletion(MediaPlayer mp) {
            if (!quiet)
                Log.d(TAG, "Video completed");

            // a.setContentView(R.layout.main);
            // WebView wb = (WebView) a.findViewById(R.id.webview);
            // a.initWebView();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            Log.e(TAG, "MediaPlayer onError: " + what + ", " + extra);
            return false;
        }
    }

    private void openFirstPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mReadiumJSApi.open
                if (mPackage != null) {
                    SpineItem spineItem = mPackage.getSpineItems().get(0);
                    mOpenPageRequestData = OpenPageRequest.fromIdref(spineItem.getIdRef());
                }
            }
        });
    }

    public static Fragment newInstance(String fileName, long containerId) {
        Fragment fragment = new EpubFragment();
        Bundle args = new Bundle();
        args.putString(FILE_NAME, fileName);
        args.putLong(Constants.CONTAINER_ID, containerId);
        fragment.setArguments(args);
        return fragment;
    }

    public class EpubInterface {

        @JavascriptInterface
        public void onPaginationChanged(String currentPagesInfo) {
            if (!quiet)
                Log.d(TAG, "onPaginationChanged: " + currentPagesInfo);
            try {
                PaginationInfo paginationInfo = PaginationInfo
                        .fromJson(currentPagesInfo);
                List<Page> openPages = paginationInfo.getOpenPages();
                if (!openPages.isEmpty()) {
                    final Page page = openPages.get(0);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mPageInfo.setText(getString(R.string.page_x_of_y,
                                    page.getSpineItemPageIndex() + 1,
                                    page.getSpineItemPageCount()));
                            SpineItem spineItem = mPackage.getSpineItem(page
                                    .getIdref());
                            boolean isFixedLayout = spineItem
                                    .isFixedLayout(mPackage);
                            mWebview.getSettings().setBuiltInZoomControls(
                                    isFixedLayout);
                            mWebview.getSettings()
                                    .setDisplayZoomControls(false);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }

        @JavascriptInterface
        public void onSettingsApplied() {
            if (!quiet)
                Log.d(TAG, "onSettingsApplied");
        }

        @JavascriptInterface
        public void onReaderInitialized() {
            if (!quiet)
                Log.d(TAG, "onReaderInitialized");

            if (!quiet)
                Log.d(TAG, "openPageRequestData: " + mOpenPageRequestData);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mReadiumJSApi.openBook(mPackage, mViewerSettings,
                            mOpenPageRequestData);
                }
            });

        }

        @JavascriptInterface
        public void onContentLoaded() {
            if (!quiet)
                Log.d(TAG, "onContentLoaded");
        }

        @JavascriptInterface
        public void onPageLoaded() {
            if (!quiet)
                Log.d(TAG, "onPageLoaded");
        }

        @JavascriptInterface
        public void onIsMediaOverlayAvailable(String available) {
            if (!quiet)
                Log.d(TAG, "onIsMediaOverlayAvailable:" + available);
            mIsMoAvailable = available.equals("true");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().invalidateOptionsMenu();
                }
            });
        }

        @JavascriptInterface
        public void onMediaOverlayStatusChanged(String status) {
            if (!quiet)
                Log.d(TAG, "onMediaOverlayStatusChanged:" + status);
            // this should be real json parsing if there will be more data that
            // needs to be extracted

            if (status.indexOf("isPlaying") > -1) {
                mIsMoPlaying = status.indexOf("\"isPlaying\":true") > -1;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().invalidateOptionsMenu();
                }
            });
        }

    }

    class JsInterface {
        Context context;
        public JsInterface(Context context){
            this.context = context;
        }

        @JavascriptInterface
        public void click(String url){
            Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
            intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, url);
            startActivity(intent);
            Toast.makeText(getActivity(), url, Toast.LENGTH_SHORT).show();
        }
    }

}
