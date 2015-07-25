package org.techteam.decider.auth;

import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CanceledAccountManagerFuture<T> implements AccountManagerFuture<T> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
        throw new OperationCanceledException();
    }

    @Override
    public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
        throw new OperationCanceledException();
    }
}
