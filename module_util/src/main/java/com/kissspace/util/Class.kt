package com.kissspace.util

import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType

val KType.isTypeString: Boolean
    get() = this.isClass(String::class)

val KType.isTypeInt: Boolean
    get() = this.isClass(Int::class) || this.isClass(java.lang.Integer::class)

val KType.isTypeLong: Boolean
    get() = this.isClass(Long::class) || this.isClass(java.lang.Long::class)

val KType.isTypeByte: Boolean
    get() = this.isClass(Byte::class) || this.isClass(java.lang.Byte::class)

val KType.isTypeShort: Boolean
    get() = this.isClass(Short::class) || this.isClass(java.lang.Short::class)

val KType.isTypeChar: Boolean
    get() = this.isClass(Char::class) || this.isClass(java.lang.Character::class)

val KType.isTypeBoolean: Boolean
    get() = this.isClass(Boolean::class) || this.isClass(java.lang.Boolean::class)

val KType.isTypeFloat: Boolean
    get() = this.isClass(Float::class) || this.isClass(java.lang.Float::class)

val KType.isTypeDouble: Boolean
    get() = this.isClass(Double::class) || this.isClass(java.lang.Double::class)
val KType.isTypeFile: Boolean
    get() = this.isClass(File::class) || this.isClass(java.io.File::class)

fun KType.isClass(cls: KClass<*>): Boolean = this.classifier == cls