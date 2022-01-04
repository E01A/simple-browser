package android.app.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.util.Base64;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    String ua = "Mozilla/5.0 (Android 11; Mobile; rv:83.0) Gecko/83.0 Firefox/83.0";
    private WebView webView;
    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private myWebChromeClient mWebChromeClient;
    ImageView imgview;
    EditText edit;
    ImageView img1,img2,img3;
    private static boolean viewAdded;
    boolean loadingFinished = true;
    boolean redirect = false;
    protected final List<String> mPermittedHostnames = new LinkedList<String>();
    WebSettings settings;
    private long pressedTime;
   
    private ProgressDialog progressBar;

 @SuppressLint("RtlHardcoded")
 @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

     viewAdded = true;

    webView = (WebView) findViewById(R.id.webView);
    webView.setBackgroundColor(Color.TRANSPARENT);

     customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
     imgview = (ImageView) findViewById(R.id.imgview);
     mWebChromeClient = new myWebChromeClient();
     webView.setWebChromeClient(mWebChromeClient);

     img1 = (ImageView)findViewById(R.id.img1);
     img2 = (ImageView)findViewById(R.id.img2);
     img3 = (ImageView)findViewById(R.id.img3);

     edit = (EditText)findViewById(R.id.edit);

     img1.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (isNetworkAvailable()) {
                 webView.setVisibility(View.VISIBLE);
                 if (webView.canGoBack()) {
                     webView.goBack();
                 } else  {

                   Intent set = new Intent(MainActivity.this, MainActivity.class);
                   finish();
                   startActivity(set);

                 }
             } else {
                 DialogNet();
             }
         }
     });
     img2.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (isNetworkAvailable()) {
                 webView.setVisibility(View.VISIBLE);
                 if (webView.canGoForward()) {
                     webView.goForward();
                 } else  {
                     Intent set = new Intent(MainActivity.this, MainActivity.class);
                     finish();
                     startActivity(set);
                 }
             } else {
                 DialogNet();
             }
         }
     });
     img3.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (isNetworkAvailable()) {
                 webView.setVisibility(View.VISIBLE);
                 String url = edit.getText().toString();
                 if(!url.isEmpty()) {
                     if (URLUtil.isValidUrl(url)) {
                         webView.loadUrl(url);
                     }
                 } else {
                  Intent set = new Intent(MainActivity.this, MainActivity.class);
                 finish();
                 startActivity(set);

                 }

             } else {
                 DialogNet();
             }
         }
     });

     edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 if (isNetworkAvailable()) {
                     if (!edit.getText().toString().isEmpty()) {
                         String newAdder = edit.getText().toString();
                         webView.setVisibility(View.VISIBLE);
                         if(newAdder.startsWith("192.168.")|newAdder.equals("127.0.")|newAdder.equals("file:///")) {
                             initWebView(webView);
                             registerForContextMenu(webView);
                             webView.loadUrl(newAdder);
                         }

                         else{
                             initWebView(webView);
                             registerForContextMenu(webView);
                             webView.loadUrl("https://www.google.com/search?q=" + newAdder);
                         }

                     } else {
                         Toast.makeText(getApplicationContext(), "The address is not valid ", Toast.LENGTH_LONG).show();
                     }
                 } else {
                     DialogNet();
                 }
                 return true;
             }
             return false;
         }
     });


     ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
     if (intentReader.isShareIntent()) {
         String adder = Objects.requireNonNull(intentReader.getText()).toString();
       if( URLUtil.isValidUrl(adder)) {
             webView.setVisibility(View.VISIBLE);
             initWebView(webView);
             registerForContextMenu(webView);
             webView.loadUrl(adder);
             edit.setText(adder);
         }


     }

      Uri intentUri = getIntent().getData();
      if (intentUri != null) {
          String adder = Objects.requireNonNull(intentUri).toString();
          if( URLUtil.isValidUrl(adder)) {
              webView.setVisibility(View.VISIBLE);
              initWebView(webView);
              registerForContextMenu(webView);
              webView.loadUrl(adder);
              edit.setText(adder);
          }
      }



  }

   @SuppressLint("SetTextI18n")
   public void DialogNet () {
     AlertDialog.Builder Dlg = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_TRADITIONAL);
     TextView title = new TextView(MainActivity.this);
     title.setText("Attention!!!");
     title.setBackgroundColor(Color.RED);
     title.setTextColor(Color.WHITE);
     title.setPadding(10, 10, 10, 10);
     title.setGravity(Gravity.CENTER); // this is required to bring it to center.
     title.setTextSize(20);
     Dlg.setCustomTitle(title);
     Dlg.setMessage("Check Internet connection");
     Dlg.setCancelable(true);

     Dlg.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
       @Override
       public void onClick (DialogInterface arg0,int arg1){

         Dlg.setCancelable(true);

       }
     });

     Dlg.setNegativeButton("Again", new DialogInterface.OnClickListener() {
       @Override
       public void onClick (DialogInterface arg0,int arg1){

         Intent set = new Intent(MainActivity.this, MainActivity.class);
         finish();
         startActivity(set);

       }

     });

     Dlg.show();

  }

  public boolean isNetworkAvailable() {
    ConnectivityManager cn = (ConnectivityManager)  getSystemService(Context.CONNECTIVITY_SERVICE);
    android.net.NetworkInfo wifi = cn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    android.net.NetworkInfo data = cn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    return wifi != null && wifi.isConnectedOrConnecting() || data != null && data.isConnectedOrConnecting();
  }



  @Override
  public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
    super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

    final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();

    if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
            webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.setHeaderTitle("");

            contextMenu.add(0, 1, 0, "Save Image")

              .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                     String link = webViewHitTestResult.getExtra();

                    if( URLUtil.isValidUrl(link)) {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);
                        Toast.makeText(MainActivity.this,"Image Downloaded Successfully.",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Sorry.. Something Went Wrong.",Toast.LENGTH_LONG).show();
                    } return false;
                }
              });

      contextMenu.add(0, 2, 0, "View Image")
              .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                  String link = webViewHitTestResult.getExtra();
                     webView.loadUrl(link);
                     return false;
                }

              });

      contextMenu.add(0, 3, 0, "Share Image")
              .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                  String url = webViewHitTestResult.getExtra();
                  Intent shareIntent = new Intent(Intent.ACTION_SEND);
                  shareIntent.setType("text/plain");
                  shareIntent.putExtra(Intent.EXTRA_TEXT, url); //
                  startActivity(Intent.createChooser(shareIntent, "Share..."));
                  return false;
                }
         });


    } else if (webViewHitTestResult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {

        contextMenu.add(0, 3, 0, "Share Link")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        String url = webViewHitTestResult.getExtra();
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, url); //
                        startActivity(Intent.createChooser(shareIntent, "Share..."));

                        return false;
               }
         });
    }
  }



  @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
  private void initWebView(WebView webView) {
      viewAdded = false;
      imgview.setVisibility(View.GONE);
      
      progressBar = ProgressDialog.show(MainActivity.this,null,"   Please Wait ...");
      settings = webView.getSettings();
      settings.setUserAgentString(ua);
      settings.setJavaScriptEnabled(true);
      webView.getSettings().setLoadsImagesAutomatically(true);
      webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
      webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
      settings.setLoadWithOverviewMode(true);
      settings.setGeolocationEnabled(true);
      setMixedContentAllowed(true);
      setCookiesEnabled(true);
      setDesktopMode(false);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          settings.setSafeBrowsingEnabled(true);
      }
      WebView.setWebContentsDebuggingEnabled(true);
      webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
      settings.setAppCacheEnabled(true);
      setThirdPartyCookiesEnabled(true);
      settings.setLoadWithOverviewMode(true);
      settings.setDefaultTextEncodingName("utf-8");
      settings.setDatabaseEnabled(true);
      settings.setSaveFormData(true);
      settings.setDomStorageEnabled(true);
      settings.setAllowFileAccess(true);
      settings.setAllowFileAccessFromFileURLs(true);
      settings.setAllowUniversalAccessFromFileURLs(true);
      settings.setAllowContentAccess(true);
      settings.setMediaPlaybackRequiresUserGesture(true);
      settings.setSupportZoom(true);
      settings.setBuiltInZoomControls(true);
      settings.setDisplayZoomControls(false);
      settings.setJavaScriptCanOpenWindowsAutomatically(true);
      settings.setSupportMultipleWindows(false);
      settings.setAllowFileAccess(true);
      webView.setClickable(true);
      webView.setLongClickable(true);
      settings.setUseWideViewPort(true);
      webView.setHapticFeedbackEnabled(true);

      methodInvoke(settings, "setPluginsEnabled", new Class[]{boolean.class}, new Object[]{true});
      methodInvoke(settings, "setPluginState", new Class[]{WebSettings.PluginState.class}, new Object[]{WebSettings.PluginState.ON});
      methodInvoke(settings, "setPluginsEnabled", new Class[]{boolean.class}, new Object[]{true});
      methodInvoke(settings, "setAllowUniversalAccessFromFileURLs", new Class[]{boolean.class}, new Object[]{true});
      methodInvoke(settings, "setAllowFileAccessFromFileURLs", new Class[]{boolean.class}, new Object[]{true});

      webView.setWebViewClient(new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

                 if (url.startsWith("https://play.google.com/store/apps/details?id=")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("https://www.youtube.com/") || url.startsWith("youtube.com/")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("https://www.instagram.com/")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("https://t.me/")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("https://twitter.com/")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("https://wa.me/") || url.startsWith("https://api.whatsapp.com/") || url.startsWith("http://wa.me/")) {
                     final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("tel") || url.startsWith("tel:")) {
                     Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

                 if (url.startsWith("sms") || (url.startsWith("smsto"))) {
                     Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(intent);

                 }

            view.loadUrl(url);
            edit.setText(url);
            return true;
        }

      @Override
      public void onPageFinished(WebView view, String url) {
        if (!redirect) {
            loadingFinished = true;
            if (progressBar.isShowing()){
                progressBar.dismiss();
            }
        }
          super.onPageFinished(view, url);
    }

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LinearLayout layout       = new LinearLayout(MainActivity.this);
            TextView tvu        = new TextView(MainActivity.this);
            TextView tvp        = new TextView(MainActivity.this);

            final EditText username    = new EditText(MainActivity.this);
            final EditText password    = new EditText(MainActivity.this);

            tvu.setText("Username");
            tvu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            username.setSingleLine();
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(tvu);
            layout.addView(username);
            layout.setPadding(50, 40, 50, 10);


            tvp.setText("Password");
            tvp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            password.setSingleLine();
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(tvp);
            layout.addView(password);
            layout.setPadding(50, 40, 50, 10);


            builder.setView(layout);
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.cancel();
                loadingFinished = true;
                if (progressBar.isShowing()){
                    progressBar.dismiss();
                }
                handler.cancel();

            });
            builder.setPositiveButton("Done", (dialog, which) -> {
               String user = username.getText().toString();
               String pass = password.getText().toString();
                handler.proceed(user,pass);

            });
            builder.create().show();

        }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
          Toast.makeText(MainActivity.this,"Error "+ description,Toast.LENGTH_SHORT).show();
          

          AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
          alertDialog.setTitle("ERROR");
          alertDialog.setMessage(description);
          alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
          });
          alertDialog.show();





      }


      public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed() ;
      }

    });

      webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
          AlertDialog.Builder Dlg = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_TRADITIONAL);
          TextView title = new TextView(MainActivity.this);
          title.setText("Attention!!!");
          title.setBackgroundColor(Color.RED);
          title.setTextColor(Color.WHITE);
          title.setPadding(10, 10, 10, 10);
          title.setGravity(Gravity.CENTER); // this is required to bring it to center.
          title.setTextSize(20);
          Dlg.setCustomTitle(title);
          Dlg.setMessage("Is the download file allowed?");
          Dlg.setCancelable(true);

          Dlg.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface arg0,int arg1){

                  Dlg.setCancelable(true);

                  Intent shareIntent = new Intent(Intent.ACTION_SEND);
                  shareIntent.setType("text/plain");
                  shareIntent.putExtra(Intent.EXTRA_TEXT, url); //
                  startActivity(Intent.createChooser(shareIntent, "Share..."));
              }
          });

          Dlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface arg0,int arg1){

                  Dlg.setCancelable(true);


              }

          });

          Dlg.show();
   });

  }


  public void setDesktopMode(final boolean enabled) {
        final WebSettings webSettings = webView.getSettings();

        final String newUserAgent;
        if (enabled) {
            newUserAgent = webSettings.getUserAgentString().replace("Mobile", "eliboM").replace("Android", "diordnA");
        }
        else {
            newUserAgent = webSettings.getUserAgentString().replace("eliboM", "Mobile").replace("diordnA", "Android");
        }

        webSettings.setUserAgentString(newUserAgent);
        webSettings.setUseWideViewPort(enabled);
        webSettings.setLoadWithOverviewMode(enabled);
        webSettings.setSupportZoom(enabled);
        webSettings.setBuiltInZoomControls(enabled);
    }


    private static void methodInvoke(Object obj, String method, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method m = obj.getClass().getMethod(method, boolean.class);
            m.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


  public boolean isPermittedUrl(final String url) {
    // if the permitted hostnames have not been restricted to a specific set
    if (mPermittedHostnames.size() == 0) {
      // all hostnames are allowed
      return true;
    }

    final Uri parsedUrl = Uri.parse(url);

    // get the hostname of the URL that is to be checked
    final String actualHost = parsedUrl.getHost();

    // if the hostname could not be determined, usually because the URL has been invalid
    if (actualHost == null) {
      return false;
    }

    // if the host contains invalid characters (e.g. a backslash)
    if (!actualHost.matches("^[a-zA-Z0-9._!~*')(;:&=+$,%\\[\\]-]*$")) {
      // prevent mismatches between interpretations by `Uri` and `WebView`, e.g. for `http://evil.example.com\.good.example.com/`
      return false;
    }

    // get the user information from the authority part of the URL that is to be checked
    final String actualUserInformation = parsedUrl.getUserInfo();

    // if the user information contains invalid characters (e.g. a backslash)
    if (actualUserInformation != null && !actualUserInformation.matches("^[a-zA-Z0-9._!~*')(;:&=+$,%-]*$")) {
      // prevent mismatches between interpretations by `Uri` and `WebView`, e.g. for `http://evil.example.com\@good.example.com/`
      return false;
    }

    // for every hostname in the set of permitted hosts
    for (String expectedHost : mPermittedHostnames) {
      // if the two hostnames match or if the actual host is a subdomain of the expected host
      if (actualHost.equals(expectedHost) || actualHost.endsWith("." + expectedHost)) {
        // the actual hostname of the URL to be checked is allowed
        return true;
      }
    }

    // the actual hostname of the URL to be checked is not allowed since there were no matches
    return false;
  }

  /**
   * @deprecated use `isPermittedUrl` instead
   */
  protected boolean isHostnameAllowed(final String url) {
    return isPermittedUrl(url);
  }

  @SuppressWarnings("static-method")
  public void setCookiesEnabled(final boolean enabled) {
    CookieManager.getInstance().setAcceptCookie(enabled);
  }

  @SuppressLint("NewApi")
  public void setThirdPartyCookiesEnabled(final boolean enabled) {
      CookieManager.getInstance().setAcceptThirdPartyCookies(webView, enabled);
  }

  public void setMixedContentAllowed(final boolean allowed) {
    setMixedContentAllowed(webView.getSettings(), allowed);
  }

  @SuppressWarnings("static-method")
  @SuppressLint("NewApi")
  protected void setMixedContentAllowed(final WebSettings webSettings, final boolean allowed) {
      webSettings.setMixedContentMode(allowed ? WebSettings.MIXED_CONTENT_ALWAYS_ALLOW : WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  }

  UploadHandler mUploadHandler;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

    if (requestCode == Controller.FILE_SELECTED) {
      // Chose a file from the file picker.
      if (mUploadHandler != null) {
        mUploadHandler.onResult(resultCode, intent);
      }
    }

    super.onActivityResult(requestCode, resultCode, intent);
  }


    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (inCustomView()) {
            hideCustomView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (inCustomView()) {
                hideCustomView();
                return true;
            }

            if ((mCustomView == null) && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    class myWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;


        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            WebView newWebView = new WebView(MainActivity.this);
            newWebView.getSettings().setJavaScriptEnabled(true);
            newWebView.getSettings().setSupportZoom(true);
            newWebView.getSettings().setDisplayZoomControls(false);
            newWebView.getSettings().setBuiltInZoomControls(true);
            newWebView.getSettings().setSupportMultipleWindows(true);
            newWebView.getSettings().setSaveFormData(true);
            view.addView(newWebView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();


            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

            });

            newWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    view.removeView(newWebView);

                }
            });

            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
        private boolean isSystemLocationEnable() {
            final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            final boolean gpsLocationEnable = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            final boolean networkLocationEnable = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return gpsLocationEnable && networkLocationEnable;
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view,CustomViewCallback callback) {

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
            int currentOrientation = getResources().getConfiguration().orientation;
              if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        }

        @Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.view_loading_video, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCustomView == null)
                return;

            webView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            mCustomView = null;
        }

        private String getTitleFromUrl(String url) {
            try {
                URL urlObj = new URL(url);
                String host = urlObj.getHost();
                if (host != null && !host.isEmpty()) {
                    return urlObj.getProtocol() + "://" + host;
                }
                if (url.startsWith("file:")) {
                    String fileName = urlObj.getFile();
                    if (fileName != null && !fileName.isEmpty()) {
                        return fileName;
                    }
                }
            } catch (Exception e) {
                // ignore
            }

            return url;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            String newTitle = getTitleFromUrl(url);

            new AlertDialog.Builder(MainActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setCancelable(false).create().show();
            return true;
            // return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {

            String newTitle = getTitleFromUrl(url);

            new AlertDialog.Builder(MainActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).setCancelable(false).create().show();
            return true;

            // return super.onJsConfirm(view, url, message, result);
        }

        // Android 2.x
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // Android 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, "", "filesystem");
        }

        // Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadHandler = new MainActivity.UploadHandler(new MainActivity.Controller());
            mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
        }

        // Android 5.0.1
        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {

            String[] acceptTypes = fileChooserParams.getAcceptTypes();

            StringBuilder acceptType = new StringBuilder();
            for (String type : acceptTypes) {
                if (type != null && type.length() != 0)
                    acceptType.append(type).append(";");
            }
            if (acceptType.length() == 0)
                acceptType = new StringBuilder("*/*");

            final ValueCallback<Uri[]> finalFilePathCallback = filePathCallback;

            ValueCallback<Uri> vc = new ValueCallback<Uri>() {

                @Override
                public void onReceiveValue(Uri value) {

                    Uri[] result;
                    if (value != null)
                        result = new Uri[]{value};
                    else
                        result = null;

                    finalFilePathCallback.onReceiveValue(result);

                }
            };

            openFileChooser(vc, acceptType.toString(), "filesystem");


            return true;
        }


    }

  class Controller {
    final static int FILE_SELECTED = 4;

    Activity getActivity() {
      return MainActivity.this;
    }
  }

    static class UploadHandler {
        private ValueCallback<Uri> mUploadMessage;
        private String mCameraFilePath;
        private boolean mHandled;
        private boolean mCaughtActivityNotFoundException;
        private final MainActivity.Controller mController;
        public UploadHandler(MainActivity.Controller controller) {
            mController = controller;
        }
        String getFilePath() {
            return mCameraFilePath;
        }
        boolean handled() {
            return mHandled;
        }
        void onResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
                // Couldn't resolve an activity, we are going to try again so skip
                // this result.
                mCaughtActivityNotFoundException = false;
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            if (result == null && intent == null && resultCode == Activity.RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    mController.getActivity().sendBroadcast(
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            mUploadMessage.onReceiveValue(result);
            mHandled = true;
            mCaughtActivityNotFoundException = false;
        }
        void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            final String imageMimeType = "image/*";
            final String videoMimeType = "video/*";
            final String audioMimeType = "audio/*";
            final String mediaSourceKey = "capture";
      final String mediaSourceValueCamera = "camera";
      final String mediaSourceValueFileSystem = "filesystem";
      final String mediaSourceValueCamcorder = "camcorder";
      final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = uploadMsg;
            // Parse the accept type.
            String[] params = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            switch (mimeType) {
                case imageMimeType:
                    if (mediaSource.equals(mediaSourceValueCamera)) {
                        // Specified 'image/*' and requested the camera, so go ahead and launch the
                        // camera directly.
                        startActivity(createCameraIntent());
                    } else {
                        // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                        // In all these cases we show a traditional picker filetered on accept type
                        // so launch an intent for both the Camera and image/* OPENABLE.
                        Intent chooser = createChooserIntent(createCameraIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                        startActivity(chooser);
                    }
                    return;
                case videoMimeType:
                    if (mediaSource.equals(mediaSourceValueCamcorder)) {
                        // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                        // camcorder directly.
                        startActivity(createCamcorderIntent());
                    } else {
                        // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                        // In all these cases we show an intent for the traditional file picker, filtered
                        // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                        Intent chooser = createChooserIntent(createCamcorderIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                        startActivity(chooser);
                    }
                    return;
                case audioMimeType:
                    if (mediaSource.equals(mediaSourceValueMicrophone)) {
                        // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                        // recorder.
                        startActivity(createSoundRecorderIntent());
                    } else {
                        // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                        // In all these cases so go ahead and launch an intent for both the sound
                        // recorder and audio/* OPENABLE.
                        Intent chooser = createChooserIntent(createSoundRecorderIntent());
                        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                        startActivity(chooser);
                    }
                    return;
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            startActivity(createDefaultOpenableIntent());
        }
        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, MainActivity.Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so fallback to the default file upload control.
                try {
                    mCaughtActivityNotFoundException = true;
                    mController.getActivity().startActivityForResult(createDefaultOpenableIntent(),
                            MainActivity.Controller.FILE_SELECTED);
                } catch (ActivityNotFoundException e2) {
                    // Nothing can return us a file, so file upload is effectively disabled.
                    Toast.makeText(mController.getActivity(), R.string.uploads_disabled,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                    createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }
        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE,
                    mController.getActivity().getResources()
                            .getString(R.string.choose_upload));
            return chooser;
        }
        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }
      private Intent createCameraIntent() {
          Date date = new Date();
          CharSequence format = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
          Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          File externalDataDir = Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_DCIM);
          File cameraDataDir = new File(externalDataDir.getAbsolutePath() + File.separator + "Browser");
          cameraDataDir.mkdirs();
          mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                  format + ".png";
          cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
          return cameraIntent;
      }
    private Intent createCamcorderIntent() {
      return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }
    private Intent createSoundRecorderIntent() {
      return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }
  }

  @Override
  public void onBackPressed () {

      if (webView.canGoBack()) {
         webView.goBack();
      } else  {
          if (!viewAdded) {

              Intent set = new Intent(MainActivity.this, MainActivity.class);
              finish();
              startActivity(set);
      }
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();

     }
          int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
              setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      }
  }

}

