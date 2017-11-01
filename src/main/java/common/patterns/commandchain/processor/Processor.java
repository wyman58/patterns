package common.patterns.commandchain.processor;

import common.patterns.commandchain.context.ApplicationContext;
import org.slf4j.Logger;

import java.util.Optional;

public interface Processor<R extends ProcessorRequest, C extends ApplicationContext> {
    boolean isApplicable(R request, C context);
    boolean isProcessingTerminatedOnException();
    ProcessorResponse process(R request, C context);
    Logger getLogger();
    Optional<Processor<R,C>> getNextProcessor();
    void setNextProcessor(Processor<R, C> processor);
}
