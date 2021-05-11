package com.careem.mockingbird.compiler

import com.careem.mockingbird.compiler.util.*
import com.careem.mockingbird.compiler.util.asClassName
import com.careem.mockingbird.compiler.util.safePackageString
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File

class MockingBirdGenerator(
  private val genSrcDir: File,
  private val bindingContext: BindingContext,
  private val moduleDescriptor: ModuleDescriptor,
  private val messageCollector: MessageCollector
) {

  fun generateMockClassFor(ktFile: KtFile) {
    // read classes in file.kt
    ktFile.children.filterIsInstance(KtClass::class.java)
      .forEach { generateMockClassFor(it, ktFile.packageFqName.safePackageString()) }
  }


  fun generateMockClassFor(ktClass: KtClass, packageName: String) {

    val simpleName = ktClass.name ?: throw IllegalArgumentException("This class has no names. Anonymous? $ktClass")
    val outputDir = genSrcDir

    val mockingBirdMockClass = ClassName("com.careem.mockingbird.test", "Mock")

    // TODO fix package name
    debug("Generating mocks for $packageName. $simpleName")
    val mockClassBuilder = TypeSpec.classBuilder("${simpleName}Mock")
      .addType(ktClass.buildMethodObject())
      .addType(ktClass.buildArgObject())
//      .addType(ktClass.buildPropertyObject())
      .addSuperinterface(ktClass.asClassName()) // TODO check if interface or generic open class
      .addSuperinterface(mockingBirdMockClass) // TODO fix this


    for (function in ktClass.body?.functions ?: emptyList()) {
      this.mockFunction(mockClassBuilder, function)
    }

//    ktClass.properties(false).forEach { property ->
//      this.mockProperty(mockClassBuilder, property)
//    }

    // TODO support properties

    FileSpec.buildFile(packageName, "${simpleName}Mock") {
      addGeneratedByComment()
      addType(mockClassBuilder.build())
    }
      .writeTo(outputDir)
  }


  private fun KtClass.buildMethodObject(): TypeSpec {
    debug("===> Methods")
    val methodObjectBuilder = TypeSpec.objectBuilder(METHOD)
    val visitedFunctionSet = mutableSetOf<String>()
    for (function in body?.functions ?: emptyList()) {
      val functionName = function.name!!
      if (!visitedFunctionSet.contains(functionName)) {
        visitedFunctionSet.add(functionName)
        methodObjectBuilder.addProperty(
          PropertySpec.builder(functionName, String::class)
            .initializer("%S", functionName)
            .addModifiers(KModifier.CONST)
            .build()
        )
      }
    }
    return methodObjectBuilder.build()
  }

  private fun KtClass.buildArgObject(): TypeSpec {
    val argObjectBuilder = TypeSpec.objectBuilder(ARG)
    val visitedPropertySet = mutableSetOf<String>()
    for (function in body?.functions ?: emptyList()) {
      for (arg in function.valueParameters) {
        val argName = arg.name
        if (!visitedPropertySet.contains(argName)) {
          visitedPropertySet.add(argName!!)
          argObjectBuilder.addProperty(
            PropertySpec.builder(argName, String::class)
              .initializer("%S", argName)
              .addModifiers(KModifier.CONST)
              .build()
          )
        }
      }

    }
    return argObjectBuilder.build()
  }

//  TODO
//  private fun KtClass.buildPropertyObject(): TypeSpec {
//    debug("===> Prop")
//    val propertyObjectBuilder = TypeSpec.objectBuilder(PROPERTY)
//    var haveMutableProps = false
//    val visitedPropertySet = mutableSetOf<String>()
//    properties(false).forEach { property ->
//      debug("===> Prop $property")
//      property.getterSignature?.let {
//        handleProperty(it.name, visitedPropertySet, propertyObjectBuilder)
//      }
//
//      property.setterSignature?.let {
//        haveMutableProps = true
//        handleProperty(it.name, visitedPropertySet, propertyObjectBuilder)
//      }
//    }
//
//    if (haveMutableProps) {
//      val setterValueProperty = buildProperty(PROPERTY_SETTER_VALUE)
//      propertyObjectBuilder.addProperty(setterValueProperty)
//    }
//    return propertyObjectBuilder.build()
//  }

  private fun handleProperty(
    name: String,
    visited: MutableSet<String>,
    builder: TypeSpec.Builder
  ) {
    if (!visited.contains(name)) {
      visited.add(name)
      val nameProperty = buildProperty(name)
      builder.addProperty(nameProperty)
    }
  }

  private fun buildProperty(name: String) =
    PropertySpec.builder(name, String::class)
      .initializer("%S", name)
      .addModifiers(KModifier.CONST)
      .build()

  private fun debug(text: String) {
    messageCollector.report(CompilerMessageSeverity.INFO, text)
  }

//  private fun mockProperty(
//    mockClassBuilder: TypeSpec.Builder,
//    property: KtProperty
//  ) {
//    debug("===> Mocking Property ${property.getterSignature?.name} and ${property.setterSignature?.name} and ${property.setterSignature}")
//    val type = extractType(property.returnType)
//
//    val propertyBuilder = PropertySpec
//      .builder(property.name, type, KModifier.OVERRIDE)
//
//    if (property.getterSignature != null) {
//      val getterBuilder = FunSpec.getterBuilder()
//      val mockFunction = MemberName("com.careem.mockingbird.test", MOCK)
//      val getterArgsValue = mutableListOf(
//        mockFunction,
//        MemberName(
//          "",
//          property.getterSignature?.name
//            ?: throw java.lang.IllegalArgumentException("I can't mock this property")
//        )
//      )
//      val getterCodeBlocks = mutableListOf("methodName = $PROPERTY.%M")
//      val getterStatementString = """
//            return %M(
//                ${getterCodeBlocks.joinToString(separator = ",\n")}
//            )
//        """.trimIndent()
//      getterBuilder.addStatement(getterStatementString, *(getterArgsValue.toTypedArray()))
//      propertyBuilder.getter(getterBuilder.build())
//    }
//
//    if (property.setterSignature != null) {
//      val setterBuilder = FunSpec.setterBuilder()
//      val mockUnitFunction = MemberName("com.careem.mockingbird.test", MOCK_UNIT)
//      val setterArgsValue = mutableListOf(
//        mockUnitFunction,
//        MemberName(
//          "",
//          property.setterSignature?.name
//            ?: throw java.lang.IllegalArgumentException("I can't mock this property")
//        ),
//        MemberName("", PROPERTY_SETTER_VALUE),
//        PROPERTY_SETTER_VALUE
//      )
//
//      val v = mutableListOf<String>().apply {
//        add("Property.%M to %L")
//      }
//      val args = v.joinToString(separator = ",")
//      val setterCodeBlocks = mutableListOf("methodName = $PROPERTY.%M")
//      setterCodeBlocks.add("arguments = mapOf($args)")
//      val setterStatementString = """
//            return %M(
//                ${setterCodeBlocks.joinToString(separator = ",\n")}
//            )
//        """.trimIndent()
//      setterBuilder
//        .addParameter("value", type)
//        .addStatement(setterStatementString, *(setterArgsValue.toTypedArray()))
//      propertyBuilder
//        .mutable()
//        .setter(setterBuilder.build())
//    }
//
//    mockClassBuilder.addProperty(propertyBuilder.build())
//  }

  private fun mockFunction(
    mockClassBuilder: TypeSpec.Builder,
    function: KtNamedFunction
  ) {
    debug("===> Mocking")
    val funBuilder = FunSpec.builder(function.name!!)
      .addModifiers(KModifier.OVERRIDE)

    for (valueParam in function.valueParameters.mapToParameter(moduleDescriptor)) {
//      debug(valueParam.type)
      debug("adding parameter ${valueParam.name} with type ${valueParam.typeName}")
      funBuilder.addParameter(valueParam.name, valueParam.typeName)
    }

    val returnType = function.returnTypeName(moduleDescriptor)
    if (returnType != UNIT) {
      funBuilder.returns(returnType)
    }

    funBuilder.addMockStatement(function)
    mockClassBuilder.addFunction(
      funBuilder.build()
    )
  }

  private fun FunSpec.Builder.addMockStatement(function: KtNamedFunction) {
    // TODO remove duplicates in args and method names
    val returnType = function.returnTypeName(moduleDescriptor)

    val mockFunction = if (returnType == UNIT) {
      MOCK_UNIT
    } else {
      MOCK
    }
    val mockUnit = MemberName("com.careem.mockingbird.test", mockFunction)
    val v = mutableListOf<String>()
    for (i in function.valueParameters.indices) {
      v.add("Arg.%M to %L")
    }
    val args = v.joinToString(separator = ",")
    val argsValue = mutableListOf<Any>(mockUnit, MemberName("", function.name!!))
    for (vp in function.valueParameters) {
      argsValue.add(MemberName("", vp.name!!))
      argsValue.add(vp.name!!)
    }
    debug(argsValue.joinToString { it.toString() })
    val codeBlocks = mutableListOf("methodName = Method.%M")
    if (args.isNotEmpty()) {
      codeBlocks.add("arguments = mapOf($args)")
    }
    val statementString = """
            return %M(
                ${codeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()

    this.addStatement(statementString, *(argsValue.toTypedArray()))
  }

  companion object {
    private const val METHOD = "Method"
    private const val ARG = "Arg"
    private const val PROPERTY = "Property"
    private const val MOCK_UNIT = "mockUnit"
    private const val MOCK = "mock"
    private const val PROPERTY_SETTER_VALUE = "value"
  }
}
