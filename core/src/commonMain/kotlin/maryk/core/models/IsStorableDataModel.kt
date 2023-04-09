package maryk.core.models

import maryk.core.models.definitions.IsNamedDataModelDefinition
import maryk.core.models.migration.MigrationStatus
import maryk.core.models.migration.checkProperties

interface IsStorableDataModel<DO: Any>: IsTypedDataModel<DO> {
    val Meta: IsNamedDataModelDefinition

    /**
     * Checks if a migration is needed between [storedDataModel] and current model and returns a status
     * indicating if the models are compatible.
     * Pass a [migrationReasons] if this method is overridden
     */
    fun isMigrationNeeded(
        storedDataModel: IsStorableDataModel<*>,
        migrationReasons: MutableList<String> = mutableListOf()
    ): MigrationStatus {
        if (storedDataModel.Meta.name != this.Meta.name) {
            migrationReasons += "Names of models did not match: ${storedDataModel.Meta.name} -> ${this.Meta.name}"
        }

        val hasNewProperties = this.checkProperties(storedDataModel) {
            migrationReasons += it
        }

        return when {
            migrationReasons.isNotEmpty() -> MigrationStatus.NeedsMigration(storedDataModel, migrationReasons, null)
            hasNewProperties -> MigrationStatus.OnlySafeAdds
            else -> MigrationStatus.UpToDate
        }
    }
}
