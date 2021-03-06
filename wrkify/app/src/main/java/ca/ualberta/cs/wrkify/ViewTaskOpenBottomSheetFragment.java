package ca.ualberta.cs.wrkify;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import static android.view.View.inflate;


/**
 * Bottom sheet for a task provider viewing a task that is
 * open or bidded, and that they have not bidded on themselves.
 * Contains controls for placing a bid.
 */
public class ViewTaskOpenBottomSheetFragment extends ViewTaskBottomSheetFragment {

    private Task task;

    /**
     * create the bottomsheet view
     * @param inflater the LayoutInflater
     * @param container the ViewGroup container
     * @param savedInstanceState unused
     * @return the created view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        EditText bidField = getContentView().findViewById(R.id.taskViewBottomSheetBidField);
        Button button = getContentView().findViewById(R.id.taskViewBottomSheetButtonBid);

        // Confirm and submit bid on button press or text field submit
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                confirmAndSubmitBid(view);
            }
        });
        bidField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    confirmAndSubmitBid(view);
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    /**
     * Raises a confirmation dialog. If confirmed, submits the entered bid.
     * @param view View corresponding to the sheet
     */
    private void confirmAndSubmitBid(View view) {
        final EditText bidField = view.findViewById(R.id.taskViewBottomSheetBidField);
        final Price bidPrice;
        try {
            bidPrice = new Price(bidField.getText().toString());
        } catch (NumberFormatException e) {
            // invalid bid
            return;
        }
        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.makeDialog(
                    String.format(Locale.US, "Bid $%s on this task?", bidField.getText()),
                    "Cancel", "Bid",
                    new ConfirmationDialogFragment.OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            try {
                                new BidTask().execute(bidPrice);
                                collapse();
                            } catch (NumberFormatException e) {
                                // invalid bid - continue
                            }
                        }
                    }
        );
        dialog.show(getActivity().getFragmentManager(), null);
    }

    /**
     * BidTask is an AsyncTask to place a bid and then update the view
     */
    private class BidTask extends AsyncTask<Price, Void, Void> {
        /**
         * place the view
         * @param prices a single price
         * @return unused
         */
        @Override
        protected Void doInBackground(Price... prices) {

            Bid bid = new Bid(
                    prices[0],
                    Session.getInstance(getContext()).getUser());

            User user = Session.getInstance(getContext()).getUser();
            Bid userBid = task.getBidForUser(user);
            task.replaceBid(userBid, bid);

            TransactionManager transactionManager = Session.getInstance(getActivity()).getTransactionManager();
            transactionManager.enqueue(new TaskAddOrReplaceBidTransaction(task, userBid, bid));

            // TODO notify of offline status
            transactionManager.flush(WrkifyClient.getInstance());

            WrkifyClient.getInstance().updateCached(task);
            return null;
        }

        /**
         * update the view
         * @param result unused
         */
        @Override
        protected void onPostExecute(Void result) {
            ((ViewTaskActivity) getActivity()).initializeFromTask(task);
        }
    }

    /**
     * reinitialize the view from a task
     * @param container ViewGroup containing the header and content frame
     * @param task task object to initialize from.
     */
    @Override
    protected void initializeWithTask(ViewGroup container, Task task) {
        this.task = task;
        Integer bidCount = task.getBidList().size();
        if (bidCount == 0) {
            setDetailString(container,
                    "No bids yet");
        }
        else {
            setDetailString(container,
                    String.format(Locale.US, "%d bids so far", bidCount));
            Bid lowestBid = task.getBidList().get(0);
            setRightStatusString(container, lowestBid.getValue().toString());
        }
    }

    /**
     * get the status string
     * @return always null
     */
    @Override
    protected String getStatusString() {
        return "Open";
    }

    /**
     * get the background color of the bottomsheet
     * @return always colorStatusRequested
     */
    @Override
    protected int getBackgroundColor() {
        return R.color.colorStatusRequested;
    }

    /**
     * gets the content layout
     * @param root the root viewgroup
     * @return the inflated view
     */
    @Override
    protected View getContentLayout(ViewGroup root) {
        return inflate(getActivity(), R.layout.activity_view_task_bottom_sheet_bid, null);
    }

    /**
     * collapse the bottom sheet view
     */
    @Override
    public void collapse() {
        if (getView().findViewById(R.id.taskViewBottomSheetBidField).isFocused()) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        super.collapse();
    }
}
