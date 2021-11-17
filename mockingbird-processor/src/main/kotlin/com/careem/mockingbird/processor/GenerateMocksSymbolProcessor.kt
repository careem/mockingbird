/*
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.careem.mockingbird.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName

class GenerateMocksSymbolProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    @OptIn(KotlinPoetKspPreview::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("KSP process")


        val symbols = resolver.getSymbolsWithAnnotation("com.careem.mockingbird.test.GenerateMocksFor")
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(GenerateMocksVisitor(), Unit) }
        return ret
    }


    @OptIn(KotlinPoetKspPreview::class)
    inner class GenerateMocksVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {

            val className = classDeclaration.toClassName()
            logger.warn("Detected class: $className")

            // TODO complete generate here

            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//            val parent = function.parentDeclaration as KSClassDeclaration
//            val packageName = parent.containingFile!!.packageName.asString()
//            val className = "${parent.simpleName.asString()}Builder"
//            val file = codeGenerator.createNewFile(Dependencies(true, function.containingFile!!), packageName , className)
//            file.appendText("package $packageName\n\n")
//            file.appendText("import HELLO\n\n")
//            file.appendText("class $className{\n")
//            function.parameters.forEach {
//                val name = it.name!!.asString()
//                val typeName = StringBuilder(it.type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>")
//                val typeArgs = it.type.element!!.typeArguments
//                if (it.type.element!!.typeArguments.isNotEmpty()) {
//                    typeName.append("<")
//                    typeName.append(
//                        typeArgs.map {
//                            val type = it.type?.resolve()
//                            "${it.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
//                                    if (type?.nullability == Nullability.NULLABLE) "?" else ""
//                        }.joinToString(", ")
//                    )
//                    typeName.append(">")
//                }
//                file.appendText("    private var $name: $typeName? = null\n")
//                file.appendText("    internal fun with${name.capitalize()}($name: $typeName): $className {\n")
//                file.appendText("        this.$name = $name\n")
//                file.appendText("        return this\n")
//                file.appendText("    }\n\n")
//            }
//            file.appendText("    internal fun build(): ${parent.qualifiedName!!.asString()} {\n")
//            file.appendText("        return ${parent.qualifiedName!!.asString()}(")
//            file.appendText(
//                function.parameters.map {
//                    "${it.name!!.asString()}!!"
//                }.joinToString(", ")
//            )
//            file.appendText(")\n")
//            file.appendText("    }\n")
//            file.appendText("}\n")
//            file.close()
        }
    }
}