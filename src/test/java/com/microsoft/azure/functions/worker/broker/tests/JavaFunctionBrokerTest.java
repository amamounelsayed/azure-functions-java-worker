package com.microsoft.azure.functions.worker.broker.tests;

import com.microsoft.azure.functions.rpc.messages.InvocationRequest;
import com.microsoft.azure.functions.rpc.messages.ParameterBinding;
import com.microsoft.azure.functions.rpc.messages.TypedData;
import com.microsoft.azure.functions.worker.broker.JavaFunctionBroker;
import com.microsoft.azure.functions.worker.reflect.DefaultClassLoaderProvider;
import mockit.*;
import org.junit.Test;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class JavaFunctionBrokerTest {

    @Test
    public void getTriggerMetadataMap_success(
            @Mocked InvocationRequest request,
            @Mocked ParameterBinding binding,
            @Mocked TypedData bindingData,
            @Mocked TypedData name,
            @Mocked TypedData query,
            @Mocked TypedData headers,
            @Mocked TypedData sys
            ) throws Exception {

        String expectedData = "http {\n  method: \"GET\"\n  url: \"https://localhost:5001/api/HttpExample?name=ushio\"\n  headers {\n    key: \"cache-control\"\n    value: \"max-age=0\"\n  }\n  headers {\n    key: \"connection\"\n    value: \"Keep-Alive\"\n  }\n  headers {\n    key: \"accept\"\n    value: \"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\"\n  }\n  headers {\n    key: \"accept-encoding\"\n    value: \"gzip, deflate, br\"\n  }\n  headers {\n    key: \"accept-language\"\n    value: \"en-US,ja;q=0.8,en-GB;q=0.5,en;q=0.3\"\n  }\n  headers {\n    key: \"host\"\n    value: \"localhost:5001\"\n  }\n  headers {\n    key: \"user-agent\"\n    value: \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19041\"\n  }\n  headers {\n    key: \"upgrade-insecure-requests\"\n    value: \"1\"\n  }\n  query {\n    key: \"name\"\n    value: \"ushio\"\n  }\n  identities {\n    name_claim_type {\n      value: \"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name\"\n    }\n    role_claim_type {\n      value: \"http://schemas.microsoft.com/ws/2008/06/identity/claims/role\"\n    }\n  }\n}\n";
        String expectedName = "req";
        new Expectations() {{
            bindingData.hasHttp(); result = true;
            bindingData.getString(); result = expectedData;
            binding.getName(); result = expectedName;
            binding.getData(); result = bindingData;
            request.getInputDataList(); result = Arrays.asList(binding);

            name.getString(); result = "string: \"John\"\n"; minTimes = 0;
            query.getString(); result = "json: \"{\"name\":\"ushio\"}\""; minTimes = 0;
            headers.getString(); result = "json: \"{\"Cache-Control\":\"max-age=0\",\"Connection\":\"Keep-Alive\",\"Accept\":\"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\",\"Accept-Encoding\":\"gzip, deflate, br\",\"Accept-Language\":\"en-US,ja;q=0.8,en-GB;q=0.5,en;q=0.3\",\"Host\":\"localhost:5001\",\"User-Agent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19041\",\"Upgrade-Insecure-Requests\":\"1\"}\""; minTimes = 0;
            sys.getString(); result = "json: \"{\"MethodName\":\"HttpExample\",\"UtcNow\":\"2020-04-30T15:26:57.281277Z\",\"RandGuid\":\"cd332c4a-df9e-415a-acd4-973994072e46\"}\""; minTimes = 0;

            Map<String,TypedData> triggerMetadata = new HashMap<String, TypedData>();
            triggerMetadata.put("name", name);
            triggerMetadata.put("Query", query);
            triggerMetadata.put("Headers", headers);
            triggerMetadata.put("sys", sys);
            request.getTriggerMetadataMap(); result = Collections.unmodifiableMap(triggerMetadata);
        }};

        JavaFunctionBroker broker = new JavaFunctionBroker(new DefaultClassLoaderProvider());
        Map<String, TypedData> actualTriggerMetadata = broker.getTriggerMetadataMap(request);
        TypedData actual = actualTriggerMetadata.get("$request");
        assertEquals(actual.getString(), expectedData);
        TypedData actual2 = actualTriggerMetadata.get(expectedName);
        assertEquals(actual2.getString(), expectedData);
    }
}
