package common.patterns.commandchain.processor;

import common.patterns.commandchain.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@SuppressWarnings({"PMD.LoggerIsNotStaticFinal"})
public abstract class AbstractProcessor<R extends ProcessorRequest, C extends ApplicationContext> implements Processor<R,C>{
    private final boolean terminateOnException;
    private Processor<R,C> nextProcessor;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected AbstractProcessor(final boolean terminateOnException) {
        this.terminateOnException = terminateOnException;
    }

    @Override
    public boolean isProcessingTerminatedOnException() {
        return this.terminateOnException;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public Optional<Processor<R, C>> getNextProcessor() {
        return Optional.ofNullable(nextProcessor);
    }

    public void setNextProcessor(final Processor<R, C> nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
