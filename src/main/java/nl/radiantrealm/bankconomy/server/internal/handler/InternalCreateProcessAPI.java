package nl.radiantrealm.bankconomy.server.internal.handler;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.processor.ProcessType;
import nl.radiantrealm.bankconomy.processor.Processor;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.FormatUtils;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.concurrent.atomic.AtomicReference;

public class InternalCreateProcessAPI implements RequestHandler {

    @Override
    public void handle(HttpRequest request) throws Exception {
        JsonObject object = Result.nullFunction(request::getRequestBodyAsJson);

        if (object == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing JSON body.");
            return;
        }

        String action = Result.nullFunction(() -> JsonUtils.getJsonString(object, "action"));

        if (action == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing process action.");
            return;
        }

        ProcessType type = Result.nullFunction(() -> FormatUtils.formatEnum(ProcessType.class, action));

        if (type == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST);
            return;
        }

        JsonObject payload = Result.nullFunction(() -> JsonUtils.getJsonObject(object, "payload"));

        if (payload == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing payload body.");
            return;
        }

        ProcessHandler handler = DataObject.fromJson(type.handler, payload);

        if (handler == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Invalid payload body.");
            return;
        }

        AtomicReference<ProcessResult> atomicReference = new AtomicReference<>();
        Processor.createProcess(handler, payload, atomicReference::set);
        request.sendProcessResult(atomicReference.get());
    }
}
