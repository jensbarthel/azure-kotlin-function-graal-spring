package com.example.hello.infra

import com.pulumi.Context
import com.pulumi.asset.FileArchive
import com.pulumi.azurenative.resources.ResourceGroup
import com.pulumi.azurenative.storage.Blob
import com.pulumi.azurenative.storage.BlobArgs
import com.pulumi.azurenative.storage.BlobContainer
import com.pulumi.azurenative.storage.BlobContainerArgs
import com.pulumi.azurenative.storage.StorageAccount
import com.pulumi.azurenative.storage.StorageAccountArgs
import com.pulumi.azurenative.storage.StorageFunctions
import com.pulumi.azurenative.storage.enums.HttpProtocol
import com.pulumi.azurenative.storage.enums.Kind
import com.pulumi.azurenative.storage.enums.Permissions
import com.pulumi.azurenative.storage.enums.SignedResource
import com.pulumi.azurenative.storage.enums.SkuName
import com.pulumi.azurenative.storage.inputs.ListStorageAccountKeysArgs
import com.pulumi.azurenative.storage.inputs.ListStorageAccountServiceSASArgs
import com.pulumi.azurenative.storage.inputs.SkuArgs
import com.pulumi.azurenative.web.AppServicePlan
import com.pulumi.azurenative.web.AppServicePlanArgs
import com.pulumi.azurenative.web.WebApp
import com.pulumi.azurenative.web.WebAppArgs
import com.pulumi.azurenative.web.enums.FtpsState
import com.pulumi.azurenative.web.enums.SupportedTlsVersions
import com.pulumi.azurenative.web.inputs.NameValuePairArgs
import com.pulumi.azurenative.web.inputs.SiteConfigArgs
import com.pulumi.azurenative.web.inputs.SkuDescriptionArgs
import com.pulumi.core.Output
import java.io.File


class Stack {
    fun provision(ctx: Context) {
        // Create a separate resource group for this example.
        val resourceGroup = ResourceGroup("linux-fn-rg")

        // Storage account is required by Function App.
        // Also, we will upload the function code to the same storage account.
        val storageAccount = StorageAccount(
            "linux-fn-sa", StorageAccountArgs.builder()
                .accountName("linuxfnsa")
                .resourceGroupName(resourceGroup.name())
                .kind(Kind.StorageV2)
                .sku(
                    SkuArgs.builder()
                        .name(SkuName.Standard_LRS)
                        .build()
                )
                .build()
        )

        // Function code archives will be stored in this container.
        val codeContainer = BlobContainer(
            "zips", BlobContainerArgs.builder()
                .resourceGroupName(resourceGroup.name())
                .accountName(storageAccount.name())
                .build()
        )

        // Upload Azure Function's code as a zip archive to the storage account.
        val codeBlob = Blob(
            "zip", BlobArgs.builder()
                .resourceGroupName(resourceGroup.name())
                .accountName(storageAccount.name())
                .containerName(codeContainer.name())
                .source(FileArchive(findAppArchive().absolutePath))
                .build()
        )

        // Define a Consumption Plan for the Function App.
        // You can change the SKU to Premium or App Service Plan if needed.
        val plan = AppServicePlan(
            "plan", AppServicePlanArgs.builder()
                .resourceGroupName(resourceGroup.name())
                .kind("Linux")
                .reserved(true) // required for Linux
                .sku(
                    SkuDescriptionArgs.builder()
                        .tier("Dynamic")
                        .name("Y1")
                        .capacity(1)
                        .build()
                )
                .build()
        )

        // Build the connection string and zip archive's SAS URL. They will go to Function App's settings.
        val storageConnectionString: Output<String> = getConnectionString(resourceGroup.name(), storageAccount.name())
        val codeBlobUrl: Output<String> = signedBlobReadUrl(codeBlob, codeContainer, storageAccount, resourceGroup)
        val app = storageConnectionString.applyValue { conn ->
            WebApp(
                "function", WebAppArgs.builder()
                    .resourceGroupName(resourceGroup.name())
                    .serverFarmId(plan.getId())
                    .kind("functionapp,linux,container")
                    .httpsOnly(true)
                    .siteConfig(
                        SiteConfigArgs.builder()
                            .numberOfWorkers(1)
                            .minTlsVersion(SupportedTlsVersions._1_2)
                            .ftpsState(FtpsState.Disabled)
                            .appSettings(
                                NameValuePairArgs.builder().name("AzureWebJobsStorage").value(conn).build(),
                                NameValuePairArgs.builder().name("WEBSITE_RUN_FROM_PACKAGE").value(codeBlobUrl)
                                    .build(),
                                NameValuePairArgs.builder().name("FUNCTIONS_EXTENSION_VERSION").value("~4").build(),
                                NameValuePairArgs.builder().name("FUNCTIONS_WORKER_RUNTIME").value("powershell")
                                    .build()
                            ).build()
                    )
                    .build()
            )
        }
        ctx.export("functionName", app.apply(WebApp::name))
        ctx.export(
            "endpoint", Output.format(
                "https://%s/api/hello",  // 'hello' corresponds to app/src/main/function/hello
                app.apply(WebApp::defaultHostName)
            )
        )
    }

    private fun getConnectionString(resourceGroupName: Output<String?>?, accountName: Output<String?>?): Output<String> {
        // Retrieve the primary storage account key.
        val primaryStorageKey = StorageFunctions.listStorageAccountKeys(
            ListStorageAccountKeysArgs.builder()
                .resourceGroupName(resourceGroupName)
                .accountName(accountName)
                .build()
        )
            .applyValue { r -> r.keys().get(0).value() }

        // Build the connection string to the storage account.
        return Output.format(
            "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
            accountName, primaryStorageKey
        )
    }

    fun signedBlobReadUrl(
        blob: Blob, container: BlobContainer, account: StorageAccount, resourceGroup: ResourceGroup
    ): Output<String> {
        val blobSASServiceSasToken = StorageFunctions.listStorageAccountServiceSAS(
            ListStorageAccountServiceSASArgs.builder()
                .resourceGroupName(resourceGroup.name())
                .accountName(account.name())
                .protocols(HttpProtocol.Https)
                .sharedAccessExpiryTime("2030-01-01")
                .sharedAccessStartTime("2021-01-01")
                .resource(SignedResource.C)
                .permissions(Permissions.R)
                .canonicalizedResource(Output.format("/blob/%s/%s", account.name(), container.name()))
                .contentType("application/json")
                .cacheControl("max-age=5")
                .contentDisposition("inline")
                .contentEncoding("deflate")
                .build()
        )
            .applyValue { sas -> sas.serviceSasToken() }
        return Output.format(
            "https://%s.blob.core.windows.net/%s/%s?$%s",
            account.name(), container.name(), blob.name(), blobSASServiceSasToken
        )
    }

    private fun findAppArchive(): File {
        val files = File("../app/build/dist")
            .listFiles { dir: File?, name: String -> name.endsWith("-app.zip") }
        check(!(files == null || files.size == 0)) {
            "Could not find app archive in `./app/build/dist/*-app.zip`;" +
                    " did you run `gradle app:packageDistribution`?"
        }
        check(files.size <= 1) {
            "Found more than one app archive `./app/build/dist/*-app.zip`;" +
                    " confused which one to use."
        }
        return files[0]
    }
}

