/*
 * Copyright 2018 CMPUT301W18T18
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ca.ualberta.cs.wrkify;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity for a user to edit their profile. Must receive a User
 * as EXTRA_TARGET_USER, which will be the user to edit. If the
 * changes are saved, the activity will return RESULT_OK and the
 * modified user will be EXTRA_RETURNED_USER. If the activity is
 * cancelled, EXTRA_RETURNED_USER will not be set.
 */
public class EditProfileActivity extends AppCompatActivity {
    public static final int RESULT_UNSYNCED_CHANGES = 20;

    /** User being passed in to EditProfileActivity */
    public static final String EXTRA_TARGET_USER = "ca.ualberta.cs.wrkify.EXTRA_TARGET_USER";

    /** User being passed back from EditProfileActivity */
    public static final String EXTRA_RETURNED_USER = "ca.ualberta.cs.wrkify.EXTRA_RETURNED_USER";

    private User user;

    private EditText emailField;
    private EditText phoneField;

    /**
     * create the view and populate fields and appbar
     * @param savedInstanceState unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        this.user = (User) getIntent().getSerializableExtra(EXTRA_TARGET_USER);

        // Populate fields
        this.emailField = findViewById(R.id.editProfileEmailField);
        this.phoneField = findViewById(R.id.editProfilePhoneField);
        TextView usernameView = findViewById(R.id.editProfileUsername);

        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        usernameView.setText(user.getUsername());

        // Set app bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setTitle("Editing profile"); }
    }

    /**
     * setup the options menu to display the save option
     * @param menu the menu to inflate
     * @return always true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile, menu);

        // Save button saves the user
        menu.findItem(R.id.menuItemSaveProfile).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveAndFinish();
                return true;
            }
        });
        return true;
    }

    /**
     * end the activity when the user tries to go up.
     * @return always true.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Writes changes to the User. If successful, finishes the activity
     * with RESULT_OK. If the User rejects the changes, sets the error
     * message on the appropriate field and does not return.
     */
    private void saveAndFinish() {
        boolean valid = true;

        String newEmail = emailField.getText().toString();
        String newPhoneNumber = phoneField.getText().toString();

        try {
            User.verifyEmail(newEmail);
            user.setEmail(newEmail);
        } catch (IllegalArgumentException e) {
            emailField.setError("Not a valid email address");
            valid = false;
        }

        try {
            User.verifyPhoneNumber(newPhoneNumber);
            user.setPhoneNumber(newPhoneNumber);
        } catch (IllegalArgumentException e) {
            phoneField.setError("Not a valid phone number");
            valid = false;
        }

        if (valid) {
            this.new UploadProfileTask().execute(newEmail, newPhoneNumber);
        }
    }

    /**
     * UploadProfileTask is an AsyncTask that uploads the User
     * and then returns to the previous activity.
     */
    private class UploadProfileTask extends AsyncTask<String, Void, Void> {
        private int resultCode;
        /**
         * upload the user and save it if it is the session user
         * user should always be the session user
         * @param emailphone the email and the phone number
         * @return unused
         */
        @Override
        protected Void doInBackground(String... emailphone) {
            String newEmail = emailphone[0];
            String newPhoneNumber = emailphone[1];

            Session session = Session.getInstance(EditProfileActivity.this);

            TransactionManager transactionManager = session.getTransactionManager();
            transactionManager.enqueue(new UserSetEmailTransaction(user, newEmail));
            transactionManager.enqueue(new UserSetPhoneNumberTransaction(user, newPhoneNumber));

            if (transactionManager.flush(WrkifyClient.getInstance())) {
                resultCode = RESULT_OK;
            } else {
                resultCode = RESULT_UNSYNCED_CHANGES;
            }

            if (session.getUser().equals(user)) {
                session.setUser(user, EditProfileActivity.this);
            }

            WrkifyClient.getInstance().updateCached(user);

            return null;
        }

        /**
         * after we finish uploading the user,
         * finish the activity.
         * @param result unused
         */
        @Override
        protected void onPostExecute(Void result) {
            Intent intent = getIntent();
            intent.putExtra(EXTRA_RETURNED_USER, user);

            setResult(resultCode, intent);
            finish();
        }
    }
}
