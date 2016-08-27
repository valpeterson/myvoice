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
    private SpeechContent content;
    /**
     * The dummy content this fragment is presenting.
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
        content = AppData.getInstance().getContent();
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = content.getItemFromID(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phrase_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.phrase_detail)).setText(mItem.toString());
            if (!mItem.isCategory) {
                ((TextView) rootView.findViewById(R.id.phrase_file)).setText(mItem.audioFilePath);
            }
        }

        return rootView;
    }
}
