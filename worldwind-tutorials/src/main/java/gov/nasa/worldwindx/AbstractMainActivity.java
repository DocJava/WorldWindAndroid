/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;
import java.util.Locale;

import gov.nasa.worldwind.FrameMetrics;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logger;

/**
 * This abstract Activity class implements a Navigation Drawer menu shared by all the World Wind Example activities.
 */
public abstract class AbstractMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    protected static final int PRINT_METRICS = 1;

    protected static final int PRINT_METRICS_DELAY = 3000;

    protected static final Date sessionTimestamp = new Date();

    protected static int selectedItemId = R.id.nav_basic_globe_activity;

    protected ActionBarDrawerToggle drawerToggle;

    protected NavigationView navigationView;

    protected String aboutBoxTitle = "Title goes here";

    protected String aboutBoxText = "Description goes here;";

    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == PRINT_METRICS) {
                return printMetrics();
            } else {
                return false;
            }
        }
    });

    /**
     * Returns a reference to the WorldWindow.
     * <p/>
     * Derived classes must implement this method.
     *
     * @return The WorldWindow GLSurfaceView object
     */
    abstract public WorldWindow getWorldWindow();

    /**
     * This method should be called by derived classes in their onCreate method.
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        this.onCreateDrawer();
    }

    protected void onCreateDrawer() {
        // Add support for a Toolbar and set to act as the ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add support for the navigation drawer full of examples
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.drawerToggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(this.drawerToggle);
        this.drawerToggle.syncState();

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        this.navigationView.setCheckedItem(selectedItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the menu by highlighting the last selected menu item
        this.navigationView.setCheckedItem(selectedItemId);
        // Use this Activity's Handler to periodically print the FrameMetrics.
        this.handler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop printing frame metrics when this activity is paused.
        this.handler.removeMessages(PRINT_METRICS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAboutBox();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setAboutBoxTitle(String title) {
        this.aboutBoxTitle = title;
    }

    protected void setAboutBoxText(String text) {
        this.aboutBoxText = text;
    }

    /**
     * This method is invoked when the About button is selected in the Options menu.
     */
    protected void showAboutBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.aboutBoxTitle);
        alertDialogBuilder
            .setMessage(this.aboutBoxText)
            .setCancelable(true)
            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                }
            });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected boolean printMetrics() {
        // Assemble the current system memory info.
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        // Assemble the current World Wind frame metrics.
        FrameMetrics fm = this.getWorldWindow().getFrameMetrics();

        // Print a log message with the system memory, World Wind cache usage, and World Wind average frame time.
        Logger.log(Logger.INFO, String.format(Locale.US, "System memory %,.0f KB    Heap memory %,.0f KB    Render cache %,.0f KB    Frame time %.1f ms + %.1f ms",
            (mi.totalMem - mi.availMem) / 1024.0,
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0,
            fm.getRenderResourceCacheUsedCapacity() / 1024.0,
            fm.getRenderTimeAverage(),
            fm.getDrawTimeAverage()));

        // Reset the accumulated World Wind frame metrics.
        fm.reset();

        // Print the frame metrics again after the configured delay.
        return this.handler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Persist the selected item between Activities
        selectedItemId = item.getItemId();

        // Handle navigation view item clicks here.
        switch (selectedItemId) {

            case R.id.nav_basic_globe_activity:
                startActivity(new Intent(getApplicationContext(), BasicGlobeActivity.class));
                break;
            case R.id.nav_camera_view_activity:
                startActivity(new Intent(getApplicationContext(), CameraViewActivity.class));
                break;
            case R.id.nav_camera_control_activity:
                startActivity(new Intent(getApplicationContext(), CameraControlActivity.class));
                break;
            case R.id.nav_look_at_view_activity:
                startActivity(new Intent(getApplicationContext(), LookAtViewActivity.class));
                break;
            case R.id.nav_navigator_event_activity:
                startActivity(new Intent(getApplicationContext(), NavigatorEventActivity.class));
                break;
            case R.id.nav_placemarks_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksActivity.class));
                break;
            case R.id.nav_placemarks_picking_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksPickingActivity.class));
                break;
            case R.id.nav_show_tessellation_activity:
                startActivity(new Intent(getApplicationContext(), ShowTessellationActivity.class));
                break;
            case R.id.nav_surface_image_activity:
                startActivity(new Intent(getApplicationContext(), SurfaceImageActivity.class));
                break;
            case R.id.nav_wms_layer_activity:
                startActivity(new Intent(getApplicationContext(), WmsLayerActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected static long getSessionTimestamp() {
        return sessionTimestamp.getTime();
    }
}