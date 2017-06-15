package org.dataportabilityproject.serviceProviders.rememberTheMilk;

import com.google.common.collect.ImmutableList;
import org.dataportabilityproject.dataModels.DataModel;
import org.dataportabilityproject.dataModels.Exporter;
import org.dataportabilityproject.dataModels.Importer;
import org.dataportabilityproject.jobDataCache.JobDataCacheImpl;
import org.dataportabilityproject.shared.IOInterface;
import org.dataportabilityproject.shared.PortableDataType;
import org.dataportabilityproject.shared.Secrets;
import org.dataportabilityproject.shared.ServiceProvider;

import java.io.IOException;
import org.dataportabilityproject.shared.auth.AuthData;

/**
 * The {@link ServiceProvider} for the Rembmer the Milk service (https://www.rememberthemilk.com/).
 */
public final class RememberTheMilkProvider implements ServiceProvider {
    private final Secrets secrets;
    private final IOInterface ioInterface;
    private RememberTheMilkTaskService taskService;

    public RememberTheMilkProvider(Secrets secrets, IOInterface ioInterface) throws IOException {
        this.secrets = secrets;
        this.ioInterface = ioInterface;
    }

    @Override public String getName() {
        return "Remember the Milk";
    }

    @Override
    public ImmutableList<PortableDataType> getExportTypes() {
        return ImmutableList.of(PortableDataType.TASKS);
    }

    @Override
    public ImmutableList<PortableDataType> getImportTypes() {
        return ImmutableList.of(PortableDataType.TASKS);
    }

    @Override public Exporter<? extends DataModel> getExporter(PortableDataType type) throws IOException {
        if (type != PortableDataType.TASKS) {
            throw new IllegalArgumentException("Type " + type + " is not supported");
        }

        return getInstanceOfService();
    }

    @Override public Importer<? extends DataModel> getImporter(PortableDataType type) throws IOException {
        if (type != PortableDataType.TASKS) {
            throw new IllegalArgumentException("Type " + type + " is not supported");
        }

        return getInstanceOfService();
    }

    private synchronized RememberTheMilkTaskService getInstanceOfService() throws IOException {
        if (null == taskService) {
            TokenGenerator tokenGenerator = new TokenGenerator(
                new RememberTheMilkSignatureGenerator(
                    secrets.get("RTM_API_KEY"),
                    secrets.get("RTM_SECRET"),
                    null
                ));
            AuthData authData = tokenGenerator.generateAuthData(ioInterface);
            RememberTheMilkSignatureGenerator signer = new RememberTheMilkSignatureGenerator(
                secrets.get("RTM_API_KEY"),
                secrets.get("RTM_SECRET"),
                tokenGenerator.getToken(authData));

            taskService = new RememberTheMilkTaskService(signer, new JobDataCacheImpl());
        }
        return taskService;
    }
}