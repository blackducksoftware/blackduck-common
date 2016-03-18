package com.blackducksoftware.integration.hub.polling;

import java.util.concurrent.CountDownLatch;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;

public class ScanStatusChecker implements Runnable {

    private ScanStatusToPoll currentStatus;

    private final CountDownLatch countDownLock;

    private final HubIntRestService service;

    private HubIntegrationException exception = null;

    private boolean running;

    public ScanStatusChecker(HubIntRestService service, ScanStatusToPoll currentStatus, CountDownLatch countDownLock) {
        this.service = service;
        this.currentStatus = currentStatus;
        this.countDownLock = countDownLock;
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean hasError() {
        return exception != null;
    }

    public HubIntegrationException getError() {
        return exception;
    }

    private boolean isScanFinished(ScanStatusToPoll status) {
        if (ScanStatus.isFinishedStatus(currentStatus.getStatusEnum()) == false) {
            return false;
        } else {

            if (ScanStatus.isErrorStatus(currentStatus.getStatusEnum())) {
                exception = new HubIntegrationException("There was a problem with one of the scans. Error Status : "
                        + currentStatus.getStatusEnum().name());
            }
            countDownLock.countDown(); // finished so unlock the parent thread.
            setRunning(false);
            return true;
        }

    }

    @Override
    public void run() {
        while (isRunning() == true) {

            try {
                if (isScanFinished(currentStatus) == true) {
                    break;
                } else {
                    // The code location is still updating or matching, etc.
                    currentStatus = service.checkScanStatus(currentStatus.get_meta().getHref());

                    if (isScanFinished(currentStatus) == true) {
                        break;
                    }
                }

                Thread.sleep(10000);
            } catch (Exception ex) {
                setRunning(false);
            }
        }
    }
}
