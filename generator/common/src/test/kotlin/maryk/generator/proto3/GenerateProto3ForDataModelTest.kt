package maryk.generator.proto3

import maryk.CompleteMarykModel
import maryk.MarykEnum
import maryk.SimpleMarykModel
import maryk.generator.kotlin.GenerationContext
import maryk.test.shouldBe
import kotlin.test.Test

val generatedProto3ForSimpleMarykModel: String = """
syntax = "proto3";

option java_package = "maryk";

message SimpleMarykModel {
  string value = 1;
}
""".trimIndent()

val generatedProto3ForCompleteMarykModel = """
syntax = "proto3";

option java_package = "maryk";

message CompleteMarykModel {
  message MultiType {
    oneof multi {
      string o1 = 1;
      bool o2 = 2;
    }
  }
  message MultiForKeyType {
    oneof multiForKey {
      string o1 = 1;
      bool o2 = 2;
    }
  }
  enum MarykEnumEmbedded {
    UNKNOWN = 0;
    E1 = 1;
    E2 = 2;
    E3 = 3;
  }
  message MapWithEnumEntry {
    MarykEnumEmbedded key = 1;
    string value = 2;
  }
  string string = 1;
  uint64 number = 2;
  bool boolean = 3;
  MarykEnum enum = 4;
  sint64 date = 5;
  int64 dateTime = 6;
  uint32 time = 7;
  bytes fixedBytes = 8;
  bytes flexBytes = 9;
  bytes reference = 10;
  SimpleMarykModel subModel = 11;
  bytes valueModel = 12;
  repeated string list = 13;
  repeated sint32 set = 14;
  map<sint64, sint32> map = 15;
  MultiType multi = 16;
  bool booleanForKey = 17;
  sint64 dateForKey = 18;
  MultiForKeyType multiForKey = 19;
  MarykEnumEmbedded enumEmbedded = 20;
  repeated MapWithEnumEntry mapWithEnum = 21;
}
""".trimIndent()

class GenerateProto3ForDataModelTest {
    @Test
    fun testDataModelConversion() {
        var output = ""

        CompleteMarykModel.generateProto3Schema(
            "maryk",
            GenerationContext(
                enums = mutableListOf(MarykEnum)
            )
        ) {
            output += it
        }

        output shouldBe generatedProto3ForCompleteMarykModel
    }

    @Test
    fun testSimpleDataModelConversion() {
        var output = ""

        SimpleMarykModel.generateProto3Schema(
            "maryk",
            GenerationContext(
                enums = mutableListOf(MarykEnum)
            )
        ) {
            output += it
        }

        output shouldBe generatedProto3ForSimpleMarykModel
    }
}