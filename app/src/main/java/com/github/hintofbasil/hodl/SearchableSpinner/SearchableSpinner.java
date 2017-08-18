package com.github.hintofbasil.hodl.SearchableSpinner;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified from https://www.simplifiedcoding.net/android-spinner-with-search/
 */

public class SearchableSpinner extends Spinner implements View.OnTouchListener,
        SearchableListDialog.SearchableItem {

    String selectedItem;
    //this string above will store the value of selected item.

    public static final int NO_ITEM_SELECTED = -1;
    private Context context;
    private List items;
    private SearchableListDialog searchableListDialog;

    private boolean isDirty;
    private ArrayAdapter arrayAdapter;

    public SearchableSpinner(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        items = new ArrayList();
        searchableListDialog = SearchableListDialog.newInstance
                (items);
        searchableListDialog.setOnSearchableItemClickListener(this);
        setOnTouchListener(this);

        arrayAdapter = (ArrayAdapter) getAdapter();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (null != arrayAdapter) {

                // Refresh content #6
                // Change Start
                // Description: The items were only set initially, not reloading the data in the
                // spinner every time it is loaded with items in the adapter.
                items.clear();
                for (int i = 0; i < arrayAdapter.getCount(); i++) {
                    items.add(arrayAdapter.getItem(i));
                }
                // Change end.

                searchableListDialog.show(scanForActivity(context).getFragmentManager(), "TAG");
            }
        }
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        arrayAdapter = (ArrayAdapter) adapter;
        super.setAdapter(adapter);
    }

    //The method just below is executed  when an item in the searchlist is tapped.This is where we store the value int string called selectedItem.
    @Override
    public void onSearchableItemClicked(Object item, int position) {
        setSelection(items.indexOf(item));

        if (!isDirty) {
            isDirty = true;
            setAdapter(arrayAdapter);
            setSelection(items.indexOf(item));
        }
        selectedItem= getItemAtPosition(position).toString();

        Toast.makeText(getContext(),"You selected "+selectedItem, Toast.LENGTH_LONG).show();
    }

    private Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    @Override
    public int getSelectedItemPosition() {
        if (!isDirty) {
            return NO_ITEM_SELECTED;
        } else {
            return super.getSelectedItemPosition();
        }
    }

    @Override
    public Object getSelectedItem() {
        if (!isDirty) {
            return null;
        } else {
            return super.getSelectedItem();
        }
    }
}