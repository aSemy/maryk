package maryk.datastore.rocksdb.model

import maryk.core.exceptions.StorageException
import maryk.core.models.IsRootDataModel
import maryk.core.models.RootDataModel
import maryk.core.models.migration.MigrationStatus
import maryk.core.models.migration.MigrationStatus.NewModel
import maryk.core.models.migration.MigrationStatus.UpToDate
import maryk.core.properties.types.Version
import maryk.core.query.DefinitionsConversionContext
import maryk.rocksdb.ColumnFamilyHandle
import maryk.rocksdb.RocksDB

fun checkModelIfMigrationIsNeeded(
    rocksDB: RocksDB,
    modelColumnFamily: ColumnFamilyHandle,
    dataModel: IsRootDataModel,
    onlyCheckVersion: Boolean
): MigrationStatus {
    val name = rocksDB.get(modelColumnFamily, modelNameKey)?.decodeToString()
    val version = rocksDB.get(modelColumnFamily, modelVersionKey)?.let {
        var readIndex = 0
        Version.Serializer.readFromBytes { it[readIndex++] }
    }

    if (name == null || version == null) {
        return NewModel
    }

    return when {
        dataModel.Meta.version != version || !onlyCheckVersion -> {
            // Read currently stored model
            val modelBytes = rocksDB.get(modelColumnFamily, modelDefinitionKey)
                ?: throw StorageException("Model is unexpectedly missing in metadata for ${dataModel.Meta.name}")

            var readIndex = 0
            val context = DefinitionsConversionContext()
            val storedDataModel = RootDataModel.Model.Serializer.readProtoBuf(modelBytes.size, { modelBytes[readIndex++] }, context).toDataObject()

            // Check by comparing the data models for if migration is needed
            return dataModel.isMigrationNeeded(storedDataModel)
        }
        else -> UpToDate
    }
}
