package com.careem.mockingbird.processor

import com.google.devtools.ksp.symbol.KSType

internal fun KSType.fullyQualifiedName() =
    "${this.declaration.qualifiedName?.asString()}"
