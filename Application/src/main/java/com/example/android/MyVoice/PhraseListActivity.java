package com.example.android.MyVoice;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

import com.example.android.MyVoice.helpers.AppData;
import com.example.android.MyVoice.helpers.SpeechContent;
import com.example.android.MyVoice.helpers.SpeechItem;


/**
 * An activity representing a list of Phrases. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PhraseDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PhraseListFragment} and the item details
 * (if present) is a {@link PhraseDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link PhraseListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PhraseListActivity extends Activity
        implements PhraseListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SpeechContent mContent;
    private PhraseDetailFragment mDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mContent = new SpeechContent(getApplicationContext(), R.layout.relative_phrase_list);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage() + ".  Returning to top menu.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        AppData.getInstance().setContent(mContent);  //Store SpeechContent for the fragments and other activities to use
        setContentView(R.layout.activity_phrase_list);

        if (findViewById(R.id.phrase_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PhraseListFragment) getFragmentManager()
                    .findFragmentById(R.id.phrase_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link PhraseListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        SpeechItem mItem;

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            mItem = mContent.getItemFromID(id);
            if (mDetailFragment == null) {
//                Bundle arguments = new Bundle();
//                arguments.putString(PhraseDetailFragment.ARG_ITEM_ID, id);
                mDetailFragment = new PhraseDetailFragment();
//                mDetailFragment.setArguments(arguments);
                // If we're drilling down into a category then add to the back stack so user can use back button to navigate back up to category
                if (mItem.isCategory) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.phrase_detail_container, mDetailFragment)
                            .addToBackStack("category") //PhraseListFragment will use this to know if it needs to go back to parent when back button is pressed
                            .commit();
                } else {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.phrase_detail_container, mDetailFragment)
                            .commit();
                }
            } else {
                mDetailFragment.updateData(id);
            }
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PhraseDetailActivity.class);
                                    detailIntent.putExtra(PhraseDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    /**
     * Callback method from {@link PhraseListFragment.Callbacks}
     * indicating that a fragment was popped off the back stack because user wants to go
     * back up to parent category.
     */
    @Override
    public void onBackStack() {
        mDetailFragment = null;
    }

}
