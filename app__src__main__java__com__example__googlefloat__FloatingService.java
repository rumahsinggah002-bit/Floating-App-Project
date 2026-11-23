package com.example.googlefloat;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

public class FloatingService extends Service {
    private WindowManager wm;
    private View bubble, panel;
    private WebView browser;
    private EditText address;
    private WindowManager.LayoutParams bubbleParams, panelParams;
    private boolean expanded = true;
    private int panelWidth, panelHeight;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Notification n = new Notification.Builder(this, "google_float")
            .setContentTitle("GOOGLE FLOAT berjalan")
            .setContentText("Ketuk tombol Google untuk membuka browser mini")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true).build();
        startForeground(7, n);
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        if (Settings.canDrawOverlays(this)) createBubble();
        else stopSelf();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("google_float", "Google Float", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); return d;
    }

    private TextView button(String text) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(15);
        v.setGravity(Gravity.CENTER); v.setTypeface(null, Typeface.BOLD);
        v.setBackground(bg(Color.rgb(42,112,230), 18));
        return v;
    }

    private void createBubble() {
        if (bubble != null) return;
        TextView b = new TextView(this);
        b.setText("G"); b.setTextSize(25); b.setTypeface(null, Typeface.BOLD);
        b.setTextColor(Color.rgb(66,133,244)); b.setGravity(Gravity.CENTER);
        b.setBackground(bg(Color.WHITE, 100));
        b.setElevation(12);
        bubble = b;
        bubbleParams = new WindowManager.LayoutParams(dp(54), dp(54), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = getPreferences().getInt("bubble_x", 24);
        bubbleParams.y = getPreferences().getInt("bubble_y", 220);
        wm.addView(bubble, bubbleParams);
        b.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy; int ox, oy; boolean moved;
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    sx=e.getRawX(); sy=e.getRawY(); ox=bubbleParams.x; oy=bubbleParams.y; moved=false; return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx=(int)(e.getRawX()-sx), dy=(int)(e.getRawY()-sy);
                    if (Math.abs(dx)+Math.abs(dy)>8) moved=true;
                    bubbleParams.x=ox+dx; bubbleParams.y=oy+dy; wm.updateViewLayout(bubble,bubbleParams); return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    getPreferences().edit().putInt("bubble_x",bubbleParams.x).putInt("bubble_y",bubbleParams.y).apply();
                    if (!moved) togglePanel(); return true;
                }
                return true;
            }
        });
    }

    private void togglePanel() {
        if (panel != null) { closePanel(); return; }
        buildPanel();
    }

    private void buildPanel() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(8),dp(8),dp(8),dp(8));
        outer.setBackground(bg(Color.WHITE, dp(20)));
        outer.setElevation(18);
        panel = outer;

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8),dp(4),dp(4),dp(4));
        top.setBackground(bg(Color.rgb(235,243,255),dp(14)));
        TextView brand = new TextView(this); brand.setText(" G  GOOGLE FLOAT"); brand.setTextColor(Color.rgb(35,85,160));
        brand.setTypeface(null,Typeface.BOLD); brand.setTextSize(13);
        top.addView(brand,new LinearLayout.LayoutParams(0,dp(38),1));
        TextView resize=button("↗"); TextView min=button("—"); TextView close=button("×");
        top.addView(resize,new LinearLayout.LayoutParams(dp(38),dp(36)));
        addSpace(top,5);
        top.addView(min,new LinearLayout.LayoutParams(dp(38),dp(36)));
        addSpace(top,5);
        top.addView(close,new LinearLayout.LayoutParams(dp(38),dp(36)));
        outer.addView(top);

        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        address = new EditText(this); address.setSingleLine(true); address.setTextSize(13);
        address.setHint("Cari Google atau masukkan alamat");
        address.setPadding(dp(10),0,dp(8),0);
        address.setText("https://www.google.com");
        bar.addView(address,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView go=button("GO"); LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(dp(48),dp(38)); gp.leftMargin=dp(5); bar.addView(go,gp);
        outer.addView(bar);

        LinearLayout nav = new LinearLayout(this); nav.setGravity(Gravity.CENTER);
        TextView back=button("‹"), forward=button("›"), refresh=button("⟳"), home=button("⌂"), fav=button("☆");
        TextView[] navs={back,forward,refresh,home,fav};
        for(TextView t:navs){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(34),1); p.setMargins(dp(2),dp(3),dp(2),dp(3)); nav.addView(t,p); }
        outer.addView(nav);

        browser = new WebView(this);
        WebSettings ws=browser.getSettings();
        ws.setJavaScriptEnabled(true); ws.setDomStorageEnabled(true); ws.setSupportZoom(true); ws.setBuiltInZoomControls(true); ws.setDisplayZoomControls(false);
        browser.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view,String url){ if(address!=null) address.setText(url); saveHistory(url); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u=r.getUrl();
                if ("http".equals(u.getScheme()) || "https".equals(u.getScheme())) return false;
                return true;
            }
        });
        browser.setWebChromeClient(new WebChromeClient());
        outer.addView(browser,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(panelWidth==0?340:panelWidth),dp(panelHeight==0?480:panelHeight));
        panelParams=new WindowManager.LayoutParams(p.width,p.height,overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
        panelParams.gravity=Gravity.TOP|Gravity.START; panelParams.x=dp(12); panelParams.y=dp(100);
        wm.addView(panel,panelParams);

        go.setOnClickListener(v->navigate());
        address.setOnEditorActionListener((v,id,event)->{navigate(); return true;});
        back.setOnClickListener(v->{if(browser.canGoBack())browser.goBack();});
        forward.setOnClickListener(v->{if(browser.canGoForward())browser.goForward();});
        refresh.setOnClickListener(v->browser.reload());
        home.setOnClickListener(v->{address.setText("https://www.google.com");navigate();});
        fav.setOnClickListener(v->{
            String saved=getPreferences().getString("favorite","");
            if(saved.isEmpty()) Toast.makeText(this,"Belum ada favorit. Tekan ☆ lagi setelah membuka situs.",Toast.LENGTH_SHORT).show();
            else { address.setText(saved); browser.loadUrl(saved); }
        });
        fav.setOnLongClickListener(v->{String u=browser.getUrl(); if(u!=null){getPreferences().edit().putString("favorite",u).apply(); Toast.makeText(this,"Favorit disimpan",Toast.LENGTH_SHORT).show();} return true;});
        history.setOnClickListener(v->{
            String raw=getPreferences().getString("history","");
            String[] items=raw.isEmpty()?new String[]{"Belum ada riwayat"}:raw.split("\\n");
            new AlertDialog.Builder(this).setTitle("Riwayat pencarian").setItems(items,(dialog,which)->{
                if(!raw.isEmpty() && which<items.length){address.setText(items[which]);browser.loadUrl(items[which]);}
            }).setPositiveButton("Tutup",null).show();
        });
        close.setOnClickListener(v->closePanel());
        min.setOnClickListener(v->closePanel());
        resize.setOnClickListener(v->{
            panelWidth=panelParams.width>dp(400)?dp(320):dp(400);
            panelHeight=panelParams.height>dp(550)?dp(460):dp(600);
            panelParams.width=panelWidth; panelParams.height=panelHeight; wm.updateViewLayout(panel,panelParams);
        });
        brand.setOnTouchListener(new View.OnTouchListener(){
            float sx,sy; int ox,oy;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==0){sx=e.getRawX();sy=e.getRawY();ox=panelParams.x;oy=panelParams.y;return true;}
                if(e.getAction()==2){panelParams.x=ox+(int)(e.getRawX()-sx);panelParams.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(panel,panelParams);return true;}
                return true;
            }
        });
        browser.loadUrl("https://www.google.com");
    }

    private void navigate() {
        String s=address.getText().toString().trim(); if(s.isEmpty())return;
        String url;
        if(s.matches("(?i)^https?://.*")) url=s;
        else if(s.contains(".") && !s.contains(" ")) url="https://"+s;
        else url="https://www.google.com/search?q="+Uri.encode(s);
        browser.loadUrl(url);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);
        address.clearFocus();
    }
    private void saveHistory(String url) {
        String old=getPreferences().getString("history","");
        String next=url+"\n"+old;
        String[] lines=next.split("\\n");
        StringBuilder b=new StringBuilder();
        for(int i=0;i<Math.min(30,lines.length);i++) if(!lines[i].isEmpty()) b.append(lines[i]).append("\n");
        getPreferences().edit().putString("history",b.toString()).apply();
    }
    private android.content.SharedPreferences getPreferences(){return getSharedPreferences("google_float",MODE_PRIVATE);}
    private void addSpace(LinearLayout l,int w){View s=new View(this);l.addView(s,new LinearLayout.LayoutParams(dp(w),1));}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);}
    private void closePanel(){if(panel!=null){try{wm.removeView(panel);}catch(Exception ignored){} panel=null;browser=null;address=null;}}
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){closePanel();if(bubble!=null){wm.removeView(bubble);bubble=null;}stopForeground(true);super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
