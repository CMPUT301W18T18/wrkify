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


import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SignalManager converts signals inro android notifications
 */
public class SignalManager {
    private static final String NI_GENERIC_TOP = "The task";
    private static final String NI_CLOSED_BOTTOM = "has been marked complete.";
    private static final String NI_DEASSIGNED_TOP = "You've been deassigned from";
    private static final String NI_ASSIGNED_BOTTOM = "has been assigned to you.";
    private static final String NI_GENERIC_BID_TOP = "Your bid on";
    private static final String NI_REJECTED_BOTTOM = "has been rejected.";
    private static final String NI_OUTBID_BOTTOM = "is no longer the highest bid.";
    private static final String NI_GENERIC_POST_TOP = "Your posted task";
    private static final String NI_NEW_BIDS_BOTTOM_F = "has %d new bids.";

    private final RemoteClient client;
    private final Map<String, List<Signal>> targetMap;

    /**
     * create the SignalManager from a RemoteClient
     * @param client the client
     */
    public SignalManager(RemoteClient client) {
        this.client = client;
        this.targetMap = new HashMap<>();
    }

    /**
     * add a signal to the signal manager;
     * @param signal the signal to add
     */
    public void addSignal(Signal signal) {
        String targetID = signal.getTargetId();
        if (!targetMap.containsKey(targetID)) {
            targetMap.put(signal.getTargetId(), new ArrayList<Signal>());
        }

        targetMap.get(targetID).add(signal);
    }

    /**
     * add many signals to the signal manager via
     * addSignal
     * @param signals the signals to add
     */
    public void addSignals(List<Signal> signals) {
        for (Signal s: signals) {
            this.addSignal(s);
        }
    }

    /**
     * clears the signals
     */
    public void clear() {
        this.targetMap.clear();
    }

    /**
     * get all the notifications from the
     * accrued signals.
     * @return the list of Notifications
     */
    public List<NotificationInfo> getNotifications() {
        List<NotificationInfo> notifications = new ArrayList<>();

        for (String targetID: targetMap.keySet()) {
            List<Signal> targetSignals = targetMap.get(targetID);
            NotificationInfo notification = generateNotificationFor(targetID, targetSignals);
            if (notification != null) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * delete signals that target the given id
     * @param id the given id
     */
    public void deleteSignalsForID(String id) {
        if (!this.targetMap.containsKey(id)) { return; }

        for (Signal signal: this.targetMap.get(id)) {
            client.delete(signal);
        }
    }

    /**
     * generate notifications for a target
     * @param targetID the id of the target
     * @param targetSignals the signals for the target
     * @return the notification
     */
    @Nullable
    protected NotificationInfo generateNotificationFor(String targetID, List<Signal> targetSignals) {
        int newBidCount = 0;
        Signal firstNewBidSignal = null;

        for (Signal signal: targetSignals) {
            switch (signal.getType()) {
                case SIGNAL_CLOSED: return makeTaskStatusNI(NI_GENERIC_TOP, signal.getTargetName(), NI_CLOSED_BOTTOM,
                        signal.getTargetId());
                case SIGNAL_DEASSIGNED: return makeTaskStatusNI(NI_DEASSIGNED_TOP, signal.getTargetName(), "",
                        signal.getTargetId());
                case SIGNAL_REJECTED: return makeTaskStatusNI(NI_GENERIC_BID_TOP, signal.getTargetName(), NI_REJECTED_BOTTOM,
                        signal.getTargetId());
                case SIGNAL_ASSIGNED: return makeTaskStatusNI(NI_GENERIC_TOP, signal.getTargetName(), NI_ASSIGNED_BOTTOM,
                        signal.getTargetId());
                case SIGNAL_OUTBID: return makeTaskStatusNI(NI_GENERIC_BID_TOP, signal.getTargetName(), NI_OUTBID_BOTTOM,
                        signal.getTargetId());
                case SIGNAL_NEW_BID:
                    newBidCount += 1;
                    if (firstNewBidSignal == null) { firstNewBidSignal = signal; }
                default:
                    Log.w("SignalManager", "Unsupported notification signal" + signal.getType());
            }
        }

        if (newBidCount > 0) { return makeNewBidsNI(firstNewBidSignal.getTargetName(), firstNewBidSignal.getTargetId(), newBidCount); }

        return null;
    }

    /**
     * make a notification for task statuts
     * @param preText
     * @param taskTitle
     * @param postText
     * @param taskID the id of the task to notify for.
     * @return the generated notification
     */
    protected NotificationInfo makeTaskStatusNI(String preText, String taskTitle, String postText, String taskID) {
        NotificationInfo notification = new NotificationInfo(preText, taskTitle, postText);
        notification.setIsImportant(true);
        notification.setViewTarget(taskID, new ViewTaskNotificationAction(taskID));
        notification.setOnNotificationAcknowledgedListener(new NotificationInfo.OnNotificationAcknowledgedListener() {
            @Override
            public void onNotificationAcknowledged(NotificationInfo notification) {
                deleteSignalsForID(notification.getTargetId());
            }
        });
        return notification;
    }

    /**
     * make a notification for task statuts
     * @param bidCount the number of bids to notify for
     * @param taskTitle
     * @param taskID the id of the task to notify for.
     * @return the generated notification
     */
    protected NotificationInfo makeNewBidsNI(String taskTitle, String taskID, int bidCount) {
        String taskCountString = String.format(Locale.US, NI_NEW_BIDS_BOTTOM_F, bidCount);
        NotificationInfo notification = new NotificationInfo(NI_GENERIC_POST_TOP, taskTitle, taskCountString);
        notification.setIsImportant(false);
        notification.setViewTarget(taskID, new ViewTaskNotificationAction(taskID));
        notification.setOnNotificationAcknowledgedListener(new NotificationInfo.OnNotificationAcknowledgedListener() {
            @Override
            public void onNotificationAcknowledged(NotificationInfo notification) {
                deleteSignalsForID(notification.getTargetId());
            }
        });
        return notification;
    }
}
