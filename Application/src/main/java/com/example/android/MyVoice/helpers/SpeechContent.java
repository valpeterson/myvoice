package com.example.android.MyVoice.helpers;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.MyVoice.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Helper class for managing the speech content for PhraseListActivity and PhraseDetailActivity.
 * <p/>
 */
public class SpeechContent implements ListAdapter {

    public static final String TAG = "MyVoice.SpeechContent";
    /**
     * An array of speech items.
     */
    private SpeechFiler filer;
    private Context context;
    private int resource;
    private LayoutInflater inflater;

    public SpeechContent(Context ctx, int resourceID) throws IOException, XmlPullParserException {
        context = ctx;
        inflater = LayoutInflater.from( ctx );
        resource = resourceID;
        try {
            filer = new SpeechFiler();
        } catch (IOException e) {
            throw e;
        }
        // filer.LoadDictionary();
        try {
            filer.LoadCatalog();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error reading speech catalog file.");
            e.printStackTrace();
        }

    }

    public Object getItem(int position) {
        return filer.getChild(position).data;
    }

    public SpeechItem getItemFromID(String id) {
        return filer.getItemFromID(id);
    }

    public int getCount() {
        return filer.getNumNodeChildren();
    }

    public long getItemId(int position) {
        TreeNode node = filer.getChild(position);
        SpeechItem item = (SpeechItem)node.data;
        return Long.parseLong(item.id, 10);
    }

    public boolean isEmpty() {
        return (filer.getNumNodeChildren() == 0);
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String categoryName;

        if (view == null) {
            /* We weren't given anything to recycle, so create a new view
             of my layout and inflate it in the row */
            view = inflater.inflate(resource, null);
        }
        TextView text = (TextView)view.findViewById(R.id.categoryName);
        TreeNode node = filer.getChild(position);
        if (node != null) {
            categoryName = node.toString();
        } else {
            categoryName = "<enter new category here>";
        }
        text.setText(categoryName);
        return view;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;    //we don't have any separators
    }

    public int getItemViewType(int position) {
        return 0;   //we are only doing 1 view type
    }

    public int getViewTypeCount() {
        return 1;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        //TODO: what shall we do?
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        //TODO: what shall we do?
    }

    public void selectChild(int position) { filer.selectChild(position); }

    public void selectParent() {filer.selectParent();}

    public boolean isCategory() {return filer.isCategory();}
}
