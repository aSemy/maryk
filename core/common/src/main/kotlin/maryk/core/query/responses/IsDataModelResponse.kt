package maryk.core.query.responses

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.exceptions.DefNotFoundException
import maryk.core.models.RootObjectDataModel
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualModelReferenceDefinition
import maryk.core.properties.definitions.contextual.DataModelReference
import maryk.core.properties.types.TypedValue
import maryk.core.query.DataModelPropertyContext
import maryk.core.query.responses.statuses.AddSuccess
import maryk.core.query.responses.statuses.AlreadyExists
import maryk.core.query.responses.statuses.AuthFail
import maryk.core.query.responses.statuses.DoesNotExist
import maryk.core.query.responses.statuses.RequestFail
import maryk.core.query.responses.statuses.ServerFail
import maryk.core.query.responses.statuses.StatusType
import maryk.core.query.responses.statuses.Success
import maryk.core.query.responses.statuses.ValidationFail

/** A response for a data operation on a DataModel */
interface IsDataModelResponse<DO: Any, out DM: RootObjectDataModel<DO, *>>{
    val dataModel: DM

    companion object {
        internal fun <DM: Any> addDataModel(definitions: ObjectPropertyDefinitions<DM>, getter: (DM) -> RootObjectDataModel<*, *>?) {
            definitions.add(0, "dataModel",
                ContextualModelReferenceDefinition<RootObjectDataModel<*, *>, DataModelPropertyContext>(
                    contextualResolver = { context, name ->
                        context?.let {
                            @Suppress("UNCHECKED_CAST")
                            it.dataModels[name] as (() -> RootObjectDataModel<*, *>)? ?: throw DefNotFoundException("ObjectDataModel of name $name not found on dataModels")
                        } ?: throw ContextNotFoundException()
                    }
                ),
                getter = getter,
                toSerializable = { value: RootObjectDataModel<*, *>?, _ ->
                    value?.let{
                        DataModelReference(it.name){ it }
                    }
                },
                fromSerializable = { it?.get?.invoke() },
                capturer = { context, value ->
                    @Suppress("UNCHECKED_CAST")
                    context.dataModel = value.get() as RootObjectDataModel<Any, ObjectPropertyDefinitions<Any>>
                }
            )
        }
        internal fun <DM: Any> addStatuses(definitions: ObjectPropertyDefinitions<DM>, getter: (DM) -> List<TypedValue<StatusType, *>>?){
            definitions.add(1, "statuses", listOfStatuses, getter)
        }
    }
}

private val listOfStatuses = ListDefinition(
    valueDefinition = MultiTypeDefinition(
        typeEnum = StatusType,
        definitionMap = mapOf(
            StatusType.SUCCESS to EmbeddedObjectDefinition(dataModel = {  Success } ),
            StatusType.ADD_SUCCESS to EmbeddedObjectDefinition(dataModel = {  AddSuccess } ),
            StatusType.AUTH_FAIL to EmbeddedObjectDefinition(dataModel = {  AuthFail } ),
            StatusType.REQUEST_FAIL to EmbeddedObjectDefinition(dataModel = {  RequestFail } ),
            StatusType.SERVER_FAIL to EmbeddedObjectDefinition(dataModel = {  ServerFail } ),
            StatusType.VALIDATION_FAIL to EmbeddedObjectDefinition(dataModel = {  ValidationFail } ),
            StatusType.ALREADY_EXISTS to EmbeddedObjectDefinition(dataModel = {  AlreadyExists } ),
            StatusType.DOES_NOT_EXIST to EmbeddedObjectDefinition(dataModel = {  DoesNotExist } )
        )
    )
)
