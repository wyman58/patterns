package common.patterns.commandchain;

import common.patterns.commandchain.context.ApplicationContext;
import common.patterns.commandchain.processor.Processor;
import common.patterns.commandchain.processor.ProcessorRequest;
import common.patterns.commandchain.processor.ProcessorResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static common.patterns.commandchain.processor.ProcessorResponse.*;

@SuppressWarnings({"PMD.MoreThanOneLogger", "PMD.LoggerIsNotStaticFinal"})
public class ProcessorChainRunner<R extends ProcessorRequest, C extends ApplicationContext> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ProcessorResponse run(final Processor<R,C> rootProcessor, final R request, final C context) {
        log.info("Start processor chain from [{}]", rootProcessor.getClass().getSimpleName());

        Optional<Processor<R,C>> currentProcessor = Optional.of(rootProcessor);
        boolean chainComplete = false;

        ProcessorResponse result = PROCESSED;
        while(!chainComplete) {
            final Processor<R,C> processor = currentProcessor.get();

            final Logger processorLogger = processor.getLogger();

            final String processorName = processor.getClass().getSimpleName();
            processorLogger.info("    Processor [{}]", processorName);

            final long startTimeIsApplicable = System.currentTimeMillis();

            try {
                final boolean applicable = isProcessorApplicable(request, context, processor, processorLogger);
                final long durationIsApplicable = System.currentTimeMillis() - startTimeIsApplicable;
                processorLogger.info("        {} isApplicable duration [{} ms]",
                        processorName, durationIsApplicable);

                if(applicable) {
                    final long startTimeProcess = System.currentTimeMillis();

                    try {
                        final ProcessorResponse processorResponse = processor.process(request, context);
                        final long durationProcess = System.currentTimeMillis() - startTimeProcess;

                        processorLogger.info("        {} process duration [{} ms]",
                                processorName, durationProcess);

                        processorLogger.info("        ProcessorResponse [{}]",
                                processorResponse);

                        if(processorResponse.equals(TERMINATE_PROCESSING)) {
                            result = getProcessorResponseWhenTerminating(processorLogger);
                            chainComplete = true;
                        }
                    } catch (final Throwable t) {
                        context.setThrownException(t);

                        final long durationProcess = System.currentTimeMillis() - startTimeProcess;
                        processorLogger.info("        {} process duration [{} ms]",
                                processorName, durationProcess);

                        processorLogger.info("        Threw exception [{}]", ExceptionUtils.getStackTrace(t));

                        if(processor.isProcessingTerminatedOnException()) {
                            result = getProcessorResponseTerminatingOnException(processorLogger);
                            chainComplete = true;
                        } else {
                            processorLogger.info("        Continuing following this exception");
                        }
                    }
                } else {
                    processorLogger.info("        Skipped");
                }

                currentProcessor = processor.getNextProcessor();
                chainComplete |= !currentProcessor.isPresent();
            } catch (Throwable t) {
                final long durationIsApplicable = System.currentTimeMillis() - startTimeIsApplicable;
                processorLogger.info("        {} isApplicable duration [{} ms]",
                        processorName, durationIsApplicable);

                result = getProcessorResponseForIsApplicableException(context, processorLogger, t);
                chainComplete = true;
            }
        }

        log.info("Processor list complete");

        return result;
    }

    private ProcessorResponse getProcessorResponseWhenTerminating(final Logger processorLogger) {
        ProcessorResponse result;
        processorLogger.info("        Terminating, so exiting");
        result = TERMINATE_PROCESSING;
        return result;
    }

    private boolean isProcessorApplicable(final R request,
                                          final C context,
                                          final Processor<R, C> processor,
                                          final Logger processorLogger) {
        final boolean applicable =  processor.isApplicable(request, context);
        processorLogger.info("        Applicable [{}]", applicable);
        return applicable;
    }

    private ProcessorResponse getProcessorResponseTerminatingOnException(final Logger processorLogger) {
        processorLogger.info("        Terminating with this exception");
        return EXCEPTION_THROWN;
    }

    private ProcessorResponse getProcessorResponseForIsApplicableException(final C context,
                                                                           final Logger processorLogger,
                                                                           final Throwable t) {
        context.setThrownException(t);
        processorLogger.info("        Is applicable threw exception, terminating [{}]",
                ExceptionUtils.getStackTrace(t));
        return EXCEPTION_THROWN;
    }
}
