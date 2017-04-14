package com.example.uberv.loadersdemo;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MainActivity.class.getSimpleName();

    // Adapter for displaying the list's data
    SimpleCursorAdapter mAdapter;
    // Search filter working with OnQueryTextListener
    String mCurFilter;
    // Contacts columns that we wil lretrieve
    static final String[] PROJECTION = new String[]
            {ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME};
    // Select criteria for the contacts URI
    static final String SELECTION = "((" +
            ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
            ContactsContract.Data.DISPLAY_NAME + " != '' ))";
    // Loader id
    static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the adapter
        mAdapter = createEmptyAdapter();
        setListAdapter(mAdapter);

        // Hide the listview and show the progress bar
        showProgressbar();

        // Initialize a loader for an id of 0
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Create a simple list adapter with a null cursor
     *
     * @return
     */
    private SimpleCursorAdapter createEmptyAdapter() {
        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
        int[] toViews = {R.id.display_name_tv};
        // Return the cursor
        return new SimpleCursorAdapter(this,
                R.layout.simple_list_item,
                null,   // cursor
                fromColumns,
                toViews);
    }

    /**
     * This is a LoaderManager callback. Return a properly constructor CursorLoader
     * This gets called only if the loader does not previously exist.
     * This means this method will not be called on rotation because a previous
     * loader with this ID is already available and initialized
     * This also gets called when the loader is "restarted" by calling LoaderManager.restartLoader()
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader for loader id:" + id);
        Uri baseUri;
        if (mCurFilter != null) {
            // we have some filter for contact names
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mCurFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }
        String[] selectionsArgs = null;
        String sortOrder = null;
        return new CursorLoader(
                this,
                baseUri,
                PROJECTION,
                SELECTION,
                selectionsArgs,
                sortOrder);
    }

    /**
     * This gets called when the laoder finishes. Called on the main thread.
     * can be called multiple times as the data changes underneath;
     * Also gets called after rotation with out requerying the cursor.
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished for loader id:" + loader.getId());
        Log.d(TAG, "Number of items found: " + data.getCount());
        hideProgressbar();
        mAdapter.swapCursor(data);
    }

    /**
     * Remove any references to this data.
     * This gets called when the loader is destroyed like when activity is done
     * FYI - this does NOT get called because of loader "restart"
     * This can be seen as a "destructor" for the laoder
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset for loader id:" + loader.getId());
        showProgressbar();
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        Log.d(TAG, "Restarting the loader");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(this);
        sv.setOnQueryTextListener(this);
        item.setActionView(sv);
        return true;
    }

    private void showProgressbar() {
        //show progress bar
        View pbar = this.getProgressbar();
        pbar.setVisibility(View.VISIBLE);
        //hide listview
        getListView().setVisibility(View.GONE);
        findViewById(android.R.id.empty).setVisibility(View.GONE);
    }

    private void hideProgressbar() {
        //show progress bar
        View pbar = this.getProgressbar();
        pbar.setVisibility(View.GONE);
        //hide listview
        getListView().setVisibility(View.VISIBLE);
    }

    private View getProgressbar() {
        return findViewById(R.id.tla_pbar);
    }
}
