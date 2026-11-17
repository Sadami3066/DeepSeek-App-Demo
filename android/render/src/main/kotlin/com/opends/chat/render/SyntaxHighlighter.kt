package com.opends.chat.render

import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

internal object SyntaxHighlighter {
    private val cache = object : LruCache<String, AnnotatedString>(24) {}

    fun highlight(code: String, language: String, palette: SyntaxPalette, isDark: Boolean): AnnotatedString {
        val spec = LangSpec.resolve(language)
        if (spec.profile == Profile.Plain) return AnnotatedString(code)
        val key = "${if (isDark) 1 else 0}|${language.lowercase()}|${code.hashCode()}|${code.length}"
        cache.get(key)?.let { return it }
        val result = Tokenizer(code, spec, palette).tokenize()
        cache.put(key, result)
        return result
    }
}

private enum class Profile { CLike, Python, JavaScript, Json, Shell, Sql, Markup, Css, Yaml, Plain }

private class LangSpec(
    val profile: Profile,
    val keywords: Set<String>,
    val types: Set<String>,
    val builtins: Set<String>,
    val caseInsensitive: Boolean = false,
    val tripleQuotes: Boolean = false,
    val templateStrings: Boolean = false,
    val annotations: Boolean = false,
    val hashComment: Boolean = false,
    val slashComment: Boolean = true,
    val blockComment: Boolean = true,
    val preprocessor: Boolean = false,
    val nestedBlockComment: Boolean = false,
    val docComment: Boolean = false,
) {
    companion object {
        fun resolve(language: String): LangSpec {
            val key = language.lowercase().trim().removePrefix("language-")
            return when (key) {
                "kotlin", "kt", "kts" -> kotlin
                "java" -> java
                "c" -> c
                "cpp", "c++", "cc", "cxx", "objc", "objectivec", "h", "hpp" -> cpp
                "go", "golang" -> go
                "rust", "rs" -> rust
                "python", "py", "python3" -> python
                "javascript", "js", "jsx", "node", "mjs", "cjs" -> javascript
                "typescript", "ts", "tsx" -> typescript
                "json", "jsonc", "json5" -> json
                "bash", "sh", "shell", "zsh", "fish", "console" -> shell
                "sql", "mysql", "postgresql", "postgres", "sqlite" -> sql
                "xml", "html", "xhtml", "svg", "vue", "plist" -> markup
                "css", "scss", "less" -> css
                "yaml", "yml", "toml", "ini", "properties", "conf" -> yaml
                "diff", "patch" -> diff
                else -> plain
            }
        }

        private val C_BUILTINS = builtins(
            "printf", "fprintf", "sprintf", "snprintf", "malloc", "calloc", "realloc", "free",
            "memcpy", "memset", "memmove", "strlen", "strcmp", "strncmp", "strcpy", "strncpy",
            "strcat", "strstr", "fopen", "fclose", "fread", "fwrite", "fgets", "fputs",
            "exit", "abort", "atoi", "atof", "qsort", "assert", "sizeof", "nullptr",
            "stdin", "stdout", "stderr", "NULL",
        )

        private val kotlin = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
                "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
                "true", "try", "typealias", "typeof", "val", "var", "when", "while", "by", "catch",
                "constructor", "delegate", "dynamic", "field", "file", "finally", "get", "import",
                "init", "param", "property", "receiver", "set", "setparam", "where", "actual",
                "abstract", "annotation", "companion", "const", "crossinline", "data", "enum",
                "expect", "external", "final", "infix", "inline", "inner", "internal", "lateinit",
                "noinline", "open", "operator", "out", "override", "private", "protected", "public",
                "reified", "sealed", "suspend", "tailrec", "vararg", "lazy", "it",
            ),
            types = keywords(
                "Int", "Long", "Short", "Byte", "Float", "Double", "Boolean", "Char", "String",
                "Unit", "Any", "Nothing", "List", "MutableList", "Map", "MutableMap", "Set",
                "MutableSet", "Array", "Pair", "Triple", "Sequence", "Flow", "StateFlow", "Result",
                "IntArray", "LongArray", "ByteArray", "BooleanArray", "StringBuilder", "Regex",
            ),
            builtins = keywords(
                "println", "print", "arrayOf", "listOf", "mutableListOf", "mapOf", "mutableMapOf",
                "setOf", "mutableSetOf", "emptyList", "emptyMap", "emptySet", "require", "check",
                "error", "TODO", "runCatching", "launch", "async", "withContext", "coroutineScope",
                "remember", "mutableStateOf", "rememberCoroutineScope",
            ),
            annotations = true,
            docComment = true,
        )

        private val java = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "abstract", "assert", "break", "case", "catch", "class", "const", "continue",
                "default", "do", "else", "enum", "extends", "final", "finally", "for", "goto", "if",
                "implements", "import", "instanceof", "interface", "native", "new", "package",
                "private", "protected", "public", "return", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient", "try", "volatile", "while",
                "true", "false", "null", "var", "record", "sealed", "permits", "yield",
            ),
            types = keywords(
                "boolean", "byte", "char", "double", "float", "int", "long", "short", "void",
                "String", "Integer", "Long", "Double", "Float", "Boolean", "Character", "Object",
                "List", "ArrayList", "Map", "HashMap", "Set", "HashSet", "Optional", "Stream",
                "Exception", "RuntimeException", "Thread", "Class", "Override", "Deprecated",
            ),
            builtins = keywords(
                "System", "Math", "Arrays", "Collections", "Objects", "StringBuilder", "StringBuffer",
                "printStackTrace", "println", "printf", "valueOf", "toString", "equals", "hashCode",
            ),
            annotations = true,
            docComment = true,
        )

        private val c = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "auto", "break", "case", "char", "const", "continue", "default", "do", "double",
                "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long",
                "register", "restrict", "return", "short", "signed", "sizeof", "static", "struct",
                "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "_Bool",
                "bool", "true", "false", "NULL",
            ),
            types = keywords(
                "int8_t", "int16_t", "int32_t", "int64_t", "uint8_t", "uint16_t", "uint32_t",
                "uint64_t", "size_t", "ssize_t", "ptrdiff_t", "FILE", "wchar_t",
            ),
            builtins = C_BUILTINS,
            preprocessor = true,
        )

        private val cpp = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "auto", "break", "case", "catch", "char", "class", "const", "constexpr", "continue",
                "decltype", "default", "delete", "do", "double", "else", "enum", "explicit",
                "extern", "false", "final", "float", "for", "friend", "goto", "if", "inline", "int",
                "long", "mutable", "namespace", "new", "noexcept", "nullptr", "operator",
                "override", "private", "protected", "public", "register", "return", "short",
                "signed", "sizeof", "static", "static_cast", "dynamic_cast", "reinterpret_cast",
                "const_cast", "struct", "switch", "template", "this", "throw", "true", "try",
                "typedef", "typename", "union", "unsigned", "using", "virtual", "void", "volatile",
                "while", "override", "requires", "concept",
            ),
            types = keywords(
                "int8_t", "int16_t", "int32_t", "int64_t", "uint8_t", "uint16_t", "uint32_t",
                "uint64_t", "size_t", "ptrdiff_t", "string", "vector", "map", "set", "unordered_map",
                "unordered_set", "shared_ptr", "unique_ptr", "optional", "variant", "tuple",
                "pair", "function", "thread", "mutex", "atomic", "FILE", "wchar_t",
            ),
            builtins = C_BUILTINS,
            preprocessor = true,
        )

        private val go = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "break", "case", "chan", "const", "continue", "default", "defer", "else",
                "fallthrough", "for", "func", "go", "goto", "if", "import", "interface", "map",
                "package", "range", "return", "select", "struct", "switch", "type", "var",
            ),
            types = keywords(
                "bool", "byte", "complex64", "complex128", "error", "float32", "float64", "int",
                "int8", "int16", "int32", "int64", "rune", "string", "uint", "uint8", "uint16",
                "uint32", "uint64", "uintptr", "any",
            ),
            builtins = keywords(
                "append", "cap", "close", "complex", "copy", "delete", "imag", "len", "make",
                "new", "panic", "print", "println", "real", "recover", "nil", "true", "false",
                "iota", "fmt", "Println", "Printf", "Errorf", "Sprintf",
            ),
        )

        private val rust = LangSpec(
            profile = Profile.CLike,
            keywords = keywords(
                "as", "async", "await", "break", "const", "continue", "crate", "dyn", "else",
                "enum", "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match",
                "mod", "move", "mut", "pub", "ref", "return", "self", "Self", "static", "struct",
                "super", "trait", "true", "type", "unsafe", "use", "where", "while", "yield",
                "macro_rules", "union",
            ),
            types = keywords(
                "i8", "i16", "i32", "i64", "i128", "isize", "u8", "u16", "u32", "u64", "u128",
                "usize", "f32", "f64", "bool", "char", "str", "String", "Vec", "Box", "Rc", "Arc",
                "Option", "Result", "HashMap", "HashSet", "BTreeMap", "Cow", "Mutex", "RwLock",
            ),
            builtins = keywords(
                "Ok", "Err", "Some", "None", "println", "print", "format", "vec", "panic",
                "assert", "assert_eq", "todo", "unreachable", "write", "writeln", "include",
                "env", "cfg",
            ),
            nestedBlockComment = true,
            docComment = true,
        )

        private val python = LangSpec(
            profile = Profile.Python,
            keywords = keywords(
                "False", "None", "True", "and", "as", "assert", "async", "await", "break",
                "class", "continue", "def", "del", "elif", "else", "except", "finally", "for",
                "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or",
                "pass", "raise", "return", "try", "while", "with", "yield", "match", "case",
            ),
            types = keywords(
                "int", "float", "bool", "str", "bytes", "list", "dict", "set", "tuple", "frozenset",
                "complex", "object", "type", "Exception", "ValueError", "TypeError", "KeyError",
                "IndexError", "RuntimeError", "OSError", "Any", "Optional", "List", "Dict", "Set",
                "Tuple", "Callable", "Iterator", "Iterable", "Union",
            ),
            builtins = keywords(
                "print", "len", "range", "enumerate", "zip", "map", "filter", "sum", "min", "max",
                "abs", "round", "sorted", "reversed", "open", "super", "self", "isinstance",
                "issubclass", "getattr", "setattr", "hasattr", "format", "repr", "input", "id",
                "hash", "iter", "next", "all", "any", "divmod", "pow", "hex", "oct", "bin",
                "staticmethod", "classmethod", "property", "dataclass",
            ),
            tripleQuotes = true,
            hashComment = true,
            slashComment = false,
            docComment = true,
        )

        private val javascript = LangSpec(
            profile = Profile.JavaScript,
            keywords = keywords(
                "async", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
                "default", "delete", "do", "else", "export", "extends", "finally", "for", "from",
                "function", "get", "if", "import", "in", "instanceof", "let", "new", "of",
                "return", "set", "static", "super", "switch", "this", "throw", "try", "typeof",
                "var", "void", "while", "with", "yield", "null", "undefined", "true", "false",
                "NaN", "Infinity",
            ),
            types = keywords(
                "Array", "Object", "String", "Number", "Boolean", "Function", "Promise", "Map",
                "Set", "WeakMap", "WeakSet", "Date", "RegExp", "Error", "TypeError", "Symbol",
                "BigInt", "JSON", "Math", "Reflect", "Proxy", "ArrayBuffer", "Uint8Array",
            ),
            builtins = keywords(
                "console", "window", "document", "globalThis", "setTimeout", "setInterval",
                "clearTimeout", "clearInterval", "fetch", "require", "module", "exports", "process",
                "parseInt", "parseFloat", "isNaN", "encodeURIComponent", "decodeURIComponent",
                "structuredClone", "localStorage", "sessionStorage",
            ),
            templateStrings = true,
        )

        private val typescript = LangSpec(
            profile = Profile.JavaScript,
            keywords = javascript.keywords + keywords(
                "interface", "type", "enum", "namespace", "declare", "implements", "readonly",
                "abstract", "as", "satisfies", "keyof", "infer", "is", "asserts", "public",
                "private", "protected", "override", "any", "unknown", "never", "string", "number",
                "boolean", "symbol", "bigint", "object", "undefined",
            ),
            types = javascript.types,
            builtins = javascript.builtins,
            templateStrings = true,
        )

        private val json = LangSpec(
            profile = Profile.Json,
            keywords = keywords("true", "false", "null"),
            types = emptySet(),
            builtins = emptySet(),
            slashComment = false,
            blockComment = true,
        )

        private val shell = LangSpec(
            profile = Profile.Shell,
            keywords = keywords(
                "if", "then", "else", "elif", "fi", "for", "while", "until", "do", "done",
                "case", "esac", "function", "return", "exit", "local", "export", "readonly",
                "unset", "shift", "source", "alias", "in", "select", "time", "declare", "typeset",
                "read", "trap", "set", "eval", "exec",
            ),
            types = emptySet(),
            builtins = keywords(
                "echo", "cd", "ls", "cp", "mv", "rm", "mkdir", "rmdir", "touch", "cat", "head",
                "tail", "grep", "sed", "awk", "cut", "sort", "uniq", "wc", "find", "xargs", "tar",
                "gzip", "curl", "wget", "git", "sudo", "chmod", "chown", "chgrp", "pwd", "env",
                "which", "ps", "kill", "killall", "test", "printf", "read", "sleep", "ssh", "scp",
                "rsync", "docker", "kubectl", "npm", "pnpm", "yarn", "node", "python", "python3",
                "pip", "java", "gradle", "make", "cmake", "apt", "apt-get", "brew", "systemctl",
            ),
            hashComment = true,
            slashComment = false,
            blockComment = false,
        )

        private val sql = LangSpec(
            profile = Profile.Sql,
            keywords = keywords(
                "select", "from", "where", "insert", "into", "values", "update", "set", "delete",
                "create", "table", "drop", "alter", "add", "column", "index", "view", "join",
                "inner", "left", "right", "outer", "full", "cross", "on", "group", "by", "order",
                "having", "limit", "offset", "distinct", "as", "and", "or", "not", "null", "is",
                "in", "like", "between", "exists", "case", "when", "then", "else", "end", "union",
                "all", "primary", "key", "foreign", "references", "default", "unique", "constraint",
                "int", "integer", "varchar", "text", "char", "date", "datetime", "timestamp",
                "boolean", "float", "double", "decimal", "bigint", "serial", "auto_increment",
                "if", "begin", "commit", "rollback", "truncate", "explain", "asc", "desc",
            ),
            types = emptySet(),
            builtins = keywords(
                "count", "sum", "avg", "min", "max", "coalesce", "cast", "now", "concat", "length",
                "substring", "trim", "upper", "lower", "round", "abs", "date_format", "ifnull",
                "group_concat", "row_number", "rank", "dense_rank", "over", "partition",
            ),
            caseInsensitive = true,
            hashComment = true,
            slashComment = true,
        )

        private val markup = LangSpec(
            profile = Profile.Markup,
            keywords = emptySet(),
            types = emptySet(),
            builtins = emptySet(),
            slashComment = false,
            blockComment = false,
        )

        private val css = LangSpec(
            profile = Profile.Css,
            keywords = keywords(
                "important", "media", "import", "keyframes", "supports", "font-face", "charset",
                "namespace", "page", "layer", "container", "property",
            ),
            types = emptySet(),
            builtins = emptySet(),
            slashComment = false,
            blockComment = true,
        )

        private val yaml = LangSpec(
            profile = Profile.Yaml,
            keywords = keywords("true", "false", "null", "yes", "no", "on", "off", "~"),
            types = emptySet(),
            builtins = emptySet(),
            hashComment = true,
            slashComment = false,
            blockComment = false,
        )

        private val diff = LangSpec(
            profile = Profile.CLike,
            keywords = emptySet(),
            types = emptySet(),
            builtins = emptySet(),
            slashComment = false,
            blockComment = false,
        )

        private val plain = LangSpec(
            profile = Profile.Plain,
            keywords = emptySet(),
            types = emptySet(),
            builtins = emptySet(),
        )

        private fun keywords(vararg values: String): Set<String> = values.toSet()

        private fun builtins(vararg values: String): Set<String> = values.toSet()
    }
}

private class Tokenizer(
    private val code: String,
    private val spec: LangSpec,
    private val palette: SyntaxPalette,
) {
    private val builder = AnnotatedString.Builder()
    private var i = 0

    fun tokenize(): AnnotatedString {
        when (spec.profile) {
            Profile.Plain -> builder.append(code)
            Profile.Markup -> tokenizeMarkup()
            Profile.Css -> tokenizeCss()
            Profile.Json -> tokenizeJson()
            Profile.Yaml -> tokenizeYaml()
            Profile.Sql -> tokenizeSql()
            Profile.Shell -> tokenizeShell()
            Profile.Python -> tokenizeCode()
            Profile.JavaScript -> tokenizeCode()
            Profile.CLike -> tokenizeCode()
        }
        return builder.toAnnotatedString()
    }

    private fun tokenizeCode() {
        while (i < code.length) {
            val c = code[i]
            when {
                c == '\n' -> {
                    builder.append(c)
                    i++
                }
                c.isWhitespace() -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
                spec.docComment && code.startsWith("/**", i) -> {
                    val end = findBlockCommentEnd(i + 3)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                spec.blockComment && code.startsWith("/*", i) -> {
                    val end = findBlockCommentEnd(i + 2)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                spec.slashComment && code.startsWith("//", i) -> {
                    val end = lineEnd(i)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                spec.hashComment && c == '#' -> {
                    val end = lineEnd(i)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                spec.preprocessor && c == '#' && isLineStart(i) -> {
                    val end = lineEnd(i)
                    appendStyled(code.substring(i, end), palette.meta)
                    i = end
                }
                c == '"' || c == '\'' || (spec.templateStrings && c == '`') -> {
                    i = scanString(c)
                }
                spec.tripleQuotes && (code.startsWith("\"\"\"", i) || code.startsWith("'''", i)) -> {
                    val quote = code.substring(i, i + 3)
                    val end = code.indexOf(quote, i + 3)
                    val stop = if (end < 0) code.length else end + 3
                    appendStyled(code.substring(i, stop), palette.string)
                    i = stop
                }
                c.isDigit() || (c == '.' && i + 1 < code.length && code[i + 1].isDigit()) -> {
                    i = scanNumber()
                }
                spec.annotations && c == '@' && i + 1 < code.length && isIdentifierStart(code[i + 1]) -> {
                    var j = i + 1
                    while (j < code.length && isIdentifierPart(code[j])) j++
                    appendStyled(code.substring(i, j), palette.property)
                    i = j
                }
                isIdentifierStart(c) -> {
                    var j = i
                    while (j < code.length && isIdentifierPart(code[j])) j++
                    val word = code.substring(i, j)
                    val color = classify(word, i, j)
                    if (color != null) appendStyled(word, color) else appendPlainRange(i, j)
                    i = j
                }
                c == ':' && spec.profile == Profile.CLike && code.getOrNull(i - 1) == ':' -> {
                    appendStyledRange(i - 1, i + 1, palette.operator)
                    i++
                }
                c in OPERATORS -> {
                    var j = i
                    while (j < code.length && code[j] in OPERATORS) j++
                    appendStyled(code.substring(i, j), palette.operator)
                    i = j
                }
                c in BRACKETS -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                c == ',' || c == ';' || c == '.' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun classify(word: String, start: Int, end: Int): Color? {
        val normalized = if (spec.caseInsensitive) word.lowercase() else word
        if (spec.keywords.contains(normalized)) return palette.keyword
        if (spec.types.contains(normalized)) return palette.type
        if (spec.builtins.contains(normalized)) return palette.builtin
        if (spec.profile == Profile.CLike && word.length > 1 && word.all { it.isUpperCase() || it.isDigit() || it == '_' } && word.any { it.isLetter() }) {
            return palette.constant
        }
        if (spec.profile == Profile.CLike && word == "self") return palette.keyword
        val next = code.getOrNull(end) ?: ' '
        val prev = previousNonSpace(start)
        if (next == '(') return palette.function
        if (prev == '.' && !(word.length > 1 && word[0].isUpperCase())) return palette.property
        if (word.length > 2 && word[0].isUpperCase() && word.any { it.isLowerCase() }) return palette.type
        return null
    }

    private fun tokenizeJson() {
        while (i < code.length) {
            val c = code[i]
            when {
                c.isWhitespace() -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
                c == '"' -> {
                    val end = scanStringEnd(i, '"')
                    val text = code.substring(i, end)
                    val next = nextNonSpace(end)
                    val color = if (next == ':') palette.property else palette.string
                    appendStyled(text, color)
                    i = end
                }
                c.isDigit() || c == '-' -> {
                    i = scanNumber()
                }
                code.startsWith("true", i) || code.startsWith("null", i) -> {
                    appendStyled(code.substring(i, i + 4), palette.keyword)
                    i += 4
                }
                code.startsWith("false", i) -> {
                    appendStyled(code.substring(i, i + 5), palette.keyword)
                    i += 5
                }
                c in "{}[]:," -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun tokenizeMarkup() {
        while (i < code.length) {
            val c = code[i]
            when {
                code.startsWith("<!--", i) -> {
                    val end = code.indexOf("-->", i + 4)
                    val stop = if (end < 0) code.length else end + 3
                    appendComment(code.substring(i, stop), palette.comment)
                    i = stop
                }
                code.startsWith("<![CDATA[", i) -> {
                    val end = code.indexOf("]]>", i + 9)
                    val stop = if (end < 0) code.length else end + 3
                    appendStyled(code.substring(i, stop), palette.meta)
                    i = stop
                }
                c == '<' && i + 1 < code.length && (code[i + 1] == '!' || code[i + 1] == '?') -> {
                    val end = code.indexOf('>', i)
                    val stop = if (end < 0) code.length else end + 1
                    appendStyled(code.substring(i, stop), palette.meta)
                    i = stop
                }
                c == '<' && i + 1 < code.length && (isIdentifierStart(code[i + 1]) || code[i + 1] == '/') -> {
                    var j = i + 1
                    if (code[j] == '/') j++
                    appendStyled("<" + if (j - 1 > i && code[i + 1] == '/') "/" else "", palette.punctuation)
                    var nameStart = j
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '-' || code[j] == ':')) j++
                    appendStyled(code.substring(nameStart, j), palette.tag)
                    i = j
                    scanAttributes()
                }
                c == '&' -> {
                    val end = code.indexOf(';', i)
                    if (end in (i + 1)..(i + 10)) {
                        appendStyled(code.substring(i, end + 1), palette.constant)
                        i = end + 1
                    } else {
                        appendPlainRange(i, i + 1)
                        i++
                    }
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun scanAttributes() {
        while (i < code.length) {
            val c = code[i]
            when {
                c == '>' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                    return
                }
                code.startsWith("/>", i) -> {
                    appendStyledRange(i, i + 2, palette.punctuation)
                    i += 2
                    return
                }
                c.isWhitespace() || c == '\n' -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
                c == '=' -> {
                    appendStyledRange(i, i + 1, palette.operator)
                    i++
                }
                c == '"' || c == '\'' -> {
                    i = scanString(c)
                }
                isIdentifierStart(c) || c == '-' || c == ':' -> {
                    var j = i
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '-' || code[j] == ':')) j++
                    appendStyled(code.substring(i, j), palette.attribute)
                    i = j
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun tokenizeCss() {
        var depth = 0
        while (i < code.length) {
            val c = code[i]
            when {
                code.startsWith("/*", i) -> {
                    val end = findBlockCommentEnd(i + 2)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                c == '{' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    depth++
                    i++
                }
                c == '}' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    depth = (depth - 1).coerceAtLeast(0)
                    i++
                }
                c == '"' || c == '\'' -> i = scanString(c)
                c == '@' -> {
                    var j = i + 1
                    while (j < code.length && isIdentifierPart(code[j])) j++
                    appendStyled(code.substring(i, j), palette.keyword)
                    i = j
                }
                c == '#' && i + 1 < code.length && isHexDigit(code[i + 1]) -> {
                    var j = i + 1
                    while (j < code.length && isHexDigit(code[j])) j++
                    appendStyled(code.substring(i, j), palette.number)
                    i = j
                }
                c.isDigit() || (c == '.' && i + 1 < code.length && code[i + 1].isDigit()) || ((c == '-' || c == '+') && i + 1 < code.length && code[i + 1].isDigit()) -> {
                    i = scanNumberWithUnit()
                }
                isIdentifierStart(c) || c == '-' -> {
                    var j = i
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '-')) j++
                    val word = code.substring(i, j)
                    val next = nextNonSpace(j)
                    val color = when {
                        word.startsWith("-") -> palette.property
                        depth > 0 && next == ':' -> palette.property
                        next == '(' -> palette.function
                        spec.keywords.contains(word.lowercase()) -> palette.keyword
                        else -> null
                    }
                    if (color != null) appendStyled(word, color) else appendPlainRange(i, j)
                    i = j
                }
                c in OPERATORS -> {
                    var j = i
                    while (j < code.length && code[j] in OPERATORS) j++
                    appendStyled(code.substring(i, j), palette.operator)
                    i = j
                }
                c == ';' || c == ':' || c == ',' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun tokenizeYaml() {
        while (i < code.length) {
            val c = code[i]
            when {
                c == '\n' -> {
                    builder.append(c)
                    i++
                }
                c == '#' -> {
                    val end = lineEnd(i)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                c == '"' || c == '\'' -> i = scanString(c)
                (c.isDigit() || c == '-' || c == '.') && (c == '-' || c.isDigit() || c == '.') && looksLikeValue() -> {
                    i = scanNumber()
                }
                c == '-' && i + 1 < code.length && code[i + 1] == ' ' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                isIdentifierStart(c) -> {
                    var j = i
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '-' || code[j] == '.')) j++
                    val word = code.substring(i, j)
                    val next = nextNonSpace(j)
                    val color = when {
                        next == ':' -> palette.property
                        spec.keywords.contains(word.lowercase()) -> palette.keyword
                        else -> null
                    }
                    if (color != null) appendStyled(word, color) else appendPlainRange(i, j)
                    i = j
                }
                c == '[' || c == ']' || c == '{' || c == '}' || c == ':' || c == ',' -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun tokenizeSql() {
        while (i < code.length) {
            val c = code[i]
            when {
                code.startsWith("--", i) -> {
                    val end = lineEnd(i)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                code.startsWith("/*", i) -> {
                    val end = findBlockCommentEnd(i + 2)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                c == '\'' || c == '"' || c == '`' -> i = scanString(c)
                c.isDigit() -> i = scanNumber()
                isIdentifierStart(c) -> {
                    var j = i
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '_')) j++
                    val word = code.substring(i, j)
                    val lower = word.lowercase()
                    val color = when {
                        spec.keywords.contains(lower) -> palette.keyword
                        spec.builtins.contains(lower) -> palette.function
                        nextNonSpace(j) == '(' -> palette.function
                        else -> null
                    }
                    if (color != null) appendStyled(word, color) else appendPlainRange(i, j)
                    i = j
                }
                c in OPERATORS -> {
                    var j = i
                    while (j < code.length && code[j] in OPERATORS) j++
                    appendStyled(code.substring(i, j), palette.operator)
                    i = j
                }
                c in "(),;." -> {
                    appendStyledRange(i, i + 1, palette.punctuation)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun tokenizeShell() {
        while (i < code.length) {
            val c = code[i]
            when {
                c == '#' -> {
                    val end = lineEnd(i)
                    appendComment(code.substring(i, end), palette.comment)
                    i = end
                }
                c == '"' || c == '\'' -> i = scanString(c)
                c == '$' -> {
                    var j = i + 1
                    if (j < code.length && code[j] == '{') {
                        val close = code.indexOf('}', j)
                        j = if (close < 0) code.length else close + 1
                    } else if (j < code.length && isIdentifierStart(code[j])) {
                        while (j < code.length && isIdentifierPart(code[j])) j++
                    } else if (j < code.length && code[j] == '(') {
                        val close = code.indexOf(')', j)
                        j = if (close < 0) code.length else close + 1
                    }
                    appendStyled(code.substring(i, j), palette.variable)
                    i = j
                }
                c.isDigit() -> i = scanNumber()
                isIdentifierStart(c) -> {
                    var j = i
                    while (j < code.length && (isIdentifierPart(code[j]) || code[j] == '-' || code[j] == '.')) j++
                    val word = code.substring(i, j)
                    val color = when {
                        spec.keywords.contains(word) -> palette.keyword
                        spec.builtins.contains(word) -> palette.builtin
                        else -> null
                    }
                    if (color != null) appendStyled(word, color) else appendPlainRange(i, j)
                    i = j
                }
                c in "|&;<>()" -> {
                    appendStyledRange(i, i + 1, palette.operator)
                    i++
                }
                else -> {
                    appendPlainRange(i, i + 1)
                    i++
                }
            }
        }
    }

    private fun scanString(quote: Char): Int {
        if ((quote == '"' || quote == '\'') && i + 2 < code.length && code[i + 1] == quote && code[i + 2] == quote) {
            val triple = "$quote$quote$quote"
            val end = code.indexOf(triple, i + 3)
            val stop = if (end < 0) code.length else end + 3
            appendStyled(code.substring(i, stop), palette.string)
            return stop
        }
        val stop = scanStringEnd(i, quote)
        appendStyled(code.substring(i, stop), palette.string)
        return stop
    }

    private fun scanStringEnd(start: Int, quote: Char): Int {
        var j = start + 1
        while (j < code.length) {
            val c = code[j]
            if (c == '\\') {
                j += 2
                continue
            }
            if (c == quote) return j + 1
            if (c == '\n' && quote != '`') return j
            j++
        }
        return code.length
    }

    private fun scanNumber(): Int {
        var j = i
        if (code[j] == '-' || code[j] == '+') j++
        if (j + 1 < code.length && code[j] == '0' && (code[j + 1] == 'x' || code[j + 1] == 'X')) {
            j += 2
            while (j < code.length && (isHexDigit(code[j]) || code[j] == '_')) j++
            appendStyled(code.substring(i, j), palette.number)
            i = j
            return i
        }
        if (j + 1 < code.length && code[j] == '0' && (code[j + 1] == 'b' || code[j + 1] == 'B')) {
            j += 2
            while (j < code.length && (code[j] == '0' || code[j] == '1' || code[j] == '_')) j++
            appendStyled(code.substring(i, j), palette.number)
            i = j
            return i
        }
        while (j < code.length && (code[j].isDigit() || code[j] == '_')) j++
        if (j < code.length && code[j] == '.') {
            j++
            while (j < code.length && (code[j].isDigit() || code[j] == '_')) j++
        }
        if (j < code.length && (code[j] == 'e' || code[j] == 'E')) {
            val save = j
            j++
            if (j < code.length && (code[j] == '+' || code[j] == '-')) j++
            if (j < code.length && code[j].isDigit()) {
                while (j < code.length && code[j].isDigit()) j++
            } else {
                j = save
            }
        }
        while (j < code.length && (code[j] == 'f' || code[j] == 'F' || code[j] == 'd' || code[j] == 'D' || code[j] == 'l' || code[j] == 'L' || code[j] == 'u' || code[j] == 'U' || code[j] == 'i' || code[j] == 's' || code[j] == 'z')) j++
        appendStyled(code.substring(i, j), palette.number)
        i = j
        return i
    }

    private fun scanNumberWithUnit(): Int {
        val start = i
        scanNumberSilently()
        while (i < code.length && (code[i].isLetter() || code[i] == '%')) i++
        appendStyled(code.substring(start, i), palette.number)
        return i
    }

    private fun scanNumberSilently() {
        var j = i
        if (code[j] == '-' || code[j] == '+') j++
        while (j < code.length && (code[j].isDigit() || code[j] == '.' || code[j] == '_')) j++
        i = j
    }

    private fun findBlockCommentEnd(from: Int): Int {
        var j = from
        var depth = 1
        while (j < code.length) {
            if (spec.nestedBlockComment && code.startsWith("/*", j)) {
                depth++
                j += 2
                continue
            }
            if (code.startsWith("*/", j)) {
                depth--
                j += 2
                if (depth == 0) return j
                continue
            }
            j++
        }
        return code.length
    }

    private fun lineEnd(from: Int): Int {
        val index = code.indexOf('\n', from)
        return if (index < 0) code.length else index
    }

    private fun isLineStart(position: Int): Boolean {
        var j = position - 1
        while (j >= 0 && (code[j] == ' ' || code[j] == '\t')) j--
        return j < 0 || code[j] == '\n'
    }

    private fun previousNonSpace(from: Int): Char? {
        var j = from - 1
        while (j >= 0 && code[j].isWhitespace()) j--
        return if (j >= 0) code[j] else null
    }

    private fun nextNonSpace(from: Int): Char? {
        var j = from
        while (j < code.length && (code[j] == ' ' || code[j] == '\t')) j++
        return if (j < code.length) code[j] else null
    }

    private fun looksLikeValue(): Boolean {
        var j = i - 1
        while (j >= 0 && (code[j] == ' ' || code[j] == '\t')) j--
        return j >= 0 && (code[j] == ':' || code[j] == '-')
    }

    private fun appendPlainRange(start: Int, end: Int) {
        builder.append(code.substring(start, end))
    }

    private fun appendStyled(text: String, color: Color) {
        builder.pushStyle(SpanStyle(color = color))
        builder.append(text)
        builder.pop()
    }

    private fun appendComment(text: String, color: Color) {
        builder.pushStyle(SpanStyle(color = color, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
        builder.append(text)
        builder.pop()
    }

    private fun appendStyledRange(start: Int, end: Int, color: Color) {
        appendStyled(code.substring(start, end), color)
    }

    private fun isIdentifierStart(c: Char): Boolean = c == '_' || c == '$' || c.isLetter()

    private fun isIdentifierPart(c: Char): Boolean = c == '_' || c == '$' || c.isLetterOrDigit()

    private fun isHexDigit(c: Char): Boolean =
        c.isDigit() || (c in 'a'..'f') || (c in 'A'..'F')

    private companion object {
        val OPERATORS = "+-*/%=!<>&|^~?:".toSet()
        val BRACKETS = "()[]{}".toSet()
    }
}

@Composable
internal fun rememberCodeText(code: String, language: String, colors: MarkdownColors): AnnotatedString {
    return remember(code, language, colors.isDark) {
        SyntaxHighlighter.highlight(code, language, colors.syntax, colors.isDark)
    }
}
