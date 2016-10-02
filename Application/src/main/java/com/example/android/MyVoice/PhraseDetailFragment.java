package com.example.android.MyVoice;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.android.MyVoice.helpers.AppData;
import com.example.android.MyVoice.helpers.SpeechContent;
import com.example.android.MyVoice.helpers.SpeechItem;

/**
 * A fragment representing a single Phrase detail screen.
 * This fragment is either contained in a {@link PhraseListActivity}
 * in two-pane mode (on tablets) or a {@link PhraseDetailActivity}
 * on handsets.
 */
public class PhraseDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private SpeechContent mContent;
    private TextView mNameText;
    private TextView mFileText;

    /**
     * The dummy mContent this fragment is presenting.
     */
    private SpeechItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhraseDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = AppData.getInstance().getContent();
//        if (getArguments().containsKey(ARG_ITEM_ID)) {
            //find out from fragment arguments what item was selected in the parent activity
//            mItem = mContent.getItemFromID(getArguments().getString(ARG_ITEM_ID));
//        }
    }

    public void updateData(String itemID) {
        mItem = mContent.getItemFromID(itemID);
        mNameText.setText(mItem.toString());
        if (!mItem.isCategory) {
            mFileText.setText(mItem.audioFilePath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phrase_detail, container, false);
        mNameText = (TextView) rootView.findViewById(R.id.phrase_detail);
        mFileText = (TextView) rootView.findViewById(R.id.phrase_file);
        // Show the details of the selected item
        if (mItem != null) {
            mNameText.setText(mItem.toString());
            if (!mItem.isCategory) {
                mFileText.setText(mItem.audioFilePath);
            }
        }

        return rootView;
    }
}
