package com.zzt.kid.plugin.transformer

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName

class EntryHookTransformer: IrElementTransformer<Unit> {

    override fun visitFunction(declaration: IrFunction, data: Unit): IrStatement {
        println("visitFunction:: ${declaration.name}")
        declaration.getAnnotation(FqName("com.zzt.kid.annotation.EntryHook"))?.let {
        }
        return super.visitFunction(declaration, data)
    }
}