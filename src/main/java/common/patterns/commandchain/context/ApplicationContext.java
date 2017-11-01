package common.patterns.commandchain.context;

import java.util.Optional;

@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod"})
public abstract class ApplicationContext {
    private Throwable thrownException;

    public void setThrownException(final Throwable throwable) {
        thrownException = throwable;
    }

    public Optional<Throwable> getThrownException() {
        return Optional.ofNullable(thrownException);
    }
}
