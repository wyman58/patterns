package common.patterns.commandchain;

import common.patterns.commandchain.context.ApplicationContext;
import common.patterns.commandchain.processor.Processor;
import common.patterns.commandchain.processor.ProcessorRequest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ProcessorChainBuilder<R extends ProcessorRequest, C extends ApplicationContext> {
    private final List<Processor<R,C>> processorChain = new ArrayList<>();

    public ProcessorChainBuilder<R,C> addProcessor(final Processor<R,C> processor) {
        if(!processorChain.isEmpty()) {
            final Processor<R, C> lastProcessor = processorChain.get(processorChain.size() - 1);
            lastProcessor.setNextProcessor(processor);
        }
        processorChain.add(processor);
        return this;
    }

    public Processor<R,C> build() {
        Assert.notEmpty(processorChain,"Built an empty processor list");
        return processorChain.get(0);
    }
}
