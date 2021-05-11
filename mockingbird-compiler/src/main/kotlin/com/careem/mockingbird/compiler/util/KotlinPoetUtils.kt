package com.careem.mockingbird.compiler.util

import com.careem.mockingbird.compiler.MockingBirdGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.KotlinLookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.FROM_BACKEND
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.parents
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes
import java.io.ByteArrayOutputStream

internal val publishedApiFqName = FqName(PublishedApi::class.java.canonicalName)
internal val jvmSuppressWildcardsFqName = FqName(JvmSuppressWildcards::class.java.canonicalName)

private val kotlinAnnotations = listOf(jvmSuppressWildcardsFqName, publishedApiFqName)

internal fun FqName.safePackageString(
  dotPrefix: Boolean = false,
  dotSuffix: Boolean = true
): String =
  if (isRoot) {
    ""
  } else {
    val prefix = if (dotPrefix) "." else ""
    val suffix = if (dotSuffix) "." else ""
    "$prefix$this$suffix"
  }

internal fun KtClassOrObject.asClassName(): ClassName =
  ClassName(
    packageName = containingKtFile.packageFqName.safePackageString(),
    simpleNames = parentsWithSelf
      .filterIsInstance<KtClassOrObject>()
      .map { it.nameAsSafeName.asString() }
      .toList()
      .reversed()
  )

internal fun ClassDescriptor.asClassName(): ClassName =
  ClassName(
    packageName = parents.filterIsInstance<PackageFragmentDescriptor>().first()
      .fqName.safePackageString(),
    simpleNames = parentsWithSelf.filterIsInstance<ClassDescriptor>()
      .map { it.name.asString() }
      .toList()
      .reversed()
  )

internal fun ModuleDescriptor.findClassOrTypeAlias(
  packageName: FqName,
  className: String
): ClassifierDescriptorWithTypeParameters? {
  resolveClassByFqName(FqName("${packageName.safePackageString()}$className"), FROM_BACKEND)
    ?.let { return it }

  findTypeAliasAcrossModuleDependencies(ClassId(packageName, Name.identifier(className)))
    ?.let { return it }

  return null
}

internal fun FqName.asClassName(module: ModuleDescriptor): ClassName {
  val segments = pathSegments().map { it.asString() }

  // If the first sentence case is not the last segment of the path it becomes ambiguous,
  // for example, com.Foo.Bar could be a inner class Bar or an unconventional package com.Foo.
  val canGuessClassName = segments.indexOfFirst { it[0].isUpperCase() } == segments.size - 1
  if (canGuessClassName) {
    return ClassName.bestGuess(asString())
  }

  for (index in (segments.size - 1) downTo 1) {
    val packageSegments = segments.subList(0, index)
    val classSegments = segments.subList(index, segments.size)

    val classifier = module.findClassOrTypeAlias(
      packageName = FqName.fromSegments(packageSegments),
      className = classSegments.joinToString(separator = ".")
    )

    if (classifier != null) {
      return ClassName(
        packageName = packageSegments.joinToString(separator = "."),
        simpleNames = classSegments
      )
    }
  }

  throw IllegalStateException("Couldn't parse ClassName for $this.")
}

internal fun PsiElement.fqNameOrNull(
  module: ModuleDescriptor
): FqName? {
  // Usually it's the opposite way, the require*() method calls the nullable method. But in this
  // case we'd like to preserve the better error messages in case something goes wrong.
  return try {
    requireFqName(module)
  } catch (e: IllegalStateException) {
    null
  }
}

private fun PsiElement.findFqNameInSuperTypes(
  module: ModuleDescriptor,
  classReference: String
): FqName? {
  fun tryToResolveClassFqName(outerClass: FqName): FqName? =
    module
      .resolveClassByFqName(FqName("$outerClass.$classReference"), FROM_BACKEND)
      ?.fqNameSafe

  return parents.filterIsInstance<KtClassOrObject>()
    .flatMap { clazz ->
      tryToResolveClassFqName(clazz.requireFqName())?.let { return@flatMap sequenceOf(it) }

      // At this point we can't work with Psi APIs anymore. We need to resolve the super types
      // and try to find inner class in them.
      val descriptor = clazz.requireClassDescriptor(module)
      listOf(descriptor.defaultType).getAllSuperTypes()
        .mapNotNull { tryToResolveClassFqName(it) }
    }
    .firstOrNull()
}

internal fun PsiElement.requireFqName(
  module: ModuleDescriptor
): FqName {
  val containingKtFile = parentsWithSelf
    .filterIsInstance<KtPureElement>()
    .first()
    .containingKtFile

  fun failTypeHandling(): Nothing = throw IllegalStateException(
    "Don't know how to handle Psi element: $text"
  )

  val classReference = when (this) {
    // If a fully qualified name is used, then we're done and don't need to do anything further.
    // An inner class reference like Abc.Inner is also considered a KtDotQualifiedExpression in
    // some cases.
    is KtDotQualifiedExpression -> {
      module
        .resolveClassByFqName(FqName(text), KotlinLookupLocation(this))
        ?.let { return it.fqNameSafe }
        ?: text
    }
    is KtNameReferenceExpression -> getReferencedName()
    is KtUserType -> {
      val isGenericType = children.any { it is KtTypeArgumentList }
      if (isGenericType) {
        // For an expression like Lazy<Abc> the qualifier will be null. If the qualifier exists,
        // then it may refer to the package and the referencedName refers to the class name, e.g.
        // a KtUserType "abc.def.GenericType<String>" has three children: a qualifier "abc.def",
        // the referencedName "GenericType" and the KtTypeArgumentList.
        val qualifierText = qualifier?.text
        val className = referencedName

        if (qualifierText != null) {

          // The generic might be fully qualified. Try to resolve it and return early.
          module
            .resolveClassByFqName(FqName("$qualifierText.$className"), FROM_BACKEND)
            ?.let { return it.fqNameSafe }

          // If the name isn't fully qualified, then it's something like "Outer.Inner".
          // We can't use `text` here because that includes the type parameter(s).
          "$qualifierText.$className"
        } else {
          className ?: failTypeHandling()
        }
      } else {
        val text = text

        // Sometimes a KtUserType is a fully qualified name. Give it a try and return early.
        if (text.contains(".") && text[0].isLowerCase()) {
          module
            .resolveClassByFqName(FqName(text), FROM_BACKEND)
            ?.let { return it.fqNameSafe }
        }

        // We can't use referencedName here. For inner classes like "Outer.Inner" it would only
        // return "Inner", whereas text returns "Outer.Inner", what we expect.
        text
      }
    }
    is KtTypeReference -> {
      val children = children
      if (children.size == 1) {
        try {
          // Could be a KtNullableType or KtUserType.
          return children[0].requireFqName(module)
        } catch (e: IllegalStateException) {
          // Fallback to the text representation.
          text
        }
      } else {
        text
      }
    }
    is KtNullableType -> return innerType?.requireFqName(module) ?: failTypeHandling()
    is KtAnnotationEntry -> return typeReference?.requireFqName(module) ?: failTypeHandling()
    else -> failTypeHandling()
  }

  // E.g. OuterClass.InnerClass
  val classReferenceOuter = classReference.substringBefore(".")

  val importPaths = containingKtFile.importDirectives.mapNotNull { it.importPath }

  // First look in the imports for the reference name. If the class is imported, then we know the
  // fully qualified name.
  importPaths
    .filter { it.alias == null && it.fqName.shortName().asString() == classReference }
    .also { matchingImportPaths ->
      when {
        matchingImportPaths.size == 1 ->
          return matchingImportPaths[0].fqName
        matchingImportPaths.size > 1 ->
          return matchingImportPaths.first { importPath ->
            module.resolveClassByFqName(importPath.fqName, FROM_BACKEND) != null
          }.fqName
      }
    }

  importPaths
    .filter { it.alias == null && it.fqName.shortName().asString() == classReferenceOuter }
    .also { matchingImportPaths ->
      when {
        matchingImportPaths.size == 1 ->
          return FqName("${matchingImportPaths[0].fqName.parent()}.$classReference")
        matchingImportPaths.size > 1 ->
          return matchingImportPaths.first { importPath ->
            val fqName = FqName("${importPath.fqName.parent()}.$classReference")
            module.resolveClassByFqName(fqName, FROM_BACKEND) != null
          }.fqName
      }
    }

  // If there is no import, then try to resolve the class with the same package as this file.
  module.findClassOrTypeAlias(containingKtFile.packageFqName, classReference)
    ?.let { return it.fqNameSafe }

  // If this doesn't work, then maybe a class from the Kotlin package is used.
  module.resolveClassByFqName(FqName("kotlin.$classReference"), FROM_BACKEND)
    ?.let { return it.fqNameSafe }

  // If this doesn't work, then maybe a class from the Kotlin collection package is used.
  module.resolveClassByFqName(FqName("kotlin.collections.$classReference"), FROM_BACKEND)
    ?.let { return it.fqNameSafe }

  // If this doesn't work, then maybe a class from the Kotlin jvm package is used.
  module.resolveClassByFqName(FqName("kotlin.jvm.$classReference"), FROM_BACKEND)
    ?.let { return it.fqNameSafe }

  // Or java.lang.
  module.resolveClassByFqName(FqName("java.lang.$classReference"), FROM_BACKEND)
    ?.let { return it.fqNameSafe }

  findFqNameInSuperTypes(module, classReference)
    ?.let { return it }

  containingKtFile.importDirectives
    .asSequence()
    .filter { it.isAllUnder }
    .mapNotNull {
      // This fqName is the everything in front of the star, e.g. for "import java.io.*" it
      // returns "java.io".
      it.importPath?.fqName
    }
    .forEach { importFqName ->
      module.findClassOrTypeAlias(importFqName, classReference)?.let { return it.fqNameSafe }
    }

  // Check if it's a named import.
  containingKtFile.importDirectives
    .firstOrNull { classReference == it.importPath?.importedName?.asString() }
    ?.importedFqName
    ?.let { return it }

  // Everything else isn't supported.
  throw IllegalStateException(
    "Couldn't resolve FqName $classReference for Psi element: $text"
  )
}

internal fun KtTypeReference.requireTypeName(
  module: ModuleDescriptor
): TypeName {
  fun PsiElement.fail(): Nothing = throw IllegalStateException("Couldn't resolve type: $text")

  fun KtTypeElement.requireTypeName(): TypeName {
    return when (this) {
      is KtUserType -> {
        val className = fqNameOrNull(module)?.asClassName(module)
          ?: if (isTypeParameter()) {
            val bounds = findExtendsBound().map { it.asClassName(module) }
            return TypeVariableName(text, bounds)
          } else {
            throw IllegalStateException("Couldn't resolve fqName.")
          }

        val typeArgumentList = typeArgumentList
        if (typeArgumentList != null) {
          className.parameterizedBy(
            typeArgumentList.arguments.map { typeProjection ->
              if (typeProjection.projectionKind == KtProjectionKind.STAR) {
                STAR
              } else {
                val typeReference = typeProjection.typeReference ?: typeProjection.fail()
                typeReference
                  .requireTypeName(module)
                  .let { typeName ->
                    // Preserve annotations, e.g. List<@JvmSuppressWildcards Abc>.
                    if (typeReference.annotationEntries.isNotEmpty()) {
                      typeName.copy(
                        annotations = typeName.annotations + typeReference.annotationEntries
                          .map { annotationEntry ->
                            AnnotationSpec
                              .builder(
                                annotationEntry
                                  .requireFqName(module)
                                  .asClassName(module)
                              )
                              .build()
                          }
                      )
                    } else {
                      typeName
                    }
                  }
                  .let { typeName ->
                    val modifierList = typeProjection.modifierList
                    when {
                      modifierList == null -> typeName
                      modifierList.hasModifier(KtTokens.OUT_KEYWORD) ->
                        WildcardTypeName.producerOf(typeName)
                      modifierList.hasModifier(KtTokens.IN_KEYWORD) ->
                        WildcardTypeName.consumerOf(typeName)
                      else -> typeName
                    }
                  }
              }
            }
          )
        } else {
          className
        }
      }
      is KtFunctionType ->
        LambdaTypeName.get(
          receiver = receiver?.typeReference?.requireTypeName(module),
          parameters = parameterList
            ?.parameters
            ?.map { parameter ->
              val parameterReference = parameter.typeReference ?: parameter.fail()
              ParameterSpec.unnamed(parameterReference.requireTypeName(module))
            }
            ?: emptyList(),
          returnType = (returnTypeReference ?: fail())
            .requireTypeName(module)
        )
      is KtNullableType -> {
        (innerType ?: fail()).requireTypeName().copy(nullable = true)
      }
      else -> fail()
    }
  }

  return (typeElement ?: fail()).requireTypeName()
}

fun KotlinType.asTypeName(): TypeName {
  if (isTypeParameter()) return TypeVariableName(toString())

  val className = classDescriptorForType().asClassName()
  if (arguments.isEmpty()) return className.copy(nullable = isMarkedNullable)

  val argumentTypeNames = arguments.map { typeProjection ->
    if (typeProjection.isStarProjection) {
      STAR
    } else {
      typeProjection.type.asTypeName()
    }
  }

  return className.parameterizedBy(argumentTypeNames).copy(nullable = isMarkedNullable)
}

// When the Kotlin type is of the form: KClass<OurType>.
internal fun KotlinType.argumentType(): KotlinType = arguments.first().type

internal fun KotlinType.classDescriptorForType() = DescriptorUtils.getClassDescriptorForType(this)

internal data class Parameter(
  val name: String,
  val typeName: TypeName,
  val isWrappedInProvider: Boolean,
  val isWrappedInLazy: Boolean
)

fun FileSpec.Builder.addGeneratedByComment() {
  addComment(
    """
      Generated by ${MockingBirdGenerator::class.simpleName} 
  """.trimIndent()
  )
}

fun FileSpec.writeToString(): String {
  val stream = ByteArrayOutputStream()
  stream.writer().use {
    writeTo(it)
  }
  return stream.toString()
}

private fun FileSpec.Builder.suppressWarnings() {
  addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"DEPRECATION\"").build())
}

fun FileSpec.Companion.buildFile(
  packageName: String,
  fileName: String,
  block: FileSpec.Builder.() -> Unit
): FileSpec =
  builder(packageName, fileName)
    .apply {
      // Suppress any deprecation warnings.
      suppressWarnings()
      block()
    }
    .build()

fun KtParameter.asTypeName(bindingContext: BindingContext): TypeName? {
  return getType(bindingContext)?.asTypeName()
}

internal fun KtFile.classesAndInnerClasses(): Sequence<KtClassOrObject> {
  val children = findChildrenByClass(KtClassOrObject::class.java)

  return generateSequence(children.toList()) { list ->
    list
      .flatMap {
        it.declarations.filterIsInstance<KtClassOrObject>()
      }
      .ifEmpty { null }
  }.flatMap { it.asSequence() }
}

internal fun KtNamedDeclaration.requireFqName(): FqName = requireNotNull(fqName) {
  "fqName was null for $this, $nameAsSafeName"
}

internal fun KtAnnotated.isInterface(): Boolean = this is KtClass && this.isInterface()


internal fun KtClassOrObject.functions(
  includeCompanionObjects: Boolean
): List<KtNamedFunction> = classBodies(includeCompanionObjects).flatMap { it.functions }

internal fun KtClassOrObject.properties(
  includeCompanionObjects: Boolean
): List<KtProperty> = classBodies(includeCompanionObjects).flatMap { it.properties }

private fun KtClassOrObject.classBodies(includeCompanionObjects: Boolean): List<KtClassBody> {
  val elements = children.toMutableList()
  if (includeCompanionObjects) {
    elements += companionObjects.flatMap { it.children.toList() }
  }
  return elements.filterIsInstance<KtClassBody>()
}

fun KtTypeReference.isNullable(): Boolean = typeElement is KtNullableType

fun KtTypeReference.isGenericType(): Boolean {
  val typeElement = typeElement ?: return false
  val children = typeElement.children

  if (children.size != 2) return false
  return children[1] is KtTypeArgumentList
}

fun KtTypeReference.isFunctionType(): Boolean = typeElement is KtFunctionType

fun KtClassOrObject.isGenericClass(): Boolean = typeParameterList != null

fun KtNamedFunction.returnTypeName(module: ModuleDescriptor) = typeReference?.requireTypeName(module) ?: UNIT

fun KtUserType.isTypeParameter(): Boolean {
  return parents.filterIsInstance<KtClassOrObject>().first().typeParameters.any {
    val typeParameter = it.text.split(":").first().trim()
    typeParameter == text
  }
}

fun KtUserType.findExtendsBound(): List<FqName> {
  return parents.filterIsInstance<KtClassOrObject>()
    .first()
    .typeParameters
    .mapNotNull { it.fqName }
}

fun KtClassOrObject.requireClassDescriptor(module: ModuleDescriptor): ClassDescriptor {
  return module.resolveClassByFqName(requireFqName(), KotlinLookupLocation(this))
    ?: throw IllegalStateException(
      "Couldn't resolve class for ${requireFqName()}."
    )
}

fun FqName.requireClassDescriptor(module: ModuleDescriptor): ClassDescriptor {
  return module.resolveClassByFqName(this, FROM_BACKEND)
    ?: throw IllegalStateException("Couldn't resolve class for $this.")
}

internal fun List<KotlinType>.getAllSuperTypes(): Sequence<FqName> =
  generateSequence(this) { kotlinTypes ->
    kotlinTypes.ifEmpty { null }?.flatMap { it.supertypes() }
  }
    .flatMap { it.asSequence() }
    .map { it.classDescriptorForType().fqNameSafe }

internal fun KtCallableDeclaration.requireTypeReference(): KtTypeReference = typeReference!!

internal fun List<KtCallableDeclaration>.mapToParameter(module: ModuleDescriptor): List<Parameter> =
  mapIndexed { index, parameter ->

    val isWrappedInProvider = false
    val isWrappedInLazy = false

    val paramTypeReference = parameter.requireTypeReference()
    val typeName = when {
      paramTypeReference.isNullable() -> paramTypeReference.requireTypeName(module).copy(nullable = true)
      else -> paramTypeReference.requireTypeName(module)
    }

    Parameter(
      name = parameter.name ?: "param$index",
      typeName = typeName,
      isWrappedInProvider = isWrappedInProvider,
      isWrappedInLazy = isWrappedInLazy
    )
  }


internal fun KtAnnotated.hasAnnotation(fqName: FqName): Boolean {
  return findAnnotation(fqName) != null
}

internal fun KtAnnotated.findAnnotation(fqName: FqName): KtAnnotationEntry? {
  val annotationEntries = annotationEntries
  if (annotationEntries.isEmpty()) return null

  // Look first if it's a Kotlin annotation. These annotations are usually not imported and the
  // remaining checks would fail.
  if (fqName in kotlinAnnotations) {
    annotationEntries.firstOrNull { annotation ->
      val text = annotation.text
      text.startsWith("@${fqName.shortName()}") || text.startsWith("@$fqName")
    }?.let { return it }
  }

  // Check if the fully qualified name is used, e.g. `@dagger.Module`.
  val annotationEntry = annotationEntries.firstOrNull {
    it.text.startsWith("@${fqName.asString()}")
  }
  if (annotationEntry != null) return annotationEntry

  // Check if the simple name is used, e.g. `@Module`.
  val annotationEntryShort = annotationEntries
    .firstOrNull {
      it.shortName == fqName.shortName()
    }
    ?: return null

  val importPaths = containingKtFile.importDirectives.mapNotNull { it.importPath }

  // If the simple name is used, check that the annotation is imported.
  val hasImport = importPaths.any { it.fqName == fqName }
  if (hasImport) return annotationEntryShort

  // Look for star imports and make a guess.
  val hasStarImport = importPaths
    .filter { it.isAllUnder }
    .any {
      fqName.asString().startsWith(it.fqName.asString())
    }
  if (hasStarImport) return annotationEntryShort

  return null
}
