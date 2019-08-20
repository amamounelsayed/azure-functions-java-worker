package com.microsoft.azure.functions.worker;

import com.microsoft.azure.functions.rpc.messages.StreamingMessage;
import com.microsoft.azure.functions.worker.broker.JavaFunctionBroker;
import com.microsoft.azure.functions.worker.handler.*;
import com.microsoft.azure.functions.worker.reflect.DefaultClassLoaderProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Something {
    public static final Map<StreamingMessage.ContentCase, Supplier<MessageHandler<?, ?>>> handlerSuppliers = Something.addHandlers();

    private static Map addHandlers() {
        DefaultClassLoaderProvider classPathProvider = new DefaultClassLoaderProvider();
        JavaFunctionBroker broker = new JavaFunctionBroker(classPathProvider);
        Map<StreamingMessage.ContentCase, Supplier<MessageHandler<?, ?>>> handlerSuppliers = new HashMap<>();

        handlerSuppliers.put(StreamingMessage.ContentCase.WORKER_INIT_REQUEST, WorkerInitRequestHandler::new);
        handlerSuppliers.put(StreamingMessage.ContentCase.FUNCTION_ENVIRONMENT_RELOAD_REQUEST, () -> new FunctionEnvironmentReloadRequestHandler(broker));
        handlerSuppliers.put(StreamingMessage.ContentCase.FUNCTION_LOAD_REQUEST, () -> new FunctionLoadRequestHandler(broker));
        handlerSuppliers.put(StreamingMessage.ContentCase.INVOCATION_REQUEST, () -> new InvocationRequestHandler(broker));

        return handlerSuppliers;
    }


}
