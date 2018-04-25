# Reference
Property representing a Reference to another DataObject.

- Maryk Yaml Definition: `Reference`
- Kotlin Definition: `ReferenceDefinition<T>` In which T is the DataModel name
- Kotlin Value : `Key<T>` In which T is the DataModel

## Usage options
- Value
- Map key or value
- Inside List/Set

## Validation Options
- `required` - default true
- `final` - default false
- `unique` - default false
- `minValue` - default false. Minimum value
- `maxValue` - default false. Maximum value

## Data options
- `indexed` - default false
- `searchable` - default true
- `dataModel` - Model of DataObjects to be referred to

## Examples

**Example of a Kotlin Reference property definition**
```kotlin
val def = ReferenceDefinition(
    required = true,
    final = true,
    unique = true,
    dataModel = Person
)
```

**Example of a YAML Reference property definition**
```yaml
!Reference
  dataModel: Person
  required: false
  final: true
```

## Storage/Transport Byte representation
The key of the referenced DataObject as bytes. With transport the Length Delimited
wiretype is used

## String representation
Key as base64 value