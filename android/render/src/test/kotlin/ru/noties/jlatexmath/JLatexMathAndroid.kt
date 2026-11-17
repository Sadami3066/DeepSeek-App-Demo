package ru.noties.jlatexmath

import android.content.Context
import android.graphics.Typeface
import java.io.InputStream

object JLatexMathTestResources {
    private val resourceRoot = "jlatexmath"

    fun open(path: String?): InputStream? {
        if (path == null) return null
        return JLatexMathTestResources::class.java.classLoader?.getResourceAsStream("$resourceRoot/$path")
    }
}

class JLatexMathAndroid {
    companion object {
        private val fakeTypeface: Typeface by lazy { allocateTypeface() }

        private fun allocateTypeface(): Typeface {
            val unsafeClass = Class.forName("sun.misc.Unsafe")
            val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
            unsafeField.isAccessible = true
            val unsafe = unsafeField.get(null)
            val allocate = unsafeClass.getMethod("allocateInstance", Class::class.java)
            return allocate.invoke(unsafe, Typeface::class.java) as Typeface
        }

        @JvmStatic
        fun init(context: Context?) {
        }

        @JvmStatic
        fun getResourceAsStream(name: String?): InputStream? = JLatexMathTestResources.open(name)

        @JvmStatic
        fun loadTypeface(name: String?): Typeface = fakeTypeface
    }
}
