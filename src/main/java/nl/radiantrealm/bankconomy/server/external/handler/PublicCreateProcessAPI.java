package nl.radiantrealm.bankconomy.server.external.handler;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.processor.ProcessType;
import nl.radiantrealm.bankconomy.processor.Processor;
import nl.radiantrealm.bankconomy.server.external.PublicRequestHandler;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.FormatUtils;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PublicCreateProcessAPI implements PublicRequestHandler {

    @Override
    public void handle(HttpRequest request, UUID playerUUID, JsonObject object) throws Exception {
        String action = Result.nullFunction(() -> JsonUtils.getJsonString(object, "action"));

        if (action == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing process action.");
            return;
        }

        ProcessType type = Result.nullFunction(() -> FormatUtils.formatEnum(ProcessType.class, action.toUpperCase()));

        if (type == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Unknown process type.");
            return;
        }

        if (!processWhitelist(type)) {
            request.sendStatusResponse(StatusCode.FORBIDDEN, "Process type not allowed.");
            return;
        }

        JsonObject payload = Result.nullFunction(() -> JsonUtils.getJsonObject(object, "payload"));

        if (payload == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing payload body.");
            return;
        }

        ProcessHandler handler = Result.nullFunction(() -> DataObject.fromJson(type.handler, payload));

        if (handler == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Invalid payload body.");
            return;
        }

        AtomicReference<ProcessResult> atomicReference = new AtomicReference<>();
        Processor.createProcess(handler, payload, atomicReference::set);
        request.sendProcessResult(atomicReference.get());
    }

    private boolean processWhitelist(ProcessType type) {
        return switch (type) {
            case CREATE_SAVINGS_ACCOUNT,
                 UPDATE_SAVINGS_NAME,
                 DELETE_SAVINGS_ACCOUNT,
                 CREATE_TRANSACTION -> true;

            default -> false;
        };
    }
}
