package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.processor.ProcessType;
import nl.radiantrealm.bankconomy.processor.Processor;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.http.server.ApplicationRouter;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.util.concurrent.atomic.AtomicReference;

public class MinecraftWebserverAPI extends ApplicationRouter {

    public MinecraftWebserverAPI() {
        super(69420);

        register("/create-process", new CreateProcess());
    }

    public class CreateProcess implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            JsonObject object = JsonUtils.getJsonObject(request.getRequestBody());

            ProcessType processType = JsonUtils.getJsonEnum(object, "action", ProcessType.class);

            if (processType == null) {
                request.sendStatusResponse(StatusCode.BAD_REQUEST);
                return;
            }

            if (!processActionWhitelist(processType)) {
                request.sendStatusResponse(StatusCode.UNAUTHORIZED);
                return;
            }

            JsonObject data = JsonUtils.getJsonObject(object, "data");

            if (data == null) {
                request.sendStatusResponse(StatusCode.BAD_REQUEST, "error", "Missinig data body.");
                return;
            }

            ProcessHandler handler = DataObject.fromJson(processType.handler, data);

            AtomicReference<ProcessResult> atomicReference = new AtomicReference<>();
            Processor.createProcess(handler, data, atomicReference::set);
            request.sendProcessResult(atomicReference.get());
        }
    }

    private boolean processActionWhitelist(ProcessType processType) {
        return switch (processType) {
            case UPDATE_PLAYER_NAME,
                 CREATE_SAVINGS_ACCOUNT,
                 UPDATE_SAVINGS_NAME,
                 DELETE_SAVINGS_ACCOUNT,
                 CREATE_TRANSACTION -> true;

            default -> false;
        };
    }
}
