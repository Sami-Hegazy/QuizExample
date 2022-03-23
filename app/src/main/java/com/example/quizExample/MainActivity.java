package com.example.quizExample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // COMPLETED (3) Create an instance variable storing a Cursor called mData
    // The data from the DroidTermsExample content provider
    private Cursor mData;

    private int mCurrentState;
    private Button mButton;
    private TextView mWordTextView, mDefinitionTextView;
    private int mDefCol, mWordCol;

    // This state is when the word definition is hidden and clicking the button will therefore
    // show the definition
    private final int STATE_HIDDEN = 0;

    // This state is when the word definition is shown and clicking the button will therefore
    // advance the app to the next word
    private final int STATE_SHOWN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWordTextView = findViewById(R.id.text_view_word);
        mDefinitionTextView = findViewById(R.id.text_view_definition);
        mButton = findViewById(R.id.button_next);
        mButton.setOnClickListener(this);

        new WordFetchTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mData.close();
    }

    public void nextWord() {
        // COMPLETED (3) Go to the next word in the Cursor, show the next word and hide the definition
        // Note that you shouldn't try to do this if the cursor hasn't been set yet.
        // If you reach the end of the list of words, you should start at the beginning again.

        if (mData != null) {
            // Move to the next position in the cursor, if there isn't one, move to the first
            if (!mData.moveToNext()) {
                mData.moveToFirst();
            }
            // Hide the definition TextView
            mDefinitionTextView.setVisibility(View.VISIBLE);

            // Change button text
            mButton.setText(getString(R.string.show_definition));

            // Get the next word
            mWordTextView.setText(mData.getString(mWordCol));
            mDefinitionTextView.setText(mData.getString(mDefCol));

            mCurrentState = STATE_HIDDEN;
        }
    }

    public void showDefinition() {
        // Change button text
        mButton.setText(getString(R.string.next_word));
        mCurrentState = STATE_SHOWN;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_next) {
            // Either show the definition of the current word, or if the definition is currently
            // showing, move to the next word.
            switch (mCurrentState) {
                case STATE_HIDDEN:
                    showDefinition();
                    break;
                case STATE_SHOWN:
                    nextWord();
                    break;
            }
        }
    }

    // Use an async task to do the data fetch off of the main thread.
    public class WordFetchTask extends AsyncTask<Void, Void, Cursor> {

        // Invoked on a background thread
        @Override
        protected Cursor doInBackground(Void... params) {
            // Make the query to get the data

            // Get the content resolver
            ContentResolver resolver = getContentResolver();

            // Call the query method on the resolver with the correct Uri from the contract class
            Cursor cursor = resolver.query(DroidTermsExampleContract.CONTENT_URI,
                    null, null, null, null);
            return cursor;
        }


        // Invoked on UI thread
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            // Set the data for MainActivity
            mData = cursor;
            // Get the column index, in the Cursor, of each piece of data
            mDefCol = mData.getColumnIndex(DroidTermsExampleContract.COLUMN_DEFINITION);
            mWordCol = mData.getColumnIndex(DroidTermsExampleContract.COLUMN_WORD);

            // Set the initial state
            nextWord();
        }
    }
}